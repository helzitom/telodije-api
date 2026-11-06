package pe.telodije.api.multimedia;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.telodije.api.identidad.ErrorNegocio;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fotos")
public class FotoController {

    private final AlmacenFotos almacen;

    public FotoController(AlmacenFotos almacen) {
        this.almacen = almacen;
    }

    public record IntentoSubida(String clave, String urlSubida) { }

    @PostMapping("/llegada/intento")
    public IntentoSubida intentoLlegada(Authentication auth) {
        String clave = AlmacenFotos.prefijoLlegada(auth.getName()) + UUID.randomUUID() + ".jpg";
        return new IntentoSubida(clave, "/api/v1/fotos/subida/" + clave);
    }

    @PutMapping(value = "/subida/{clave}",
            consumes = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void subir(@PathVariable String clave,
                      @RequestBody byte[] contenido,
                      Authentication auth) {
        if (!clave.startsWith(AlmacenFotos.prefijoLlegada(auth.getName()))) {
            throw new ErrorNegocio(HttpStatus.FORBIDDEN, "La foto no pertenece a tu cuenta");
        }
        almacen.guardar(clave, contenido);
    }
}