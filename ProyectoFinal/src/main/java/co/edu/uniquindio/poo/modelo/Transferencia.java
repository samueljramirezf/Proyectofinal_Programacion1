package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;
import co.edu.uniquindio.poo.excepcion.SaldoInsuficienteException;
import co.edu.uniquindio.poo.util.Config;
import co.edu.uniquindio.poo.util.Validaciones;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public class Transferencia extends Transaccion {
    private final Monedero destino;
    private final Categoria categoria;

    public Transferencia(BigDecimal monto, Monedero origen, Monedero destino, Categoria categoria,
                         LocalDateTime fecha, String descripcion)
            throws MontoInvalidoException, DatoInvalidoException {
        super(monto, origen, fecha, descripcion);
        this.destino = Validaciones.noNulo(destino, "monedero destino");
        if (origen.equals(destino)) {
            throw new DatoInvalidoException("monedero destino", "no puede ser el mismo monedero de origen");
        }
        this.categoria = Validaciones.noNulo(categoria, "categoría");
    }

    @Override
    public TipoTransaccion getTipo() {
        return TipoTransaccion.TRANSFERENCIA;
    }

    @Override
    public int calcularPuntos() {
        if (esEntreMonederosPropios()) {
            return 0;
        }
        return bloquesParaPuntos() * Config.PUNTOS_POR_BLOQUE_TRANSFERENCIA;
    }

    @Override
    public BigDecimal getTasaComisionBase() {
        return esEntreMonederosPropios() ? BigDecimal.ZERO : Config.COMISION_TRANSFERENCIA;
    }

    @Override
    public boolean involucraA(Monedero monedero) {
        return super.involucraA(monedero) || destino.equals(monedero);
    }

    public boolean esEntreMonederosPropios() {
        return getOrigen().getPropietario().equals(destino.getPropietario());
    }

    public Monedero getDestino() {
        return destino;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    @Override
    public Optional<Categoria> getCategoriaGasto() {
        return Optional.of(categoria);
    }

    @Override
    public boolean esGasto() {
        return !esEntreMonederosPropios();
    }

    @Override
    public void revertir() throws MontoInvalidoException, SaldoInsuficienteException
    {
    getDestino().debitar(getMonto());
                getOrigen().acreditar(getMontoTotal());
}
    @Override
    public String toString() {
        return super.toString() + " -> " + destino.getId() + " categoría " + categoria;
    }
}
