package co.edu.uniquindio.poo.controller;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.servicio.MonederoServicio;

public class SesionController {
    private final MonederoServicio monederoServicio;

    public SesionController(MonederoServicio monederoServicio) {
        this.monederoServicio = monederoServicio;
    }

    public Cliente iniciarSesion(String identificacion, String contrasena) throws DatoInvalidoException {
        return monederoServicio.iniciarSesion(identificacion, contrasena);
    }

    public Cliente registrarCliente(String identificacion, String nombre, String correo, String telefono,
                                    String contrasena) throws DatoInvalidoException {
        return monederoServicio.registrarCliente(identificacion, nombre, correo, telefono, contrasena);
    }
}
