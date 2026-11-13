package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;
import co.edu.uniquindio.poo.excepcion.SaldoInsuficienteException;
import co.edu.uniquindio.poo.util.Config;
import co.edu.uniquindio.poo.util.Validaciones;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Monedero {
    private static int contadorIds = 1;

    private final String id;
    private final Cliente propietario;
    private final String nombre;
    private final TipoMonedero tipo;
    private BigDecimal saldo;
    private final List<Transaccion> historial = new ArrayList<>();

    Monedero(Cliente propietario, String nombre, TipoMonedero tipo) throws DatoInvalidoException {
        this.propietario = Validaciones.noNulo(propietario, "propietario");
        this.nombre = Validaciones.textoObligatorio(nombre, "nombre del monedero");
        this.tipo = Validaciones.noNulo(tipo, "tipo de monedero");
        this.saldo = Config.redondear(BigDecimal.ZERO);
        this.id = "M-" + String.format("%04d", contadorIds++);
    }

    public void acreditar(BigDecimal monto) throws MontoInvalidoException {
        BigDecimal valor = Validaciones.montoPositivo(monto);
        saldo = saldo.add(valor);
    }

    public void debitar(BigDecimal monto) throws MontoInvalidoException, SaldoInsuficienteException {
        BigDecimal valor = Validaciones.montoPositivo(monto);
        if (!tieneSaldoSuficiente(valor)) {
            throw new SaldoInsuficienteException(saldo, valor);
        }
        saldo = saldo.subtract(valor);
    }

    public boolean tieneSaldoSuficiente(BigDecimal monto) {
        return saldo.compareTo(monto) >= 0;
    }

    public boolean estaEnSaldoBajo() {
        return saldo.compareTo(Config.UMBRAL_SALDO_BAJO) <= 0;
    }

    public void registrarTransaccion(Transaccion transaccion) throws DatoInvalidoException {
        Validaciones.noNulo(transaccion, "transacción");
        if (!transaccion.involucraA(this)) {
            throw new DatoInvalidoException("transacción", "no pertenece al monedero " + id);
        }
        historial.add(transaccion);
    }

    public String getId() {
        return id;
    }

    public Cliente getPropietario() {
        return propietario;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoMonedero getTipo() {
        return tipo;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public List<Transaccion> getHistorial() {
        return Collections.unmodifiableList(historial);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Monedero)) return false;
        return id.equals(((Monedero) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id + " " + nombre + " [" + tipo + "] saldo " + saldo.toPlainString();
    }
}
