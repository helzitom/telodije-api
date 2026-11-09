package pe.telodije.api.alojamientos;

import java.math.BigDecimal;

public interface TarifaProyeccion {
    String getFranja();
    BigDecimal getPrecioPen();
    String getTipoHabitacion();
    Short getConfianza();
}