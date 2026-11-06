package pe.telodije.api.visitas;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class VisitaDtos {

    private VisitaDtos() { }

    public record RegistroVisitaRequest(
            @NotNull UUID alojamientoId,
            UUID tipoHabitacionId,
            @NotNull FranjaEstadia franja,
            @NotNull @Min(1) @Max(72) Integer horas,
            @NotNull @DecimalMin("0.01") @Digits(integer = 6, fraction = 2) BigDecimal montoPagadoPen,
            @NotNull Instant llegadaEn,
            @NotBlank @Size(max = 120) String claveFoto) { }

    public record VisitaRespuesta(
            UUID id,
            UUID alojamientoId,
            FranjaEstadia franja,
            int horas,
            BigDecimal montoPagadoPen,
            Instant llegadaEn) {

        public static VisitaRespuesta desde(Visita v) {
            return new VisitaRespuesta(v.getId(), v.getAlojamientoId(), v.getFranja(),
                    v.getHoras(), v.getMontoPagadoPen(), v.getLlegadaEn());
        }
    }
}