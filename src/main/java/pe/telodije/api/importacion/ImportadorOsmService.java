package pe.telodije.api.importacion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class ImportadorOsmService {

    private static final String CONSULTA = """
        [out:json][timeout:120];
        area["name"="Lima"]["admin_level"="6"]->.lima;
        (
          node["tourism"~"^(hostel|guest_house|hotel|motel)$"](area.lima);
          way["tourism"~"^(hostel|guest_house|hotel|motel)$"](area.lima);
        );
        out center tags;
        """;

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();
    private final String urlOverpass;

    public ImportadorOsmService(JdbcTemplate jdbc,
                                @Value("${importacion.overpass-url}") String urlOverpass) {
        this.jdbc = jdbc;
        this.urlOverpass = urlOverpass;
    }

    public record Resumen(int recibidos, int insertados, int actualizados, int descartados) { }

    public Resumen importar() throws Exception {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create(urlOverpass))
                .timeout(Duration.ofMinutes(3))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "data=" + java.net.URLEncoder.encode(CONSULTA, StandardCharsets.UTF_8)))
                .build();

        HttpResponse<String> respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString());
        if (respuesta.statusCode() != 200) {
            throw new IllegalStateException("Overpass respondió " + respuesta.statusCode());
        }

        JsonNode elementos = mapper.readTree(respuesta.body()).path("elements");
        int insertados = 0, actualizados = 0, descartados = 0;

        for (JsonNode el : elementos) {
            JsonNode tags = el.path("tags");
            String nombre = tags.path("name").asText("").trim();
            double lat = el.has("lat") ? el.get("lat").asDouble() : el.path("center").path("lat").asDouble(Double.NaN);
            double lng = el.has("lon") ? el.get("lon").asDouble() : el.path("center").path("lon").asDouble(Double.NaN);

            // Sin nombre o sin coordenadas no sirve como referencia: se descarta.
            if (nombre.isEmpty() || Double.isNaN(lat) || Double.isNaN(lng)) {
                descartados++;
                continue;
            }

            String tipo = mapearTipo(tags.path("tourism").asText());
            String tipoOsm = el.path("type").asText().equals("node") ? "n" : "w";
            long osmId = el.path("id").asLong();
            String direccion = construirDireccion(tags);

            int filas = jdbc.update("""
                INSERT INTO alojamientos (nombre, tipo, ubicacion, direccion, tiene_cochera,
                                          estado_publicacion, fuente_dato, osm_tipo, osm_id,
                                          sin_datos_completos)
                VALUES (?, ?::tipo_alojamiento,
                        ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
                        ?, FALSE, 'APROBADO', 'OSM', ?, ?, TRUE)
                ON CONFLICT (osm_tipo, osm_id) WHERE osm_id IS NOT NULL
                DO UPDATE SET nombre = EXCLUDED.nombre,
                              ubicacion = EXCLUDED.ubicacion,
                              direccion = EXCLUDED.direccion,
                              actualizado_en = now()
                """,
                    nombre, tipo, lng, lat, direccion, tipoOsm, osmId);

            if (filas == 1) insertados++; else actualizados++;
        }

        return new Resumen(elementos.size(), insertados, actualizados, descartados);
    }

    private static String mapearTipo(String tourism) {
        return switch (tourism) {
            case "hotel" -> "HOTEL";
            case "hostel", "guest_house", "motel" -> "HOSTAL";
            default -> "OTRO";
        };
    }

    private static String construirDireccion(JsonNode tags) {
        String calle = tags.path("addr:street").asText("");
        String numero = tags.path("addr:housenumber").asText("");
        String distrito = tags.path("addr:suburb").asText(tags.path("addr:district").asText(""));
        String direccion = (calle + " " + numero).trim();
        if (!distrito.isEmpty()) direccion = direccion.isEmpty() ? distrito : direccion + ", " + distrito;
        return direccion.isEmpty() ? "Lima" : direccion;
    }
}