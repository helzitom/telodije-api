package pe.telodije.api.identidad;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sesiones")
public class SesionUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(name = "expira_en", nullable = false)
    private Instant expiraEn;

    @Column(name = "revocada_en")
    private Instant revocadaEn;

    protected SesionUsuario() { }

    public SesionUsuario(UUID usuarioId, String tokenHash, Instant expiraEn) {
        this.usuarioId = usuarioId;
        this.tokenHash = tokenHash;
        this.expiraEn = expiraEn;
    }

    public boolean estaExpirada(Instant ahora) {
        return expiraEn.isBefore(ahora);
    }

    public void revocar(Instant ahora) {
        if (revocadaEn == null) {
            revocadaEn = ahora;
        }
    }

    public UUID getUsuarioId() { return usuarioId; }
    public Instant getRevocadaEn() { return revocadaEn; }
}