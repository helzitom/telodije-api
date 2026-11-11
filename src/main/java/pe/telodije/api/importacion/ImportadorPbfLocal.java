package pe.telodije.api.importacion;

import crosby.binary.osmosis.OsmosisReader;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ImportadorPbfLocal {

    // Caja de Lima Metropolitana.
    private static final double SUR = -12.55, OESTE = -77.20, NORTE = -11.75, ESTE = -76.65;
    private static final Set<String> TIPOS = Set.of("hostel", "guest_house", "hotel", "motel");

    public static void main(String[] args) throws Exception {
        String archivo = args[0];
        String url = args[1];
        String usuario = args[2];
        String clave = args[3];

        try (Connection conn = DriverManager.getConnection(url, usuario, clave)) {
            conn.setAutoCommit(false);
            PreparedStatement insertar = conn.prepareStatement("""
                INSERT INTO alojamientos (nombre, tipo, ubicacion, direccion, tiene_cochera,
                                          estado_publicacion, fuente_dato, osm_tipo, osm_id,
                                          sin_datos_completos)
                VALUES (?, ?::tipo_alojamiento,
                        ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
                        ?, FALSE, 'APROBADO', 'OSM', 'n', ?, TRUE)
                ON CONFLICT (osm_tipo, osm_id) WHERE osm_id IS NOT NULL
                DO UPDATE SET nombre = EXCLUDED.nombre,
                              ubicacion = EXCLUDED.ubicacion,
                              direccion = EXCLUDED.direccion,
                              actualizado_en = now()
                """);

            Contador c = new Contador();

            OsmosisReader lector = new OsmosisReader(new FileInputStream(archivo));
            lector.setSink(new Sink() {
                @Override
                public void process(EntityContainer ec) {
                    Entity entidad = ec.getEntity();
                    if (!(entidad instanceof Node nodo)) return;

                    Map<String, String> tags = etiquetas(nodo);
                    String tourism = tags.get("tourism");
                    if (tourism == null || !TIPOS.contains(tourism)) return;
                    c.recibidos++;

                    String nombre = tags.get("name");
                    double lat = nodo.getLatitude();
                    double lng = nodo.getLongitude();
                    if (nombre == null || nombre.isBlank()
                            || lat < SUR || lat > NORTE || lng < OESTE || lng > ESTE) {
                        c.descartados++;
                        return;
                    }
                    try {
                        insertar.setString(1, nombre.trim());
                        insertar.setString(2, mapearTipo(tourism));
                        insertar.setDouble(3, lng);
                        insertar.setDouble(4, lat);
                        insertar.setString(5, construirDireccion(tags));
                        insertar.setLong(6, nodo.getId());
                        insertar.addBatch();
                        c.insertados++;
                        if (c.insertados % 500 == 0) insertar.executeBatch();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void initialize(Map<String, Object> metaData) { }

                @Override
                public void complete() {
                    try {
                        insertar.executeBatch();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void close() { }
            });

            lector.run();
            conn.commit();

            System.out.printf("recibidos=%d insertados=%d descartados=%d%n",
                    c.recibidos, c.insertados, c.descartados);
        }
    }

    private static class Contador {
        int recibidos, insertados, descartados;
    }

    private static Map<String, String> etiquetas(Node nodo) {
        Map<String, String> m = new HashMap<>();
        for (Tag t : nodo.getTags()) m.put(t.getKey(), t.getValue());
        return m;
    }

    private static String mapearTipo(String tourism) {
        return switch (tourism) {
            case "hotel" -> "HOTEL";
            case "hostel", "guest_house", "motel" -> "HOSTAL";
            default -> "OTRO";
        };
    }

    private static String construirDireccion(Map<String, String> tags) {
        String calle = tags.getOrDefault("addr:street", "");
        String numero = tags.getOrDefault("addr:housenumber", "");
        String distrito = tags.getOrDefault("addr:district", "");
        String direccion = (calle + " " + numero).trim();
        if (!distrito.isBlank()) {
            direccion = direccion.isEmpty() ? distrito : direccion + ", " + distrito;
        }
        return direccion.isEmpty() ? "Lima" : direccion;
    }
}