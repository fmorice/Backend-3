package cl.duoc.eft.s9.batch.movimientos;

public class MovimientoDiarioReporte {

    private String id;
    private String fecha;
    private String monto;
    private String tipo;
    private String estado;
    private long numeroFila;

    public MovimientoDiarioReporte() {
    }

    public MovimientoDiarioReporte(String id, String fecha, String monto, String tipo, String estado) {
        this.id = id;
        this.fecha = fecha;
        this.monto = monto;
        this.tipo = tipo;
        this.estado = estado;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public long getNumeroFila() {
        return numeroFila;
    }

    public void setNumeroFila(long numeroFila) {
        this.numeroFila = numeroFila;
    }
}
