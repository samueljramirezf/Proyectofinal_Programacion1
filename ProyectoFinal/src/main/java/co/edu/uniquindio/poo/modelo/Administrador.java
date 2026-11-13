package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;

public class Administrador extends Usuario {
    public Administrador(String identificacion, String nombre, String correo, String telefono, String contrasena)
            throws DatoInvalidoException {
        super(identificacion, nombre, correo, telefono, contrasena);
    }

    @Override
    public String getRol() {
        return "ADMINISTRADOR";
    }
}
