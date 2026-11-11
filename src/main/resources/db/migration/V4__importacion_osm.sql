-- Origen del dato y referencia externa para actualizaciones sin duplicados.
ALTER TABLE alojamientos
    ADD COLUMN fuente_dato VARCHAR(20) NOT NULL DEFAULT 'COMUNIDAD'
        CHECK (fuente_dato IN ('COMUNIDAD', 'OSM', 'ADMIN')),
    ADD COLUMN osm_tipo CHAR(1) CHECK (osm_tipo IN ('n', 'w', 'r')),
    ADD COLUMN osm_id BIGINT,
    ADD COLUMN sin_datos_completos BOOLEAN NOT NULL DEFAULT TRUE;

-- Un mismo elemento de OSM no se importa dos veces.
CREATE UNIQUE INDEX uq_alojamiento_osm
    ON alojamientos (osm_tipo, osm_id)
    WHERE osm_id IS NOT NULL;