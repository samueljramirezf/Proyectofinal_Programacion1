package co.edu.uniquindio.poo.excepcion;

public abstract class MonederoException extends Exception {
    protected MonederoException(String mensaje) {
        super(mensaje);
    }
}
