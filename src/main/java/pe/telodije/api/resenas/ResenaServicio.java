package pe.telodije.api.resenas;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "resena_servicios")
@IdClass(ResenaServicioId.class)
public class ResenaServicio {

    @Id
    @Column(name = "resena_id")
    private UUID resenaId;

    @Id
    @Column(name = "servicio_id")
    private short servicioId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_servicio")
    private EstadoServicio estado;

    protected ResenaServicio() { }

    public ResenaServicio(UUID resenaId, short servicioId, EstadoServicio estado) {
        this.resenaId = resenaId;
        this.servicioId = servicioId;
        this.estado = estado;
    }

    public UUID getResenaId() { return resenaId; }
    public short getServicioId() { return servicioId; }
    public EstadoServicio getEstado() { return estado; }
}