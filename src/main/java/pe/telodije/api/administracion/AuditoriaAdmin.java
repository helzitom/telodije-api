package pe.telodije.api.administracion;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auditoria_admin")
public class AuditoriaAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id")
    private UUID adminId;

    @Column(nullable = false, length = 60)
    private String accion;

    @Column(nullable = false, length = 40)
    private String entidad;

    @Column(name = "entidad_id")
    private UUID entidadId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String detalle = "{}";

    @Column(name = "creado_en", insertable = false, updatable = false)
    private Instant creadoEn;

    protected AuditoriaAdmin() { }

    public AuditoriaAdmin(UUID adminId, String accion, String entidad, UUID entidadId, String detalle) {
        this.adminId = adminId;
        this.accion = accion;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.detalle = detalle;
    }
}