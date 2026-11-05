package pe.telodije.api.identidad;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    @GetMapping("/yo")
    public Map<String, Object> yo(Authentication auth) {
        return Map.of(
                "id", auth.getName(),
                "roles", auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList());
    }
}