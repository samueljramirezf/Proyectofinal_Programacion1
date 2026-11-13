package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;
import co.edu.uniquindio.poo.util.Validaciones;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransaccionProgramada {
    private static int contadorIds = 1;

    private final String id;
    private final TipoTransaccion tipo;
    private final BigDecimal monto;
    private final Monedero origen;
    private final Monedero destino;
    private final Categoria categoria;
    private final Frecuencia frecuencia;
    private final String descripcion;
    private LocalDate proximaEjecucion;
    private boolean activa;

    public TransaccionProgramada(TipoTransaccion tipo, BigDecimal monto, Monedero origen, Monedero destino,
                                 Categoria categoria, Frecuencia frecuencia, LocalDate primeraEjecucion,
                                 String descripcion)
            throws MontoInvalidoException, DatoInvalidoException {
        this.tipo = Validaciones.noNulo(tipo, "tipo de transacción");
        this.monto = Validaciones.montoPositivo(monto);
        this.origen = Validaciones.noNulo(origen, "monedero origen");
        this.frecuencia = Validaciones.noNulo(frecuencia, "frecuencia");
        this.proximaEjecucion = Validaciones.noNulo(primeraEjecucion, "fecha de primera ejecución");

        if (tipo == TipoTransaccion.TRANSFERENCIA) {
            Validaciones.noNulo(destino, "monedero destino");
            if (origen.equals(destino)) {
                throw new DatoInvalidoException("monedero destino", "no puede ser el mismo monedero de origen");
            }
        } else if (destino != null) {
            throw new DatoInvalidoException("monedero destino", "solo aplica a transferencias");
        }
        if (tipo != TipoTransaccion.DEPOSITO) {
            Validaciones.noNulo(categoria, "categoría");
        }

        this.destino = destino;
        this.categoria = tipo == TipoTransaccion.DEPOSITO ? null : categoria;
        this.descripcion = descripcion == null ? "" : descripcion.trim();
        this.activa = true;
        this.id = "P-" + String.format("%04d", contadorIds++);
    }

    public boolean estaPendienteEn(LocalDate hoy) {
        return activa && !proximaEjecucion.isAfter(hoy);
    }

    public void avanzarProximaEjecucion() {
        proximaEjecucion = frecuencia.siguienteFecha(proximaEjecucion);
    }

    public void desactivar() {
        activa = false;
    }

    public String getId() {
        return id;
    }

    public TipoTransaccion getTipo() {
        return tipo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public Monedero getOrigen() {
        return origen;
    }

    public Monedero getDestino() {
        return destino;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Frecuencia getFrecuencia() {
        return frecuencia;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDate getProximaEjecucion() {
        return proximaEjecucion;
    }

    public boolean isActiva() {
        return activa;
    }

    @Override
    public String toString() {
        return id + " " + tipo + " " + monto.toPlainString() + " " + frecuencia
                + " próxima " + proximaEjecucion + (activa ? "" : " [INACTIVA]");
    }
}
