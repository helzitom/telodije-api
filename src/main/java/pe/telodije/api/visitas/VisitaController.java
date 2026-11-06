package pe.telodije.api.visitas;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.telodije.api.visitas.VisitaDtos.RegistroVisitaRequest;
import pe.telodije.api.visitas.VisitaDtos.VisitaRespuesta;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/visitas")
public class VisitaController {

    private final VisitaService servicio;

    public VisitaController(VisitaService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VisitaRespuesta registrar(@Valid @RequestBody RegistroVisitaRequest req,
                                     Authentication auth) {
        return servicio.registrar(UUID.fromString(auth.getName()), req);
    }

    @GetMapping("/mias")
    public List<VisitaRespuesta> mias(Authentication auth) {
        return servicio.historialDe(UUID.fromString(auth.getName()));
    }
}