package pe.telodije.api.importacion;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/importacion")
public class ImportacionController {

    private final ImportadorOsmService importador;

    public ImportacionController(ImportadorOsmService importador) {
        this.importador = importador;
    }

    @PostMapping("/osm")
    public ImportadorOsmService.Resumen importarOsm() throws Exception {
        return importador.importar();
    }
}