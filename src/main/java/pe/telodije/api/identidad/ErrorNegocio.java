package pe.telodije.api.identidad;

import org.springframework.http.HttpStatus;

//Excepciones
public class ErrorNegocio extends RuntimeException {

    private final HttpStatus estado;

    //Método para manejar los errores de las solicitudes y mostar un mensaje
    public ErrorNegocio(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    //Método para obtener el estado de la solicitud HTTP
    public HttpStatus getEstado() {
        return estado;
    }
}