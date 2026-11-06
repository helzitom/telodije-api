package pe.telodije.api.resenas;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ResenaServicioId implements Serializable {

    private UUID resenaId;
    private short servicioId;

    protected ResenaServicioId() { }

    public ResenaServicioId(UUID resenaId, short servicioId) {
        this.resenaId = resenaId;
        this.servicioId = servicioId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResenaServicioId that)) return false;
        return servicioId == that.servicioId && Objects.equals(resenaId, that.resenaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resenaId, servicioId);
    }
}