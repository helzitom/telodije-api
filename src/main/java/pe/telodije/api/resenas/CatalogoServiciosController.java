package pe.telodije.api.resenas;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/servicios")
public class CatalogoServiciosController {

    private final JdbcTemplate jdbc;

    public CatalogoServiciosController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public List<Map<String, Object>> listar() {
        return jdbc.queryForList(
                "SELECT id, codigo, nombre FROM servicios WHERE activo = TRUE ORDER BY id");
    }
}