package pe.telodije.api.identidad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final String EMISOR = "telodije-api";

    private final SecretKey clave;
    private final Duration duracionAcceso;

    //Método que permite generar el JWT para la sesión , crea clave y duración (en minutos) del JWT
    public JwtService(@Value("${jwt.secreto}") String secretoBase64,
                      @Value("${jwt.expiracion-acceso-minutos}") long minutosAcceso) {
        byte[] bytes = Decoders.BASE64.decode(secretoBase64);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRETO debe tener al menos 256 bits");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.duracionAcceso = Duration.ofMinutes(minutosAcceso);
    }

    //Método que proporcionada el acceso al usuario, segun su rol y su ID (con su respectiva duración)
    public String emitirAcceso(UUID usuarioId, RolUsuario rol) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .issuer(EMISOR)
                .subject(usuarioId.toString())
                .claim("rol", rol.name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracionAcceso)))
                .signWith(clave)
                .compact();
    }

    //Método que valida el token brindaddo por el usuario
    public Claims validar(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .requireIssuer(EMISOR)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    //Método que obtiene la duración del acceso pero en segundos
    public long segundosAcceso() {
        return duracionAcceso.getSeconds();
    }
}