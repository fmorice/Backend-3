package cl.duoc.eft.s9.batch.movimientos;

public class RejectedMovimiento {

    private long numeroFila;
    private String motivo;
    private String id;
    private String fecha;
    private String monto;
    private String tipo;

    public RejectedMovimiento() {
    }

    public RejectedMovimiento(long numeroFila, String motivo, String id, String fecha, String monto, String tipo) {
        this.numeroFila = numeroFila;
        this.motivo = motivo;
        this.id = id;
        this.fecha = fecha;
        this.monto = monto;
        this.tipo = tipo;
    }

    public long getNumeroFila() {
        return numeroFila;
    }

    public void setNumeroFila(long numeroFila) {
        this.numeroFila = numeroFila;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getMonto() {
        return monto;
    }

    public void setMonto(String monto) {
        this.monto = monto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
