package pe.telodije.api.alojamientos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DetalleAlojamientoRepository extends JpaRepository<Alojamiento, UUID> {

    @Query(value = """
        SELECT t.franja::text          AS "franja",
               t.precio_pen            AS "precioPen",
               th.nombre               AS "tipoHabitacion",
               t.confianza             AS "confianza"
        FROM tarifas t
        JOIN tipos_habitacion th ON th.id = t.tipo_habitacion_id
        WHERE th.alojamiento_id = :alojamientoId
          AND t.vigente_hasta > now()
        ORDER BY t.franja, t.precio_pen
        """, nativeQuery = true)
    List<TarifaProyeccion> tarifasVigentes(@Param("alojamientoId") UUID alojamientoId);

    @Query(value = """
        SELECT s.codigo                 AS "codigo",
               s.nombre                 AS "nombre",
               COALESCE(rs.estado::text, 'DESCONOCIDO') AS "estado"
        FROM servicios s
        LEFT JOIN resena_servicios rs ON rs.servicio_id = s.id
        LEFT JOIN resenas r ON r.id = rs.resena_id
             AND r.alojamiento_id = :alojamientoId
             AND r.estado_publicacion = 'APROBADO'
        WHERE s.activo = TRUE
        ORDER BY s.id
        """, nativeQuery = true)
    List<ServicioProyeccion> servicios(@Param("alojamientoId") UUID alojamientoId);

    @Query(value = """
    SELECT a.nombre                 AS "nombre",
           a.tipo::text             AS "tipo",
           a.direccion              AS "direccion",
           a.tiene_cochera          AS "tieneCochera",
           a.calificacion_promedio  AS "calificacionPromedio",
           a.total_resenas          AS "totalResenas"
    FROM alojamientos a
    WHERE a.id = :alojamientoId
    """, nativeQuery = true)
    DatosAlojamientoProyeccion datos(@Param("alojamientoId") UUID alojamientoId);
}