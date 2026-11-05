package pe.telodije.api.identidad;

import jakarta.validation.constraints.*;

import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() { }

    //Métodos para poder validar el ingreso de datos en el registro
    //Alias: No en blanco, regexp para que contega letras de A a la Z, numeros del 0 al 9
    //Email: No en blanco, formato email obligatorio, maximo 254 caracteres
    //Password: No en blanco, minimo 10 caracteres, máximo 128 caracteres
    public record RegistroRequest(
            @NotBlank @Pattern(regexp = "^[A-Za-z0-9_.]{3,30}$") String alias,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 10, max = 128) String password) { }

    //Método para validar el ingreso de datos en el login
    //Identificador: No en blanco y máximo 254 caracteres
    //Password: No en blanco y longitud máxima de 128
    public record LoginRequest(
            @NotBlank @Size(max = 254) String identificador,
            @NotBlank @Size(max = 128) String password) { }

    //Método para que al actualizarse el token, no llegue en blanco y tenga un limite de caracteres de 128
    public record RefrescoRequest(@NotBlank @Size(max = 128) String refreshToken) { }

    //Record que devuelve el backend cuando el usuario se registra correctamente
    public record RegistroResponse(UUID id, String alias) { }

    //Record que representa la respuesta del backend cuando autentica a un usuario o renueva sus tokens
    public record TokensResponse(String accessToken, String refreshToken,
                                 String tipo, long expiraEnSegundos) { }
}