package co.edu.uniquindio.poo.excepcion;

public class DatoInvalidoException extends MonederoException {
    public DatoInvalidoException(String campo, String motivo) {
        super("Dato inválido en '" + campo + "': " + motivo);
    }
}
