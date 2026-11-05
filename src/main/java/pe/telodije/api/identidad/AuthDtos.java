package pe.telodije.api.identidad;

import jakarta.validation.constraints.*;

import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() { }

    public record RegistroRequest(
            @NotBlank @Pattern(regexp = "^[A-Za-z0-9_.]{3,30}$") String alias,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 10, max = 128) String password) { }

    public record LoginRequest(
            @NotBlank @Size(max = 254) String identificador,
            @NotBlank @Size(max = 128) String password) { }

    public record RefrescoRequest(@NotBlank @Size(max = 128) String refreshToken) { }

    public record RegistroResponse(UUID id, String alias) { }

    public record TokensResponse(String accessToken, String refreshToken,
                                 String tipo, long expiraEnSegundos) { }
}