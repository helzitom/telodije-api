package pe.telodije.api.visitas;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.telodije.api.alojamientos.AlojamientoRepository;
import pe.telodije.api.identidad.ErrorNegocio;
import pe.telodije.api.multimedia.AlmacenFotos;
import pe.telodije.api.multimedia.Foto;
import pe.telodije.api.multimedia.FotoRepository;
import pe.telodije.api.visitas.VisitaDtos.RegistroVisitaRequest;
import pe.telodije.api.visitas.VisitaDtos.VisitaRespuesta;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class VisitaService {

    private static final Duration ANTIGUEDAD_MAXIMA = Duration.ofHours(24);
    private static final Duration TOLERANCIA_FUTURO = Duration.ofMinutes(5);

    private final VisitaRepository visitas;
    private final FotoRepository fotos;
    private final AlojamientoRepository alojamientos;
    private final AlmacenFotos almacen;

    public VisitaService(VisitaRepository visitas, FotoRepository fotos,
                         AlojamientoRepository alojamientos, AlmacenFotos almacen) {
        this.visitas = visitas;
        this.fotos = fotos;
        this.alojamientos = alojamientos;
        this.almacen = almacen;
    }

    @Transactional
    public VisitaRespuesta registrar(UUID usuarioId, RegistroVisitaRequest req) {
        Instant ahora = Instant.now();
        validarLlegada(req.llegadaEn(), ahora);
        validarHoras(req.franja(), req.horas());

        if (!alojamientos.estaAprobado(req.alojamientoId())) {
            throw new ErrorNegocio(HttpStatus.NOT_FOUND, "Hospedaje no encontrado");
        }
        if (req.tipoHabitacionId() != null
                && !alojamientos.tipoPerteneceA(req.tipoHabitacionId(), req.alojamientoId())) {
            throw new ErrorNegocio(HttpStatus.BAD_REQUEST,
                    "El tipo de habitación no corresponde a este hospedaje");
        }

        String clave = req.claveFoto();
        if (!clave.startsWith(AlmacenFotos.prefijoLlegada(usuarioId.toString()))) {
            throw new ErrorNegocio(HttpStatus.FORBIDDEN, "La foto no pertenece a tu cuenta");
        }
        if (!almacen.existe(clave)) {
            throw new ErrorNegocio(HttpStatus.BAD_REQUEST, "La foto de llegada no existe, súbela primero");
        }

        String sha256 = almacen.sha256(clave);
        if (fotos.existsBySha256(sha256)) {
            throw new ErrorNegocio(HttpStatus.CONFLICT, "Esta foto ya fue utilizada en otra visita");
        }

        // saveAndFlush: la visita debe existir antes de la foto por la clave foránea compuesta
        Visita visita = visitas.saveAndFlush(new Visita(
                usuarioId, req.alojamientoId(), req.tipoHabitacionId(),
                req.franja(), req.horas(), req.montoPagadoPen(), req.llegadaEn()));

        // El trigger diferido trg_visita_con_foto valida en el COMMIT que esta foto exista
        fotos.save(new Foto(usuarioId, req.alojamientoId(), visita.getId(), clave, sha256));

        return VisitaRespuesta.desde(visita);
    }

    @Transactional(readOnly = true)
    public List<VisitaRespuesta> historialDe(UUID usuarioId) {
        return visitas.findByUsuarioIdOrderByLlegadaEnDesc(usuarioId)
                .stream()
                .map(VisitaRespuesta::desde)
                .toList();
    }

    private static void validarLlegada(Instant llegada, Instant ahora) {
        if (llegada.isAfter(ahora.plus(TOLERANCIA_FUTURO))) {
            throw new ErrorNegocio(HttpStatus.BAD_REQUEST, "La hora de llegada no puede estar en el futuro");
        }
        if (llegada.isBefore(ahora.minus(ANTIGUEDAD_MAXIMA))) {
            throw new ErrorNegocio(HttpStatus.BAD_REQUEST,
                    "La visita debe registrarse dentro de las últimas 24 horas");
        }
    }

    private static void validarHoras(FranjaEstadia franja, int horas) {
        Integer esperadas = franja.getHorasFijas();
        if (esperadas != null && esperadas != horas) {
            throw new ErrorNegocio(HttpStatus.BAD_REQUEST,
                    "Las horas no coinciden con la franja " + franja);
        }
    }
}