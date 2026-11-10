package pe.telodije.api.busqueda;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class ConcurrenciaService {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final int MINIMO_BUSQUEDAS = 50;
    private final JdbcTemplate jdbc;

    public ConcurrenciaService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Registra una búsqueda sin datos personales: solo hora, día y celda aproximada.
    public void registrarBusqueda(String celdaGeohash, String filtrosJson) {
        ZonedDateTime ahora = ZonedDateTime.now(LIMA);
        jdbc.update(
                "INSERT INTO eventos_busqueda (celda_geohash, hora_local, dia_semana, filtros) " +
                        "VALUES (?, ?, ?, ?::jsonb)",
                celdaGeohash,
                ahora.getHour(),
                ahora.getDayOfWeek().getValue(),
                filtrosJson);
    }

    // Nivel de demanda: compara la hora actual con el promedio por hora de esa celda.
    public String nivelDemanda(String celdaGeohash) {
        Integer actual = jdbc.queryForObject(
                "SELECT COUNT(*) FROM eventos_busqueda " +
                        "WHERE celda_geohash = ? AND hora_local = ? " +
                        "AND ocurrido_en > now() - INTERVAL '30 days'",
                Integer.class, celdaGeohash, ZonedDateTime.now(LIMA).getHour());

        Double total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM eventos_busqueda " +
                        "WHERE celda_geohash = ? AND ocurrido_en > now() - INTERVAL '30 days'",
                Double.class, celdaGeohash);

        // Con poco historial, cualquier hora parece "alta". No se avisa hasta tener base real.
        if (total == null || total < MINIMO_BUSQUEDAS || actual == null) {
            return "SIN_DATOS";
        }

        // Promedio por hora sobre las 24 horas, incluidas las que no tienen búsquedas.
        double promedioPorHora = total / 24.0;

        if (actual >= promedioPorHora * 1.5) return "ALTA";
        return "NORMAL";
    }
}