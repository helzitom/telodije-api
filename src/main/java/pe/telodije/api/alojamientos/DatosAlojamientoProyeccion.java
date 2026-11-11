package pe.telodije.api.alojamientos;

import java.math.BigDecimal;

public interface DatosAlojamientoProyeccion {
    String getNombre();
    String getTipo();
    String getDireccion();
    boolean getTieneCochera();
    BigDecimal getCalificacionPromedio();
    int getTotalResenas();
    boolean getVerificado();
}