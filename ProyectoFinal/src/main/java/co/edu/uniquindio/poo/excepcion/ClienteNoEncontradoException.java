package co.edu.uniquindio.poo.excepcion;

public class ClienteNoEncontradoException extends MonederoException {
    public ClienteNoEncontradoException(String identificacion) {
        super("No existe un cliente con identificación " + identificacion);
    }
}
