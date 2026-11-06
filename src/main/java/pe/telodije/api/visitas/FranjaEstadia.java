package pe.telodije.api.visitas;

public enum FranjaEstadia {
    H4(4), H6(6), H8(8), H12(12), NOCHE_COMPLETA(null), OTRO(null);

    private final Integer horasFijas;

    FranjaEstadia(Integer horasFijas) {
        this.horasFijas = horasFijas;
    }

    public Integer getHorasFijas() {
        return horasFijas;
    }
}