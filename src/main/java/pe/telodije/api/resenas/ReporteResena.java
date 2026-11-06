package pe.telodije.api.resenas;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "reportes_resena")
public class ReporteResena {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "resena_id", nullable = false)
    private UUID resenaId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "varchar")
    private MotivoReporte motivo;

    @Column(length = 300)
    private String detalle;

    protected ReporteResena() { }

    public ReporteResena(UUID resenaId, UUID usuarioId, MotivoReporte motivo, String detalle) {
        this.resenaId = resenaId;
        this.usuarioId = usuarioId;
        this.motivo = motivo;
        this.detalle = detalle;
    }
}