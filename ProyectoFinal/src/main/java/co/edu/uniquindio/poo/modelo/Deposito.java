package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;
import co.edu.uniquindio.poo.excepcion.SaldoInsuficienteException;
import co.edu.uniquindio.poo.util.Config;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Deposito extends Transaccion {
    public Deposito(BigDecimal monto, Monedero destino, LocalDateTime fecha, String descripcion)
            throws MontoInvalidoException, DatoInvalidoException {
        super(monto, destino, fecha, descripcion);
    }

    @Override
    public TipoTransaccion getTipo() {
        return TipoTransaccion.DEPOSITO;
    }

    @Override
    public int calcularPuntos() {
        return bloquesParaPuntos() * Config.PUNTOS_POR_BLOQUE_DEPOSITO;
    }
    @Override
    public BigDecimal getTasaComisionBase() {
        return Config.COMISION_DEPOSITO;
    }

    @Override
    public void revertir() throws
    MontoInvalidoException, SaldoInsuficienteException{
        getOrigen().debitar(getMonto());
    }
}
