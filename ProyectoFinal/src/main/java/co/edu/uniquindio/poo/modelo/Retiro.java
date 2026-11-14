package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;
import co.edu.uniquindio.poo.excepcion.SaldoInsuficienteException;
import co.edu.uniquindio.poo.util.Config;
import co.edu.uniquindio.poo.util.Validaciones;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public class Retiro extends Transaccion {
    private final Categoria categoria;

    public Retiro(BigDecimal monto, Monedero origen, Categoria categoria, LocalDateTime fecha, String descripcion)
            throws MontoInvalidoException, DatoInvalidoException {
        super(monto, origen, fecha, descripcion);
        this.categoria = Validaciones.noNulo(categoria, "categoría");
    }

    @Override
    public TipoTransaccion getTipo() {
        return TipoTransaccion.RETIRO;
    }

    @Override
    public int calcularPuntos() {
        return bloquesParaPuntos() * Config.PUNTOS_POR_BLOQUE_RETIRO;
    }

    @Override
    public BigDecimal getTasaComisionBase() {
        return Config.COMISION_RETIRO;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    @Override
    public Optional<Categoria> getCategoriaGasto() {
        return Optional.of(categoria);
    }

    @Override
    public void revertir() throws MontoInvalidoException, SaldoInsuficienteException{
        getOrigen().acreditar(getMontoTotal());
    }

    @Override
    public String toString() {
        return super.toString() + " categoría " + categoria;
    }
}
