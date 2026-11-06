package pe.telodije.api.visitas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VisitaRepository extends JpaRepository<Visita, UUID> {

    List<Visita> findByUsuarioIdOrderByLlegadaEnDesc(UUID usuarioId);
}