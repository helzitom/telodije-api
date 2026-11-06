package pe.telodije.api.multimedia;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fotos")
public class Foto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "alojamiento_id", nullable = false)
    private UUID alojamientoId;

    @Column(name = "visita_id")
    private UUID visitaId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "tipo_foto")
    private TipoFoto tipo = TipoFoto.LLEGADA;

    @Column(name = "clave_storage", nullable = false, unique = true)
    private String claveStorage;

    @Column(nullable = false, length = 64)
    private String sha256;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_foto")
    private EstadoFoto estado = EstadoFoto.PENDIENTE;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private Instant creadoEn;

    protected Foto() { }

    public Foto(UUID usuarioId, UUID alojamientoId, UUID visitaId, String claveStorage, String sha256) {
        this.usuarioId = usuarioId;
        this.alojamientoId = alojamientoId;
        this.visitaId = visitaId;
        this.claveStorage = claveStorage;
        this.sha256 = sha256;
    }

    public UUID getId() { return id; }
}