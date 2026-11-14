package co.edu.uniquindio.poo.notificacion;
import co.edu.uniquindio.poo.modelo.Alerta;
import co.edu.uniquindio.poo.modelo.Cliente;

public interface Notificador {
    public void enviar(Cliente cliente, Alerta alerta);
}
