package pe.telodije.api.resenas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, UUID> {

    boolean existsByUsuarioIdAndAlojamientoId(UUID usuarioId, UUID alojamientoId);

    boolean existsByVisitaId(UUID visitaId);
}