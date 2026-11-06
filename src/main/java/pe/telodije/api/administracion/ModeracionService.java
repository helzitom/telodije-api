package pe.telodije.api.administracion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.telodije.api.alojamientos.EstadoPublicacion;
import pe.telodije.api.identidad.ErrorNegocio;
import pe.telodije.api.resenas.Resena;
import pe.telodije.api.resenas.ResenaRepository;

import java.util.UUID;

@Service
public class ModeracionService {

    private final ResenaRepository resenas;
    private final AuditoriaRepository auditoria;

    public ModeracionService(ResenaRepository resenas, AuditoriaRepository auditoria) {
        this.resenas = resenas;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public Page<Resena> colaPendiente(int pagina, int tamano) {
        int tamanoSeguro = Math.min(Math.max(tamano, 1), 50);
        return resenas.findByEstadoPublicacionOrderByIdAsc(
                EstadoPublicacion.EN_REVISION, PageRequest.of(Math.max(pagina, 0), tamanoSeguro));
    }

    @Transactional
    public Resena decidir(UUID adminId, UUID resenaId, Accion accion) {
        Resena resena = resenas.findById(resenaId)
                .orElseThrow(() -> new ErrorNegocio(HttpStatus.NOT_FOUND, "Reseña no encontrada"));

        if (resena.getEstadoPublicacion() != EstadoPublicacion.EN_REVISION) {
            throw new ErrorNegocio(HttpStatus.CONFLICT, "La reseña ya fue revisada");
        }

        EstadoPublicacion nuevo = switch (accion) {
            case APROBAR -> EstadoPublicacion.APROBADO;
            case RECHAZAR -> EstadoPublicacion.RECHAZADO;
        };
        resena.setEstadoPublicacion(nuevo);

        auditoria.save(new AuditoriaAdmin(
                adminId, accion.name(), "RESENA", resenaId,
                "{\"estadoNuevo\":\"" + nuevo.name() + "\"}"));

        return resena;
    }

    public enum Accion { APROBAR, RECHAZAR }
}