package pe.telodije.api.multimedia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FotoRepository extends JpaRepository<Foto, UUID> {

    boolean existsBySha256(String sha256);
}