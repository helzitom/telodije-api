package pe.telodije.api.identidad;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name= "usuarios")
public class Usuario {

    //Columna con anotacion única @Id, y el @GeneratedValue para que se genere automaticamente, de tipo UUID
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    //El alias del usuario, con una logintud mmáximma de 30 caracteres
    @Column(nullable = false, length = 30, columnDefinition = "citext")
    private String alias;

    //Email del usuario con un valor máximo de 254 caracteres
    @Column(nullable = false, length = 254, columnDefinition = "citext")
    private String email;

    //Contraseña del usuario encriptada (hasheada) en la base de datos
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // Rol del usuario con uso de los enums
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "rol_usuario")
    private RolUsuario rol = RolUsuario.USUARIO;

    //Columna de intentos fallidos almacenados en la bdd
    @Column(name = "intentos_fallidos", nullable = false)
    private short intentosFallidos;

    //Columna con la fecha del desbloqueo
    @Column(name = "bloqueado_hasta")
    private Instant bloqueadoHasta;

    //Columna con el estado del usuario (activo o no)
    @Column(nullable = false)
    private boolean activo = true;

    //Constructor vacío
    protected Usuario() { }

    //Constructor del usuario: recibe alias, email y contraseña encripatada
    public Usuario(String alias, String email, String passwordHash) {
        this.alias = alias;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    //Método que devuelve si el usuario se encuentra bloqueado o no
    public boolean estaBloqueado(Instant ahora) {
        return bloqueadoHasta != null && bloqueadoHasta.isAfter(ahora);
    }


    //Método que que registrar el fallo al iniciar sesión, lo compara con el número de
    //intentos máximos y si se excede, se bloquea la cuenta hasta cierta fecha, leugo de ello
    //el contador de intentos fallidos regresa a 0
    public void registrarFallo(Instant ahora, int intentosMaximos, Duration bloqueo) {
        intentosFallidos++;
        if (intentosFallidos >= intentosMaximos) {
            bloqueadoHasta = ahora.plus(bloqueo);
            intentosFallidos = 0;
        }
    }

    //Método que registra el inicio de sesión exitoso, reinicai el contador de intentos fallidos a 0
    // y deja vacio el espacio de la fecha de desbloqueo
    public void registrarExito() {
        intentosFallidos = 0;
        bloqueadoHasta = null;
    }

    //Getters
    public UUID getId() { return id; }
    public String getAlias() { return alias; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public RolUsuario getRol() { return rol; }
    public boolean isActivo() { return activo; }
}
