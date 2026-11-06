package pe.telodije.api.administracion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.telodije.api.resenas.Resena;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final ModeracionService moderacion;

    public AdminController(ModeracionService moderacion) {
        this.moderacion = moderacion;
    }

    public record DecisionRequest(@NotNull ModeracionService.Accion accion) { }

    @GetMapping("/resenas/pendientes")
    public Map<String, Object> pendientes(@RequestParam(defaultValue = "0") int pagina,
                                          @RequestParam(defaultValue = "20") int tamano) {
        Page<Resena> pagina_ = moderacion.colaPendiente(pagina, tamano);
        return Map.of(
                "total", pagina_.getTotalElements(),
                "contenido", pagina_.getContent().stream().map(r -> Map.of(
                        "id", r.getId(),
                        "alojamientoId", r.getAlojamientoId(),
                        "calificacion", r.getCalificacion(),
                        "comentario", r.getComentario() == null ? "" : r.getComentario(),
                        "usuarioId", r.getUsuarioId())).toList());
    }

    @PatchMapping("/resenas/{id}")
    public Map<String, Object> decidir(@PathVariable UUID id,
                                       @Valid @RequestBody DecisionRequest req,
                                       Authentication auth) {
        Resena r = moderacion.decidir(UUID.fromString(auth.getName()), id, req.accion());
        return Map.of("id", r.getId(), "estado", r.getEstadoPublicacion().name());
    }
}