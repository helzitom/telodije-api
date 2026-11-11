-- ============================================================
-- V2: estados de moderación de reseñas, servicios con tres
-- valores, reportes de reseñas y respuestas de dueños verificados
-- ============================================================

-- 1. Nuevo estado de moderación: reseña dudosa para revisión
ALTER TYPE estado_publicacion ADD VALUE IF NOT EXISTS 'EN_REVISION';

-- 2. Estado de presencia de cada servicio
CREATE TYPE estado_servicio AS ENUM ('PRESENTE', 'AUSENTE', 'DESCONOCIDO');

-- Reemplaza el booleano por el enum, conservando los datos existentes
ALTER TABLE resena_servicios
    ALTER COLUMN presente DROP NOT NULL;
ALTER TABLE resena_servicios
    ALTER COLUMN presente TYPE estado_servicio
    USING (CASE WHEN presente THEN 'PRESENTE' ELSE 'AUSENTE' END)::estado_servicio;
ALTER TABLE resena_servicios
    RENAME COLUMN presente TO estado;
ALTER TABLE resena_servicios
    ALTER COLUMN estado SET NOT NULL;

-- 3. Verificación de dueños de hospedajes
CREATE TABLE verificaciones_dueno (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    alojamiento_id  UUID NOT NULL REFERENCES alojamientos(id) ON DELETE CASCADE,
    usuario_id      UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    documento_clave TEXT NOT NULL,
    foto_local_clave TEXT NOT NULL,
    estado          VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
                    CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA')),
    revisado_por    UUID REFERENCES usuarios(id) ON DELETE SET NULL,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    revisado_en     TIMESTAMPTZ
);

-- Solo una verificación aprobada por hospedaje
CREATE UNIQUE INDEX uq_verificacion_aprobada
    ON verificaciones_dueno (alojamiento_id)
    WHERE estado = 'APROBADA';

-- 4. Reportes de reseñas hechos por usuarios
CREATE TABLE reportes_resena (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resena_id   UUID NOT NULL REFERENCES resenas(id) ON DELETE CASCADE,
    usuario_id  UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    motivo      VARCHAR(40) NOT NULL
                CHECK (motivo IN ('SPAM', 'OFENSIVO', 'FALSO', 'OTRO')),
    detalle     VARCHAR(300),
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- Un usuario reporta una reseña una sola vez
    CONSTRAINT un_reporte_por_usuario UNIQUE (resena_id, usuario_id)
);

CREATE INDEX idx_reportes_resena ON reportes_resena (resena_id);

-- 5. Respuesta pública del dueño a una reseña (solo hospedajes verificados)
CREATE TABLE respuestas_dueno (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resena_id   UUID NOT NULL UNIQUE REFERENCES resenas(id) ON DELETE CASCADE,
    usuario_id  UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    texto       VARCHAR(1000) NOT NULL,
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 6. Vista pública actualizada: incluye servicios para mostrarlos en el detalle
CREATE OR REPLACE VIEW v_resenas_publicas AS
SELECT r.id,
       r.alojamiento_id,
       'Anónimo'::text AS autor,
       r.calificacion,
       r.volveria,
       r.comentario,
       r.fecha_resena,
       rd.texto AS respuesta_dueno
FROM resenas r
LEFT JOIN respuestas_dueno rd ON rd.resena_id = r.id
WHERE r.estado_publicacion IN ('APROBADO');