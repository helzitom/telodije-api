package pe.telodije.api.alojamientos;

import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alojamientos/{id}")
public class DetalleAlojamientoController {

    private final DetalleAlojamientoService servicio;

    public DetalleAlojamientoController(DetalleAlojamientoService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/detalle")
    public Map<String, Object> detalle(@PathVariable UUID id) {
        return servicio.obtener(id);
    }
}