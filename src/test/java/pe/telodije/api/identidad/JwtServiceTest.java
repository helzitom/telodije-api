package pe.telodije.api.identidad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;


class JwtServiceTest {

    private final String secreto = Base64.getEncoder()
            .encodeToString(new SecureRandom().generateSeed(32));


    //Test unitario para emitir y validar los tokens JWT
    @Test
    void emiteYValidaTokenDeAcceso() {
        JwtService jwt = new JwtService(secreto, 15);
        UUID id = UUID.randomUUID();

        String token = jwt.emitirAcceso(id, RolUsuario.USUARIO);
        Claims claims = jwt.validar(token);

        assertEquals(id.toString(), claims.getSubject());
        assertEquals("USUARIO", claims.get("rol", String.class));
    }

    //Test unitario para rechazar un token alterado
    @Test
    void rechazaTokenAlterado() {
        JwtService jwt = new JwtService(secreto, 15);
        String token = jwt.emitirAcceso(UUID.randomUUID(), RolUsuario.ADMIN);

        assertThrows(JwtException.class, () -> jwt.validar(token + "x"));
    }

    //Test unitario que rechaza un secret debil (corto o peligroso)
    @Test
    void rechazaSecretoDebil() {
        String corto = Base64.getEncoder().encodeToString(new byte[16]);
        assertThrows(IllegalStateException.class, () -> new JwtService(corto, 15));
    }
}