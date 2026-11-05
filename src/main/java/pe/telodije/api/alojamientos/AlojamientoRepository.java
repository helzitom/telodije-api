package pe.telodije.api.alojamientos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface AlojamientoRepository extends JpaRepository<Alojamiento, UUID> {

    @Query(value = """
        SELECT a.id                     AS "id",
               a.nombre                 AS "nombre",
               a.tipo::text             AS "tipo",
               a.tiene_cochera          AS "tieneCochera",
               a.calificacion_promedio  AS "calificacionPromedio",
               a.total_resenas          AS "totalResenas",
               ST_Distance(a.ubicacion,
                   ST_MakePoint(:lng, :lat)::geography) AS "distanciaMetros",
               MIN(t.precio_pen)        AS "precioDesde"
        FROM alojamientos a
        JOIN tipos_habitacion th ON th.alojamiento_id = a.id
        JOIN tarifas t           ON t.tipo_habitacion_id = th.id
                                AND t.vigente_hasta > now()
        WHERE a.estado_publicacion = 'APROBADO'
          AND ST_DWithin(a.ubicacion,
                         ST_MakePoint(:lng, :lat)::geography,
                         :radioMetros)
          AND (CAST(:franja AS franja_estadia) IS NULL
               OR t.franja = CAST(:franja AS franja_estadia))
          AND (CAST(:precioMax AS numeric) IS NULL
               OR t.precio_pen <= CAST(:precioMax AS numeric))
          AND (CAST(:tipoHabitacion AS text) IS NULL
               OR th.nombre = CAST(:tipoHabitacion AS text))
          AND (:requiereCochera = FALSE OR a.tiene_cochera = TRUE)
        GROUP BY a.id, a.nombre, a.tipo, a.tiene_cochera,
                 a.calificacion_promedio, a.total_resenas, a.ubicacion
        ORDER BY "distanciaMetros"
        LIMIT :limite
        """, nativeQuery = true)

    List<BusquedaProyeccion> buscarCercanos(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radioMetros") double radioMetros,
            @Param("franja") String franja,
            @Param("precioMax") BigDecimal precioMax,
            @Param("tipoHabitacion") String tipoHabitacion,
            @Param("requiereCochera") boolean requiereCochera,
            @Param("limite") int limite);
}