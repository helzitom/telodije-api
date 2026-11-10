package pe.telodije.api.alojamientos;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.telodije.api.identidad.ErrorNegocio;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class DetalleAlojamientoService {

    private final AlojamientoRepository alojamientos;
    private final DetalleAlojamientoRepository detalle;

    public DetalleAlojamientoService(AlojamientoRepository alojamientos,
                                     DetalleAlojamientoRepository detalle) {
        this.alojamientos = alojamientos;
        this.detalle = detalle;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> obtener(UUID id) {
        if (!alojamientos.estaAprobado(id)) {
            throw new ErrorNegocio(HttpStatus.NOT_FOUND, "Hospedaje no encontrado");
        }
        DatosAlojamientoProyeccion datos = detalle.datos(id);
        List<TarifaProyeccion> tarifas = detalle.tarifasVigentes(id);
        List<ServicioProyeccion> servicios = detalle.servicios(id);

        Map<String, Object> respuesta = new java.util.HashMap<>();
        respuesta.put("id", id);
        respuesta.put("nombre", datos.getNombre());
        respuesta.put("tipo", datos.getTipo());
        respuesta.put("direccion", datos.getDireccion());
        respuesta.put("tieneCochera", datos.getTieneCochera());
        respuesta.put("calificacionPromedio", datos.getCalificacionPromedio());
        respuesta.put("totalResenas", datos.getTotalResenas());
        respuesta.put("tarifas", tarifas);
        respuesta.put("servicios", servicios);
        return respuesta;
    }
}