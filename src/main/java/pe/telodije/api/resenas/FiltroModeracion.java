package pe.telodije.api.resenas;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class FiltroModeracion {

    // Lista deliberadamente corta: se amplía con el uso real, no con suposiciones.
    private static final List<String> PALABRAS_OFENSIVAS = List.of(
            "idiota", "estúpido", "imbécil", "maldito", "mierda", "puta", "hijo de");

    private static final Pattern ENLACE =
            Pattern.compile("(https?://|www\\.|\\b[a-z0-9-]+\\.(com|pe|net|org|io)\\b)",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern TELEFONO =
            Pattern.compile("(\\+?\\d[\\s-]?){7,}");

    private static final Pattern REPETICION =
            Pattern.compile("(.)\\1{5,}");

    public boolean requiereRevision(String texto) {
        if (texto == null || texto.isBlank()) {
            return false;
        }
        String normalizado = texto.toLowerCase(Locale.ROOT);
        return PALABRAS_OFENSIVAS.stream().anyMatch(normalizado::contains)
                || ENLACE.matcher(texto).find()
                || TELEFONO.matcher(texto).find()
                || REPETICION.matcher(texto).find();
    }
}