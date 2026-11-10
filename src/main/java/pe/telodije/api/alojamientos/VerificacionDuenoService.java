package pe.telodije.api.alojamientos;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.telodije.api.identidad.ErrorNegocio;
import pe.telodije.api.multimedia.AlmacenFotos;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class VerificacionDuenoService {

    private final JdbcTemplate jdbc;
    private final AlmacenFotos almacen;

    public VerificacionDuenoService(JdbcTemplate jdbc, AlmacenFotos almacen) {
        this.jdbc = jdbc;
        this.almacen = almacen;
    }

    @Transactional
    public UUID solicitar(UUID usuarioId, UUID alojamientoId, String claveDocumento, String claveLocal) {
        Integer existe = jdbc.queryForObject(
                "SELECT COUNT(*) FROM alojamientos WHERE id = ? AND estado_publicacion = 'APROBADO'",
                Integer.class, alojamientoId);
        if (existe == null || existe == 0) {
            throw new ErrorNegocio(HttpStatus.NOT_FOUND, "Hospedaje no encontrado");
        }

        // No se puede tener dos solicitudes pendientes ni volver a pedir una aprobada.
        Integer activa = jdbc.queryForObject(
                "SELECT COUNT(*) FROM verificaciones_dueno WHERE alojamiento_id = ? " +
                        "AND estado IN ('PENDIENTE', 'APROBADA')",
                Integer.class, alojamientoId);
        if (activa != null && activa > 0) {
            throw new ErrorNegocio(HttpStatus.CONFLICT, "Este hospedaje ya tiene una verificación en curso o aprobada");
        }

        // Las dos claves deben existir en el almacén antes de registrar la solicitud.
        if (!almacen.existe(claveDocumento) || !almacen.existe(claveLocal)) {
            throw new ErrorNegocio(HttpStatus.BAD_REQUEST, "Las fotos no se subieron correctamente");
        }

        return jdbc.queryForObject(
                "INSERT INTO verificaciones_dueno (alojamiento_id, usuario_id, documento_clave, foto_local_clave) " +
                        "VALUES (?, ?, ?, ?) RETURNING id",
                UUID.class, alojamientoId, usuarioId, claveDocumento, claveLocal);
    }

    @Transactional
    public void decidir(UUID adminId, UUID verificacionId, boolean aprobar) {
        int filas = jdbc.update(
                "UPDATE verificaciones_dueno SET estado = ?, revisado_por = ?, revisado_en = now() " +
                        "WHERE id = ? AND estado = 'PENDIENTE'",
                aprobar ? "APROBADA" : "RECHAZADA", adminId, verificacionId);
        if (filas == 0) {
            throw new ErrorNegocio(HttpStatus.CONFLICT, "La solicitud no existe o ya fue revisada");
        }
        jdbc.update(
                "INSERT INTO auditoria_admin (admin_id, accion, entidad, entidad_id, detalle) " +
                        "VALUES (?, ?, 'VERIFICACION_DUENO', ?, '{}'::jsonb)",
                adminId, aprobar ? "APROBAR_VERIFICACION" : "RECHAZAR_VERIFICACION", verificacionId);
    }

    public List<Map<String, Object>> pendientes() {
        return jdbc.queryForList(
                "SELECT id, alojamiento_id, usuario_id, creado_en FROM verificaciones_dueno " +
                        "WHERE estado = 'PENDIENTE' ORDER BY creado_en");
    }

    public Map<String, Object> estadoDeUsuario(UUID usuarioId, UUID alojamientoId) {
        List<Map<String, Object>> filas = jdbc.queryForList(
                "SELECT estado FROM verificaciones_dueno WHERE usuario_id = ? AND alojamiento_id = ? " +
                        "ORDER BY creado_en DESC LIMIT 1",
                usuarioId, alojamientoId);
        return filas.isEmpty() ? Map.of("estado", "NINGUNA") : filas.get(0);
    }

    public boolean esDuenoVerificado(UUID usuarioId, UUID alojamientoId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM verificaciones_dueno " +
                        "WHERE usuario_id = ? AND alojamiento_id = ? AND estado = 'APROBADA'",
                Integer.class, usuarioId, alojamientoId);
        return n != null && n > 0;
    }
}