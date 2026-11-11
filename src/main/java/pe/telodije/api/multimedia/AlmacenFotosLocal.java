package pe.telodije.api.multimedia;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import pe.telodije.api.identidad.ErrorNegocio;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Pattern;

@Profile("!prod")
@Component
public class AlmacenFotosLocal implements AlmacenFotos {

    private static final int TAMANO_MAXIMO = 5 * 1024 * 1024;
    private static final long PIXELES_MAXIMOS = 40_000_000L;
    private static final Pattern CLAVE_VALIDA =
            Pattern.compile("^llegada-[0-9a-f-]{36}-[0-9a-f-]{36}\\.jpg$");

    private final Path raiz;

    public AlmacenFotosLocal(@Value("${almacenamiento.ruta-local}") String ruta) {
        this.raiz = Path.of(ruta).toAbsolutePath().normalize();
        try {
            Files.createDirectories(raiz);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear el almacén local", e);
        }
    }

    @Override
    public void guardar(String clave, byte[] contenido) {
        if (!esClaveValida(clave)) {
            throw new ErrorNegocio(HttpStatus.BAD_REQUEST, "Clave de foto no válida");
        }
        if (contenido.length == 0) {
            throw new ErrorNegocio(HttpStatus.BAD_REQUEST, "La foto está vacía");
        }
        if (contenido.length > TAMANO_MAXIMO) {
            throw new ErrorNegocio(HttpStatus.PAYLOAD_TOO_LARGE, "La foto supera 5 MB");
        }

        byte[] limpia = normalizarAJpeg(contenido);
        try {
            Files.write(raiz.resolve(clave), limpia,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (FileAlreadyExistsException e) {
            throw new ErrorNegocio(HttpStatus.CONFLICT, "La foto ya fue subida");
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la foto", e);
        }
    }

    @Override
    public boolean existe(String clave) {
        return esClaveValida(clave) && Files.exists(raiz.resolve(clave));
    }

    @Override
    public String sha256(String clave) {
        if (!existe(clave)) {
            throw new IllegalArgumentException("Foto inexistente");
        }
        try {
            byte[] bytes = Files.readAllBytes(raiz.resolve(clave));
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la foto", e);
        }
    }

    private static boolean esClaveValida(String clave) {
        return clave != null && CLAVE_VALIDA.matcher(clave).matches();
    }

    private static ErrorNegocio imagenInvalida() {
        return new ErrorNegocio(HttpStatus.BAD_REQUEST, "El archivo debe ser una imagen JPEG o PNG válida");
    }

    private static byte[] normalizarAJpeg(byte[] bytes) {
        try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (in == null) {
                throw imagenInvalida();
            }
            Iterator<ImageReader> lectores = ImageIO.getImageReaders(in);
            if (!lectores.hasNext()) {
                throw imagenInvalida();
            }
            ImageReader lector = lectores.next();
            try {
                String formato = lector.getFormatName().toLowerCase(Locale.ROOT);
                if (!formato.equals("jpeg") && !formato.equals("png")) {
                    throw imagenInvalida();
                }
                lector.setInput(in);
                long pixeles = (long) lector.getWidth(0) * lector.getHeight(0);
                if (pixeles > PIXELES_MAXIMOS) {
                    throw new ErrorNegocio(HttpStatus.BAD_REQUEST, "La resolución de la imagen es demasiado alta");
                }

                BufferedImage original = lector.read(0);
                BufferedImage rgb = new BufferedImage(
                        original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = rgb.createGraphics();
                try {
                    g.setColor(Color.WHITE);
                    g.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
                    g.drawImage(original, 0, 0, null);
                } finally {
                    g.dispose();
                }

                ByteArrayOutputStream salida = new ByteArrayOutputStream();
                if (!ImageIO.write(rgb, "jpg", salida)) {
                    throw imagenInvalida();
                }
                return salida.toByteArray();
            } finally {
                lector.dispose();
            }
        } catch (IOException e) {
            throw imagenInvalida();
        }
    }
}