package pe.telodije.api.alojamientos;

import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pe.telodije.api.busqueda.ConcurrenciaService;
import pe.telodije.api.busqueda.GeohashUtil;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/alojamientos")
@Validated
public class AlojamientoController {

    private final AlojamientoRepository repositorio;
    private final ConcurrenciaService concurrencia;

    public AlojamientoController(AlojamientoRepository repositorio,
                                 ConcurrenciaService concurrencia) {
        this.repositorio = repositorio;
        this.concurrencia = concurrencia;
    }

    @GetMapping("/cercanos")
    public ResponseEntity<List<BusquedaProyeccion>> cercanos(
            @RequestParam @DecimalMin("-90") @DecimalMax("90") double lat,
            @RequestParam @DecimalMin("-180") @DecimalMax("180") double lng,
            @RequestParam(defaultValue = "2000") @Min(100) @Max(10000) double radioMetros,
            @RequestParam(required = false) @Pattern(regexp = "H4|H6|H8|H12|NOCHE_COMPLETA") String franja,
            @RequestParam(required = false) @DecimalMin("0.01") BigDecimal precioMax,
            @RequestParam(required = false) @Size(max = 60) String tipoHabitacion,
            @RequestParam(defaultValue = "false") boolean requiereCochera,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limite) {

        // Registro anónimo de la búsqueda: solo zona aproximada, hora y filtros.
        String celda = GeohashUtil.codificar(lat, lng, 5);
        String filtrosJson = String.format(
                "{\"franja\":%s,\"requiereCochera\":%b}",
                franja == null ? "null" : "\"" + franja + "\"",
                requiereCochera);
        concurrencia.registrarBusqueda(celda, filtrosJson);

        return ResponseEntity.ok(repositorio.buscarCercanos(
                lat, lng, radioMetros, franja, precioMax,
                tipoHabitacion, requiereCochera, limite));
    }
}