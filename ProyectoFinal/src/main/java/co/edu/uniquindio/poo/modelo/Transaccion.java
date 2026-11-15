package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;
import co.edu.uniquindio.poo.excepcion.TransaccionNoReversibleException;
import co.edu.uniquindio.poo.util.Config;
import co.edu.uniquindio.poo.util.Validaciones;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

public abstract class Transaccion implements Puntuable, Reversible {
    private static int contadorIds = 1;

    private String id;
    private final BigDecimal monto;
    private final Monedero origen;
    private final LocalDateTime fecha;
    private final String descripcion;
    private BigDecimal comision;
    private EstadoTransaccion estado;
    private boolean sospechosa;

    protected Transaccion(BigDecimal monto, Monedero origen, LocalDateTime fecha, String descripcion)
            throws MontoInvalidoException, DatoInvalidoException {
        this.monto = Validaciones.montoPositivo(monto);
        this.origen = Validaciones.noNulo(origen, "monedero origen");
        this.fecha = Validaciones.noNulo(fecha, "fecha");
        this.descripcion = descripcion == null ? "" : descripcion.trim();
        this.comision = Config.redondear(BigDecimal.ZERO);
        this.estado = EstadoTransaccion.PENDIENTE;
    }

    public abstract TipoTransaccion getTipo();

    @Override
    public abstract int calcularPuntos();

    public abstract BigDecimal getTasaComisionBase();

    public Optional<Categoria> getCategoriaGasto() {
        return Optional.empty();
    }

    public boolean esGasto() {
        return getCategoriaGasto().isPresent();
    }

    protected int bloquesParaPuntos() {
        return monto.divide(Config.UNIDADES_POR_BLOQUE_PUNTOS, 0, RoundingMode.FLOOR).intValue();
    }

    public BigDecimal aplicarComision(BigDecimal factorDescuento) throws DatoInvalidoException {
        Validaciones.noNulo(factorDescuento, "factor de descuento");
        if (factorDescuento.signum() < 0 || factorDescuento.compareTo(BigDecimal.ONE) > 0) {
            throw new DatoInvalidoException("factor de descuento", "debe estar entre 0 y 1");
        }
        if (estado != EstadoTransaccion.PENDIENTE) {
            throw new IllegalStateException("La comisión solo se fija antes de completar la transacción " + id);
        }
        comision = Config.redondear(monto.multiply(getTasaComisionBase()).multiply(factorDescuento));
        return comision;
    }

    public BigDecimal getMontoTotal() {
        return monto.add(comision);
    }

    public boolean involucraA(Monedero monedero) {
        return origen.equals(monedero);
    }

    public void marcarCompletada() {
        if (estado != EstadoTransaccion.PENDIENTE) {
            throw new IllegalStateException("La transacción " + id + " ya está " + estado);
        }
        estado = EstadoTransaccion.COMPLETADA;
        id = "T-" + String.format("%05d", contadorIds++);
    }

    @Override
    public boolean puedeRevertirse(){
        return estado == EstadoTransaccion.COMPLETADA;
    }

    public void marcarRevertida() throws TransaccionNoReversibleException {
        if (!(puedeRevertirse())) {
            throw new TransaccionNoReversibleException(
                    "La transacción " + id + " no se puede revertir porque está " + estado);
        }
        estado = EstadoTransaccion.REVERTIDA;
    }

    public void marcarComoSospechosa() {
        sospechosa = true;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public Monedero getOrigen() {
        return origen;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getComision() {
        return comision;
    }

    public EstadoTransaccion getEstado() {
        return estado;
    }

    public boolean isSospechosa() {
        return sospechosa;
    }

    @Override
    public String toString() {
        return (id != null ? id : "(pendiente)") + " " + getTipo() + " " + monto.toPlainString()
                + " (comisión " + comision.toPlainString() + ") " + estado
                + (sospechosa ? " [SOSPECHOSA]" : "");
    }
}
