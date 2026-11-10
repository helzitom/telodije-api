package pe.telodije.api.busqueda;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/concurrencia")
public class ConcurrenciaController {

    private final ConcurrenciaService servicio;

    public ConcurrenciaController(ConcurrenciaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public Map<String, String> nivel(@RequestParam String geohash) {
        if (!geohash.matches("^[0-9b-hjkmnp-z]{4,6}$")) {
            return Map.of("nivel", "SIN_DATOS");
        }
        return Map.of("nivel", servicio.nivelDemanda(geohash));
    }
}