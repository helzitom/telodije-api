package pe.telodije.api.resenas;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.telodije.api.alojamientos.EstadoPublicacion;
import org.springframework.data.domain.Pageable;


import java.util.List;
import java.util.UUID;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, UUID> {

    boolean existsByUsuarioIdAndAlojamientoId(UUID usuarioId, UUID alojamientoId);

    boolean existsByVisitaId(UUID visitaId);

    Page<Resena> findByEstadoPublicacionOrderByIdAsc(EstadoPublicacion estado, Pageable pageable);

    @Query(value = """
    SELECT r.id                 AS "id",
           r.calificacion       AS "calificacion",
           r.volveria           AS "volveria",
           r.comentario         AS "comentario",
           r.fecha_resena       AS "fechaResena",
           rd.texto             AS "respuestaDueno"
    FROM resenas r
    LEFT JOIN respuestas_dueno rd ON rd.resena_id = r.id
    WHERE r.alojamiento_id = :alojamientoId
      AND r.estado_publicacion = 'APROBADO'
    ORDER BY r.fecha_resena DESC
    LIMIT :limite OFFSET :desplazamiento
    """, nativeQuery = true)
    List<ResenaPublicaProyeccion> listarPublicas(@Param("alojamientoId") UUID alojamientoId,
                                                 @Param("limite") int limite,
                                                 @Param("desplazamiento") int desplazamiento);
}