package pe.telodije.api.visitas;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "visitas")
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "alojamiento_id", nullable = false)
    private UUID alojamientoId;

    @Column(name = "tipo_habitacion_id")
    private UUID tipoHabitacionId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "franja_estadia")
    private FranjaEstadia franja;

    @Column(nullable = false)
    private short horas;

    @Column(name = "monto_pagado_pen", nullable = false, precision = 8, scale = 2)
    private BigDecimal montoPagadoPen;

    @Column(name = "llegada_en", nullable = false)
    private Instant llegadaEn;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private Instant creadoEn;

    protected Visita() { }

    public Visita(UUID usuarioId, UUID alojamientoId, UUID tipoHabitacionId,
                  FranjaEstadia franja, int horas, BigDecimal montoPagadoPen, Instant llegadaEn) {
        this.usuarioId = usuarioId;
        this.alojamientoId = alojamientoId;
        this.tipoHabitacionId = tipoHabitacionId;
        this.franja = franja;
        this.horas = (short) horas;
        this.montoPagadoPen = montoPagadoPen;
        this.llegadaEn = llegadaEn;
    }

    public UUID getId() { return id; }
    public UUID getAlojamientoId() { return alojamientoId; }
    public FranjaEstadia getFranja() { return franja; }
    public short getHoras() { return horas; }
    public BigDecimal getMontoPagadoPen() { return montoPagadoPen; }
    public Instant getLlegadaEn() { return llegadaEn; }
    public UUID getUsuarioId() { return usuarioId; }
}