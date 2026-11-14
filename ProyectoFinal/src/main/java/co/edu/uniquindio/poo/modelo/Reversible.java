package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;
import co.edu.uniquindio.poo.excepcion.SaldoInsuficienteException;

public interface Reversible {
    boolean puedeRevertirse();
    public void revertir() throws MontoInvalidoException, SaldoInsuficienteException;
}
