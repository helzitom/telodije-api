package pe.telodije.api.resenas;

import java.time.Instant;
import java.util.UUID;

public interface ResenaPublicaProyeccion {
    UUID getId();
    short getCalificacion();
    boolean getVolveria();
    String getComentario();
    Instant getFechaResena();
    String getRespuestaDueno();
}