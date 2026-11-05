package pe.telodije.api.identidad;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SesionRepository extends JpaRepository<SesionUsuario, UUID> {

    //Búsqueda de la sesión del Usuario según el token encriptado

    Optional<SesionUsuario> findByTokenHash(String tokenHash);


    //Query para poder revocar la sesión del usuario
    @Modifying
    @Query("""
        UPDATE SesionUsuario s
        SET s.revocadaEn = :ahora
        WHERE s.usuarioId = :usuarioId AND s.revocadaEn IS NULL
        """)
    int revocarTodasDeUsuario(@Param("usuarioId") UUID usuarioId,
                              @Param("ahora") Instant ahora);
}