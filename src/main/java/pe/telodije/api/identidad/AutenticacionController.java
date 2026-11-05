package pe.telodije.api.identidad;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.telodije.api.identidad.AuthDtos.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AutenticacionController {

    //Se inicializa la variable service, con el uso de la clase AutenticacionService, para poder crear los ENDPOINTS
    //a los cuales ingresarán las solicitudes HTTP
    private final AutenticacionService servicio;


    //Constructor del controlador de autenticación
    public AutenticacionController(AutenticacionService servicio) {
        this.servicio = servicio;
    }

    //Método que define un endpoint REST del tipo POST, el cual tiene como función registar a un nuevo usuario en el sistema
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroResponse registro(@Valid @RequestBody RegistroRequest req) {
        Usuario usuario = servicio.registrar(req);
        return new RegistroResponse(usuario.getId(), usuario.getAlias());
    }

    //Método que ddefine un endpoint REST del tipo POST, el cual tiene como función iniciar sesión en el sistema
    @PostMapping("/login")
    public TokensResponse login(@Valid @RequestBody LoginRequest req) {
        return servicio.login(req.identificador(), req.password());
    }

    //Método que define un endpoint REST de tipo POST, el solicita la renovación de los tokens
    @PostMapping("/refresh")
    public TokensResponse refresh(@Valid @RequestBody RefrescoRequest req) {
        return servicio.refrescar(req.refreshToken());
    }


    //Método que define el endpoint /logout, el cual tiene como funcion cerrar la sesión
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefrescoRequest req) {
        servicio.cerrarSesion(req.refreshToken());
    }
}