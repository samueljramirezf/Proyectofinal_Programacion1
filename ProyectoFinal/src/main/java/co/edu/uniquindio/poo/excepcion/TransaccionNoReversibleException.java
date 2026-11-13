package co.edu.uniquindio.poo.excepcion;

public class TransaccionNoReversibleException extends MonederoException {
    public TransaccionNoReversibleException(String mensaje) {
        super(mensaje);
    }
}
