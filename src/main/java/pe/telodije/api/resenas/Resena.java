package pe.telodije.api.resenas;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pe.telodije.api.alojamientos.EstadoPublicacion;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "resenas")
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "visita_id", nullable = false, unique = true)
    private UUID visitaId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "alojamiento_id", nullable = false)
    private UUID alojamientoId;

    @Column(nullable = false)
    private short calificacion;

    @Column(nullable = false)
    private boolean volveria;

    @Column(length = 1000)
    private String comentario;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "estado_publicacion", nullable = false, columnDefinition = "estado_publicacion")
    private EstadoPublicacion estadoPublicacion = EstadoPublicacion.APROBADO;

    @Column(name = "fecha_resena", insertable = false, updatable = false)
    private Instant fechaResena;

    @PostLoad
    void cargarFecha() { }

    protected Resena() { }

    public Resena(UUID visitaId, UUID usuarioId, UUID alojamientoId,
                  short calificacion, boolean volveria, String comentario,
                  EstadoPublicacion estado) {
        this.visitaId = visitaId;
        this.usuarioId = usuarioId;
        this.alojamientoId = alojamientoId;
        this.calificacion = calificacion;
        this.volveria = volveria;
        this.comentario = comentario;
        this.estadoPublicacion = estado;
    }

    public UUID getId() { return id; }
    public UUID getVisitaId() { return visitaId; }
    public UUID getUsuarioId() { return usuarioId; }
    public UUID getAlojamientoId() { return alojamientoId; }
    public short getCalificacion() { return calificacion; }
    public boolean isVolveria() { return volveria; }
    public String getComentario() { return comentario; }
    public EstadoPublicacion getEstadoPublicacion() { return estadoPublicacion; }
    public void setEstadoPublicacion(EstadoPublicacion estado) { this.estadoPublicacion = estado; }
}