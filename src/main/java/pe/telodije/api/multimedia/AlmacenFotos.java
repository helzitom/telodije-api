package pe.telodije.api.multimedia;

public interface AlmacenFotos {

    static String prefijoLlegada(String usuarioId) {
        return "llegada-" + usuarioId + "-";
    }

    void guardar(String clave, byte[] contenido);

    boolean existe(String clave);

    String sha256(String clave);
}