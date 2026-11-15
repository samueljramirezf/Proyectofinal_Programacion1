package co.edu.uniquindio.poo.controller;

import co.edu.uniquindio.poo.excepcion.ClienteNoEncontradoException;
import co.edu.uniquindio.poo.modelo.Alerta;
import co.edu.uniquindio.poo.servicio.AlertasServicio;

import java.util.List;

public class AlertasController {
    private final AlertasServicio alertasServicio;

    public AlertasController(AlertasServicio alertasServicio) {
        this.alertasServicio = alertasServicio;
    }

    public List<Alerta> alertasDe(String idCliente) throws ClienteNoEncontradoException {
        return alertasServicio.alertasDe(idCliente);
    }
}
