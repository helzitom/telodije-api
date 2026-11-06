package pe.telodije.api.resenas;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alojamientos/{id}/resenas")
public class ResenaPublicaController {

    private final ResenaService servicio;

    public ResenaPublicaController(ResenaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<ResenaPublicaProyeccion> listar(@PathVariable UUID id,
                                                @RequestParam(defaultValue = "0") int pagina,
                                                @RequestParam(defaultValue = "20") int tamano) {
        return servicio.listarPublicas(id, pagina, tamano);
    }
}