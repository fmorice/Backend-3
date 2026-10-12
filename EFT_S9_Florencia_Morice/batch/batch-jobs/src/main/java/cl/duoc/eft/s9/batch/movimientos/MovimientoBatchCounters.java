package cl.duoc.eft.s9.batch.movimientos;

public record MovimientoBatchCounters(
        long registrosLeidos,
        long registrosEscritos,
        long registrosPendientesNegocio,
        long registrosAceptadosNegocio,
        long registrosSinDecisionNegocio,
        long registrosRechazados,
        long erroresLectura,
        long erroresEscritura) {

    public static MovimientoBatchCounters from(
            long readCount,
            long readSkipCount,
            long writeCount,
            long processSkipCount,
            long writeSkipCount,
            long pendingBusinessCount) {
        long registrosLeidos = readCount + readSkipCount;
        long registrosSinDecisionNegocio = writeCount - pendingBusinessCount;
        if (readCount < 0 || readSkipCount < 0 || writeCount < 0 || processSkipCount < 0
                || writeSkipCount < 0 || pendingBusinessCount < 0 || registrosSinDecisionNegocio < 0) {
            throw new IllegalArgumentException("Los contadores no pueden ser negativos ni exceder sus totales");
        }

        return new MovimientoBatchCounters(
                registrosLeidos,
                writeCount,
                pendingBusinessCount,
                0,
                registrosSinDecisionNegocio,
                processSkipCount,
                readSkipCount,
                writeSkipCount);
    }

    public boolean estaConciliado() {
        return registrosLeidos == registrosEscritos + registrosRechazados + erroresLectura + erroresEscritura;
    }
}