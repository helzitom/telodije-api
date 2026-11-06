# Prueba de regresión del flujo completo.
# Requiere: API corriendo en localhost:8080, base de datos levantada,
# y las variables APP_ALIAS, APP_PASSWORD y FOTO_PRUEBA definidas.

$ErrorActionPreference = "Stop"
$base = "http://localhost:8080"
$utf8 = New-Object System.Text.UTF8Encoding($false)

function Paso($descripcion, [scriptblock]$accion) {
    try {
        & $accion
        Write-Host "[OK]   $descripcion" -ForegroundColor Green
    } catch {
        Write-Host "[FAIL] $descripcion :: $($_.Exception.Message)" -ForegroundColor Red
        throw
    }
}

# Nombre único por ejecución, sin acentos para evitar problemas de codificación
$nombre = "Prueba Regresion $(Get-Random)"

# 1. Login
$login = @{ identificador = $env:APP_ALIAS; password = $env:APP_PASSWORD } | ConvertTo-Json
Paso "Login" {
    $script:tokens = Invoke-RestMethod -Method Post -Uri "$base/api/v1/auth/login" `
        -ContentType "application/json" -Body $login
    if (-not $script:tokens.accessToken) { throw "Sin accessToken" }
}
$headers = @{ Authorization = "Bearer $($tokens.accessToken)" }

# 2. Insertar hospedaje de prueba con nombre único
Paso "Preparar hospedaje de prueba" {
    $sql = "WITH a AS (INSERT INTO alojamientos (nombre, tipo, ubicacion, direccion, tiene_cochera, estado_publicacion) VALUES ('$nombre', 'HOSPEDAJE', ST_SetSRID(ST_MakePoint(-77.0320, -12.0910), 4326)::geography, 'Prueba', FALSE, 'APROBADO') RETURNING id), th AS (INSERT INTO tipos_habitacion (alojamiento_id, nombre, capacidad) SELECT id, 'Simple', 1 FROM a RETURNING id) INSERT INTO tarifas (tipo_habitacion_id, franja, precio_pen, fuente) SELECT id, 'H4', 25.00, 'ADMIN' FROM th;"
    $salida = $sql | docker exec -i hospedajes-db psql -U telodije_app -d telodije-db 2>&1
    if ($salida -match "ERROR") { throw "Inserción fallida: $salida" }
}

# 3. Buscar el hospedaje recién creado
Paso "Búsqueda por radio" {
    $candidatos = Invoke-RestMethod -Uri "$base/api/v1/alojamientos/cercanos?lat=-12.0910&lng=-77.0320&radioMetros=300&limite=100"
    $script:hostal = $candidatos | Where-Object { $_.nombre -eq $nombre } | Select-Object -First 1
    if (-not $hostal.id) { throw "No apareció el hospedaje de prueba" }
}

# 4. Pedir clave de foto
Paso "Pedir clave de foto" {
    $script:intento = Invoke-RestMethod -Method Post -Uri "$base/api/v1/fotos/llegada/intento" -Headers $headers
    if (-not $script:intento.clave) { throw "Sin clave" }
}

# 5. Subir foto
Paso "Subir foto" {
    $resp = Invoke-WebRequest -Method Put -Uri "$base$($intento.urlSubida)" -Headers $headers `
        -ContentType "image/jpeg" -InFile $env:FOTO_PRUEBA -UseBasicParsing
    if ($resp.StatusCode -ne 204) { throw "Estado $($resp.StatusCode)" }
}

# 6. Registrar visita
Paso "Registrar visita" {
    $cuerpo = @{
        alojamientoId  = $hostal.id
        franja         = "H4"
        horas          = 4
        montoPagadoPen = 25
        llegadaEn      = (Get-Date).ToUniversalTime().ToString("o")
        claveFoto      = $intento.clave
    } | ConvertTo-Json
    $script:visita = Invoke-RestMethod -Method Post -Uri "$base/api/v1/visitas" `
        -Headers $headers -ContentType "application/json" -Body $cuerpo
    if (-not $script:visita.id) { throw "Sin id de visita" }
}

# 7. Reseña válida
Paso "Reseña válida" {
    $cuerpo = @{
        visitaId     = $visita.id
        calificacion = 4
        volveria     = $true
        comentario   = "Buen lugar para pasar la tarde."
        servicios    = @(@{ servicioId = 1; estado = "PRESENTE" })
    } | ConvertTo-Json -Depth 4
    $script:resena = Invoke-RestMethod -Method Post -Uri "$base/api/v1/resenas" `
        -Headers $headers -ContentType "application/json" -Body $cuerpo
    if ($resena.estado -ne "APROBADO") { throw "Estado $($resena.estado)" }
}

# 8. Segunda reseña para el mismo hospedaje debe rechazarse
Paso "Reseña duplicada rechazada (409)" {
    $cuerpo = @{
        visitaId = $visita.id; calificacion = 3; volveria = $false
    } | ConvertTo-Json
    try {
        Invoke-RestMethod -Method Post -Uri "$base/api/v1/resenas" -Headers $headers `
            -ContentType "application/json" -Body $cuerpo | Out-Null
        throw "Se aceptó una reseña duplicada"
    } catch [System.Net.WebException] {
        if ([int]$_.Exception.Response.StatusCode -ne 409) { throw }
    }
}

# 9. Listado público, sin token y sin exponer autor
Paso "Listado público sin token" {
    $lista = Invoke-RestMethod -Uri "$base/api/v1/alojamientos/$($hostal.id)/resenas"
    if (-not $lista) { throw "Listado vacío" }
    if ($lista | Get-Member -Name autor -ErrorAction SilentlyContinue) { throw "Expone autor" }
}

# 10. Ruta protegida sin token debe responder 401
Paso "Ruta protegida sin token responde 401" {
    try {
        Invoke-RestMethod -Uri "$base/api/v1/visitas/mias" | Out-Null
        throw "Acceso sin token"
    } catch [System.Net.WebException] {
        if ([int]$_.Exception.Response.StatusCode -ne 401) { throw }
    }
}

Write-Host "`nRegresión completa." -ForegroundColor Cyan