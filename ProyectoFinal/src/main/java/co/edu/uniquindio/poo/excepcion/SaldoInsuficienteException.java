package co.edu.uniquindio.poo.excepcion;

import java.math.BigDecimal;

public class SaldoInsuficienteException extends MonederoException {
    private final BigDecimal saldoDisponible;
    private final BigDecimal montoRequerido;

    public SaldoInsuficienteException(BigDecimal saldoDisponible, BigDecimal montoRequerido) {
        super("Saldo insuficiente: disponible " + saldoDisponible.toPlainString()
                + ", requerido " + montoRequerido.toPlainString());
        this.saldoDisponible = saldoDisponible;
        this.montoRequerido = montoRequerido;
    }

    public BigDecimal getSaldoDisponible() {
        return saldoDisponible;
    }

    public BigDecimal getMontoRequerido() {
        return montoRequerido;
    }
}
