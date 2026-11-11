package pe.telodije.api.alojamientos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
public class VerificacionController {

    private final VerificacionDuenoService servicio;

    public VerificacionController(VerificacionDuenoService servicio) {
        this.servicio = servicio;
    }

    public record SolicitudRequest(
            @NotBlank String claveDocumento,
            @NotBlank String claveLocal) { }

    public record DecisionRequest(boolean aprobar) { }

    @PostMapping("/api/v1/alojamientos/{id}/verificacion")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> solicitar(@PathVariable UUID id,
                                         @Valid @RequestBody SolicitudRequest req,
                                         Authentication auth) {
        UUID solicitud = servicio.solicitar(UUID.fromString(auth.getName()), id,
                req.claveDocumento(), req.claveLocal());
        return Map.of("id", solicitud, "estado", "PENDIENTE");
    }

    @GetMapping("/api/v1/alojamientos/{id}/verificacion")
    public Map<String, Object> miEstado(@PathVariable UUID id, Authentication auth) {
        return servicio.estadoDeUsuario(UUID.fromString(auth.getName()), id);
    }

    @GetMapping("/api/v1/admin/verificaciones")
    public java.util.List<Map<String, Object>> pendientes() {
        return servicio.pendientes();
    }

    @PatchMapping("/api/v1/admin/verificaciones/{id}")
    public Map<String, String> decidir(@PathVariable UUID id,
                                       @RequestBody DecisionRequest req,
                                       Authentication auth) {
        servicio.decidir(UUID.fromString(auth.getName()), id, req.aprobar());
        return Map.of("estado", req.aprobar() ? "APROBADA" : "RECHAZADA");
    }

    @GetMapping("/api/v1/alojamientos/{id}/soy-dueno")
    public Map<String, Boolean> soyDueno(@PathVariable UUID id, Authentication auth) {
        return Map.of("dueno", servicio.esDuenoVerificado(UUID.fromString(auth.getName()), id));
    }
}