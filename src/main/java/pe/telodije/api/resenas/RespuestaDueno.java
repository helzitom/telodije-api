package pe.telodije.api.resenas;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "respuestas_dueno")
public class RespuestaDueno {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "resena_id", nullable = false, unique = true)
    private UUID resenaId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false, length = 1000)
    private String texto;

    protected RespuestaDueno() { }

    public RespuestaDueno(UUID resenaId, UUID usuarioId, String texto) {
        this.resenaId = resenaId;
        this.usuarioId = usuarioId;
        this.texto = texto;
    }
}