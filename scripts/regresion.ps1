# Prueba de regresión del flujo completo.
# Requiere: API corriendo en localhost:8080, base con hospedaje aprobado,
# y las variables APP_ALIAS, APP_PASSWORD y FOTO_PRUEBA definidas.

$ErrorActionPreference = "Stop"
$base = "http://localhost:8080"
$utf8 = New-Object System.Text.UTF8Encoding($false)

function Paso($nombre, [scriptblock]$accion) {
    try {
        & $accion
        Write-Host "[OK]   $nombre" -ForegroundColor Green
    } catch {
        Write-Host "[FAIL] $nombre :: $($_.Exception.Message)" -ForegroundColor Red
        throw
    }
}

$login = @{ identificador = $env:APP_ALIAS; password = $env:APP_PASSWORD } | ConvertTo-Json
$tokens = $null
Paso "Login" {
    $script:tokens = Invoke-RestMethod -Method Post -Uri "$base/api/v1/auth/login" `
        -ContentType "application/json" -Body $login
    if (-not $script:tokens.accessToken) { throw "Sin accessToken" }
}
$headers = @{ Authorization = "Bearer $($tokens.accessToken)" }

Paso "Búsqueda por radio" {
    $script:hostal = (Invoke-RestMethod -Uri "$base/api/v1/alojamientos/cercanos?lat=-12.0931&lng=-77.0364&radioMetros=2000")[0]
    if (-not $script:hostal.id) { throw "Sin resultados" }
}

Paso "Pedir clave de foto" {
    $script:intento = Invoke-RestMethod -Method Post -Uri "$base/api/v1/fotos/llegada/intento" -Headers $headers
    if (-not $script:intento.clave) { throw "Sin clave" }
}

Paso "Subir foto" {
    $resp = Invoke-WebRequest -Method Put -Uri "$base$($intento.urlSubida)" -Headers $headers `
        -ContentType "image/jpeg" -InFile $env:FOTO_PRUEBA -UseBasicParsing
    if ($resp.StatusCode -ne 204) { throw "Estado $($resp.StatusCode)" }
}

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

Paso "Listado público sin token" {
    $lista = Invoke-RestMethod -Uri "$base/api/v1/alojamientos/$($hostal.id)/resenas"
    if (-not $lista) { throw "Listado vacío" }
    if ($lista | Get-Member -Name autor -ErrorAction SilentlyContinue) { throw "Expone autor" }
}

Paso "Ruta protegida sin token responde 401" {
    try {
        Invoke-RestMethod -Uri "$base/api/v1/visitas/mias" | Out-Null
        throw "Acceso sin token"
    } catch [System.Net.WebException] {
        if ([int]$_.Exception.Response.StatusCode -ne 401) { throw }
    }
}

Write-Host "`nRegresión completa." -ForegroundColor Cyan