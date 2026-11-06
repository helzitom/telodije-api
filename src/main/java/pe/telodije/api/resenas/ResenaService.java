package pe.telodije.api.resenas;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.telodije.api.alojamientos.EstadoPublicacion;
import pe.telodije.api.identidad.ErrorNegocio;
import pe.telodije.api.resenas.ResenaDtos.CrearResenaRequest;
import pe.telodije.api.resenas.ResenaDtos.ResenaRespuesta;
import pe.telodije.api.resenas.ResenaDtos.ServicioEstado;
import pe.telodije.api.visitas.Visita;
import pe.telodije.api.visitas.VisitaRepository;

import java.util.List;
import java.util.UUID;

@Service
public class ResenaService {

    private final ResenaRepository resenas;
    private final ResenaServicioRepository servicios;
    private final VisitaRepository visitas;
    private final FiltroModeracion filtro;

    public ResenaService(ResenaRepository resenas, ResenaServicioRepository servicios,
                         VisitaRepository visitas, FiltroModeracion filtro) {
        this.resenas = resenas;
        this.servicios = servicios;
        this.visitas = visitas;
        this.filtro = filtro;
    }

    @Transactional
    public ResenaRespuesta crear(UUID usuarioId, CrearResenaRequest req) {
        Visita visita = visitas.findById(req.visitaId())
                .orElseThrow(() -> new ErrorNegocio(HttpStatus.NOT_FOUND, "Visita no encontrada"));

        // Solo quien estuvo puede opinar, y solo sobre esa visita
        if (!visita.getUsuarioId().equals(usuarioId)) {
            throw new ErrorNegocio(HttpStatus.FORBIDDEN, "La visita no te pertenece");
        }
        if (resenas.existsByVisitaId(req.visitaId())) {
            throw new ErrorNegocio(HttpStatus.CONFLICT, "Ya calificaste esta visita");
        }
        if (resenas.existsByUsuarioIdAndAlojamientoId(usuarioId, visita.getAlojamientoId())) {
            throw new ErrorNegocio(HttpStatus.CONFLICT, "Ya calificaste este hospedaje");
        }

        EstadoPublicacion estado = filtro.requiereRevision(req.comentario())
                ? EstadoPublicacion.EN_REVISION
                : EstadoPublicacion.APROBADO;

        Resena resena = resenas.saveAndFlush(new Resena(
                req.visitaId(), usuarioId, visita.getAlojamientoId(),
                req.calificacion(), req.volveria(), limpiar(req.comentario()), estado));

        guardarServicios(resena.getId(), req.servicios());

        return new ResenaRespuesta(
                resena.getId(), resena.getAlojamientoId(), resena.getCalificacion(),
                resena.isVolveria(), resena.getComentario(), "Anónimo",
                null, resena.getEstadoPublicacion().name());
    }

    private void guardarServicios(UUID resenaId, List<ServicioEstado> lista) {
        if (lista == null) {
            return;
        }
        for (ServicioEstado s : lista) {
            servicios.save(new ResenaServicio(resenaId, s.servicioId(), s.estado()));
        }
    }

    private static String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    @Transactional(readOnly = true)
    public List<ResenaPublicaProyeccion> listarPublicas(UUID alojamientoId, int pagina, int tamano) {
        int limite = Math.min(Math.max(tamano, 1), 50);
        int desplazamiento = Math.max(pagina, 0) * limite;
        return resenas.listarPublicas(alojamientoId, limite, desplazamiento);
    }
}