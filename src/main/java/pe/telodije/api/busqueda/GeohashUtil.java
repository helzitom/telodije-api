package pe.telodije.api.busqueda;

public final class GeohashUtil {

    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";

    private GeohashUtil() { }

    public static String codificar(double lat, double lng, int precision) {
        double[] latRango = {-90, 90};
        double[] lngRango = {-180, 180};
        int bits = 0;
        int bit = 0;
        boolean par = true;
        StringBuilder resultado = new StringBuilder();

        while (resultado.length() < precision) {
            if (par) {
                double medio = (lngRango[0] + lngRango[1]) / 2;
                if (lng >= medio) { bit = (bit << 1) | 1; lngRango[0] = medio; }
                else { bit = bit << 1; lngRango[1] = medio; }
            } else {
                double medio = (latRango[0] + latRango[1]) / 2;
                if (lat >= medio) { bit = (bit << 1) | 1; latRango[0] = medio; }
                else { bit = bit << 1; latRango[1] = medio; }
            }
            par = !par;
            bits++;
            if (bits == 5) {
                resultado.append(BASE32.charAt(bit));
                bits = 0;
                bit = 0;
            }
        }
        return resultado.toString();
    }
}