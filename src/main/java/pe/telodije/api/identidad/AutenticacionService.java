package pe.telodije.api.identidad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.telodije.api.identidad.AuthDtos.RegistroRequest;
import pe.telodije.api.identidad.AuthDtos.TokensResponse;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class AutenticacionService {

    private static final String CREDENCIALES_INVALIDAS = "Usuario o contraseña incorrectos";
    private static final String SESION_INVALIDA = "Sesión no válida o expirada";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarios;
    private final SesionRepository sesiones;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final String hashFicticio;
    private final int intentosMaximos;
    private final Duration duracionBloqueo;
    private final Duration duracionRefresh;

    public AutenticacionService(UsuarioRepository usuarios,
                                SesionRepository sesiones,
                                PasswordEncoder encoder,
                                JwtService jwt,
                                @Value("${seguridad.intentos-maximos}") int intentosMaximos,
                                @Value("${seguridad.minutos-bloqueo}") long minutosBloqueo,
                                @Value("${jwt.expiracion-refresh-dias}") long diasRefresh) {
        this.usuarios = usuarios;
        this.sesiones = sesiones;
        this.encoder = encoder;
        this.jwt = jwt;
        this.intentosMaximos = intentosMaximos;
        this.duracionBloqueo = Duration.ofMinutes(minutosBloqueo);
        this.duracionRefresh = Duration.ofDays(diasRefresh);
        // Hash precalculado para igualar el tiempo de respuesta cuando el usuario no existe
        this.hashFicticio = encoder.encode(generarTokenOpaco());
    }

    @Transactional
    public Usuario registrar(RegistroRequest req) {
        String alias = normalizar(req.alias());
        String email = normalizar(req.email());

        if (usuarios.existsByAlias(alias) || usuarios.existsByEmail(email)) {
            throw new ErrorNegocio(HttpStatus.CONFLICT, "El alias o el correo ya están registrados");
        }
        return usuarios.save(new Usuario(alias, email, encoder.encode(req.password())));
    }

    // noRollbackFor: el contador de intentos fallidos debe persistir aunque la respuesta sea error
    @Transactional(noRollbackFor = ErrorNegocio.class)
    public TokensResponse login(String identificador, String password) {
        String clave = normalizar(identificador);
        Instant ahora = Instant.now();

        Usuario usuario = usuarios.findByAliasOrEmail(clave, clave).orElse(null);

        if (usuario == null) {
            encoder.matches(password, hashFicticio);
            throw new ErrorNegocio(HttpStatus.UNAUTHORIZED, CREDENCIALES_INVALIDAS);
        }
        if (usuario.estaBloqueado(ahora)) {
            throw new ErrorNegocio(HttpStatus.LOCKED,
                    "Cuenta bloqueada temporalmente, intenta más tarde");
        }
        if (!encoder.matches(password, usuario.getPasswordHash())) {
            usuario.registrarFallo(ahora, intentosMaximos, duracionBloqueo);
            throw new ErrorNegocio(HttpStatus.UNAUTHORIZED, CREDENCIALES_INVALIDAS);
        }
        if (!usuario.isActivo()) {
            throw new ErrorNegocio(HttpStatus.UNAUTHORIZED, CREDENCIALES_INVALIDAS);
        }

        usuario.registrarExito();
        return emitirTokens(usuario, ahora);
    }

    // Rotación: cada refresh revoca el anterior y emite uno nuevo.
    // Si se reutiliza un token ya revocado, se asume robo y se cierran todas las sesiones.
    @Transactional(noRollbackFor = ErrorNegocio.class)
    public TokensResponse refrescar(String refreshToken) {
        Instant ahora = Instant.now();

        SesionUsuario sesion = sesiones.findByTokenHash(hashToken(refreshToken))
                .orElseThrow(() -> new ErrorNegocio(HttpStatus.UNAUTHORIZED, SESION_INVALIDA));

        if (sesion.getRevocadaEn() != null) {
            sesiones.revocarTodasDeUsuario(sesion.getUsuarioId(), ahora);
            throw new ErrorNegocio(HttpStatus.UNAUTHORIZED, SESION_INVALIDA);
        }
        if (sesion.estaExpirada(ahora)) {
            throw new ErrorNegocio(HttpStatus.UNAUTHORIZED, SESION_INVALIDA);
        }

        Usuario usuario = usuarios.findById(sesion.getUsuarioId())
                .filter(Usuario::isActivo)
                .orElseThrow(() -> new ErrorNegocio(HttpStatus.UNAUTHORIZED, SESION_INVALIDA));

        sesion.revocar(ahora);
        return emitirTokens(usuario, ahora);
    }

    //Método que busca la sesión mediante el hash del refresh token, si es que existe, se revoca con la fecha actual
    @Transactional
    public void cerrarSesion(String refreshToken) {
        sesiones.findByTokenHash(hashToken(refreshToken))
                .ifPresent(s -> s.revocar(Instant.now()));
    }

    //Método para generar un refresh token aleatoriamente,
    // se guarda en la base de datos, genera un JWT, y devuelve ambos al cliente
    private TokensResponse emitirTokens(Usuario usuario, Instant ahora) {
        String refresh = generarTokenOpaco();
        sesiones.save(new SesionUsuario(usuario.getId(), hashToken(refresh), ahora.plus(duracionRefresh)));
        String acceso = jwt.emitirAcceso(usuario.getId(), usuario.getRol());
        return new TokensResponse(acceso, refresh, "Bearer", jwt.segundosAcceso());
    }

    //Método par eliminar espacios al incio y final de texto (también los convierte a minúsculas)
    private static String normalizar(String valor) {
        return valor.trim().toLowerCase(Locale.ROOT);
    }

    //Método para generar un token aleatorio de 32 bytes,
    // se convierte a base65 y se usa como refresh token

    private static String generarTokenOpaco() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // SHA-256 en hexadecimal: devuelve su hash en exadecimal para almacenar y comparar el refresh token
    // sin guardar el valor original
    static String hashToken(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}