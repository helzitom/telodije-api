-- V3: visibilidad patrocinada (nunca altera calificación ni orden de precio).
ALTER TABLE alojamientos
    ADD COLUMN IF NOT EXISTS patrocinado_hasta TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_alojamientos_patrocinados
    ON alojamientos (patrocinado_hasta)
    WHERE patrocinado_hasta IS NOT NULL;
