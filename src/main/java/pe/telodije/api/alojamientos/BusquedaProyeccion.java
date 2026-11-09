package pe.telodije.api.alojamientos;

import java.math.BigDecimal;
import java.util.UUID;

public interface BusquedaProyeccion {
    UUID getId();
    String getNombre();
    String getTipo();
    boolean getTieneCochera();
    BigDecimal getCalificacionPromedio();
    int getTotalResenas();
    Double getDistanciaMetros();
    BigDecimal getPrecioDesde();
    Double getLatitud();
    Double getLongitud();

}