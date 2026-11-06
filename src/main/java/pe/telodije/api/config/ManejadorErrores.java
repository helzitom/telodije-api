package pe.telodije.api.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.telodije.api.identidad.ErrorNegocio;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ManejadorErrores {

    //Manejo de errores del sistema (negocio)
    @ExceptionHandler(ErrorNegocio.class)
    public ResponseEntity<Map<String, String>> negocio(ErrorNegocio e) {
        return ResponseEntity.status(e.getEstado()).body(Map.of("error", e.getMessage()));
    }

    //Manejo de error de datos de entrada no válidos (validación)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException e) {
        Map<String, String> campos = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        f -> f.getDefaultMessage() == null ? "no válido" : f.getDefaultMessage(),
                        (a, b) -> a));
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Datos de entrada no válidos", "campos", campos));
    }

    //Manejo de error de recursos  no existentes (integridad)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> integridad(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "El recurso ya existe o no cumple las reglas"));
    }
}