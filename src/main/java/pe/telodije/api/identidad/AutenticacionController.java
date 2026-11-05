package pe.telodije.api.identidad;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.telodije.api.identidad.AuthDtos.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AutenticacionController {

    private final AutenticacionService servicio;

    public AutenticacionController(AutenticacionService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroResponse registro(@Valid @RequestBody RegistroRequest req) {
        Usuario usuario = servicio.registrar(req);
        return new RegistroResponse(usuario.getId(), usuario.getAlias());
    }

    @PostMapping("/login")
    public TokensResponse login(@Valid @RequestBody LoginRequest req) {
        return servicio.login(req.identificador(), req.password());
    }

    @PostMapping("/refresh")
    public TokensResponse refresh(@Valid @RequestBody RefrescoRequest req) {
        return servicio.refrescar(req.refreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefrescoRequest req) {
        servicio.cerrarSesion(req.refreshToken());
    }
}