package pe.telodije.api.resenas;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.telodije.api.resenas.ResenaDtos.CrearResenaRequest;
import pe.telodije.api.resenas.ResenaDtos.ResenaRespuesta;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resenas")
public class ResenaController {

    private final ResenaService servicio;

    public ResenaController(ResenaService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResenaRespuesta crear(@Valid @RequestBody CrearResenaRequest req,
                                 Authentication auth) {
        return servicio.crear(UUID.fromString(auth.getName()), req);
    }
}