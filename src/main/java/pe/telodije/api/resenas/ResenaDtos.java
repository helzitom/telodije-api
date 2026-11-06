package pe.telodije.api.resenas;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ResenaDtos {

    private ResenaDtos() { }

    public record ServicioEstado(
            @NotNull Short servicioId,
            @NotNull EstadoServicio estado) { }

    public record CrearResenaRequest(
            @NotNull UUID visitaId,
            @NotNull @Min(1) @Max(5) Short calificacion,
            @NotNull Boolean volveria,
            @Size(max = 1000) String comentario,
            @Valid List<ServicioEstado> servicios) { }

    public record ResenaRespuesta(
            UUID id,
            UUID alojamientoId,
            short calificacion,
            boolean volveria,
            String comentario,
            String autor,
            Instant fechaResena,
            String estado) { }
}