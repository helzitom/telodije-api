package pe.telodije.api.resenas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResenaServicioRepository extends JpaRepository<ResenaServicio, ResenaServicioId> {
}