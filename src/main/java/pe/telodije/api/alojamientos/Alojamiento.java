package pe.telodije.api.alojamientos;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alojamientos")
public class Alojamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "tipo_alojamiento")
    private TipoAlojamiento tipo;

    @Column(nullable = false, columnDefinition = "geography(Point,4326)")
    private Point ubicacion;

    @Column(nullable = false, length = 255)
    private String direccion;

    @Column(name = "tiene_cochera", nullable = false)
    private boolean tieneCochera;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "estado_publicacion", nullable = false, columnDefinition = "estado_publicacion")
    private EstadoPublicacion estadoPublicacion = EstadoPublicacion.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "estado_operativo", nullable = false, columnDefinition = "estado_operativo")
    private EstadoOperativo estadoOperativo = EstadoOperativo.ABIERTO;

    @Column(name = "calificacion_promedio", precision = 2, scale = 1)
    private BigDecimal calificacionPromedio;

    @Column(name = "total_resenas", nullable = false)
    private int totalResenas;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", insertable = false, updatable = false)
    private Instant actualizadoEn;

    protected Alojamiento() { }

    public Alojamiento(String nombre, TipoAlojamiento tipo, Point ubicacion,
                       String direccion, boolean tieneCochera) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.ubicacion = ubicacion;
        this.direccion = direccion;
        this.tieneCochera = tieneCochera;
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public TipoAlojamiento getTipo() { return tipo; }
    public Point getUbicacion() { return ubicacion; }
    public String getDireccion() { return direccion; }
    public boolean isTieneCochera() { return tieneCochera; }
    public EstadoPublicacion getEstadoPublicacion() { return estadoPublicacion; }
    public EstadoOperativo getEstadoOperativo() { return estadoOperativo; }
    public BigDecimal getCalificacionPromedio() { return calificacionPromedio; }
    public int getTotalResenas() { return totalResenas; }
}