package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.util.Validaciones;

import java.time.LocalDateTime;

public class MovimientoPuntos {
    private final TipoMovimientoPuntos tipo;
    private final int puntos;
    private final String descripcion;
    private final Transaccion transaccion;
    private final LocalDateTime fecha;

    MovimientoPuntos(TipoMovimientoPuntos tipo, int puntos, String descripcion, Transaccion transaccion,
                     LocalDateTime fecha) throws DatoInvalidoException {
        if (puntos <= 0) {
            throw new DatoInvalidoException("puntos", "un movimiento debe tener puntos mayores que cero");
        }
        this.tipo = Validaciones.noNulo(tipo, "tipo de movimiento");
        this.puntos = puntos;
        this.descripcion = Validaciones.textoObligatorio(descripcion, "descripción");
        this.transaccion = transaccion;
        this.fecha = Validaciones.noNulo(fecha, "fecha");
    }

    public TipoMovimientoPuntos getTipo() {
        return tipo;
    }

    public int getPuntos() {
        return puntos;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Transaccion getTransaccion() {
        return transaccion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    @Override
    public String toString() {
        String signo = tipo == TipoMovimientoPuntos.GANADO ? "+" : "-";
        return fecha.toLocalDate() + " " + tipo + " " + signo + puntos + " " + descripcion;
    }
}
