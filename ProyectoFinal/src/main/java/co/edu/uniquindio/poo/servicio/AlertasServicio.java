package co.edu.uniquindio.poo.servicio;

import co.edu.uniquindio.poo.excepcion.ClienteNoEncontradoException;
import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.modelo.Alerta;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.TipoAlerta;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.modelo.TransaccionProgramada;
import co.edu.uniquindio.poo.repositorio.ClienteRepositorio;
import co.edu.uniquindio.poo.util.Config;
import co.edu.uniquindio.poo.util.Reloj;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class AlertasServicio {
    private final ClienteRepositorio clienteRepositorio;
    private final Reloj reloj;

    public AlertasServicio(ClienteRepositorio clienteRepositorio, Reloj reloj) {
        this.clienteRepositorio = Objects.requireNonNull(clienteRepositorio);
        this.reloj = Objects.requireNonNull(reloj);
    }

    public void revisarSaldoBajo(Monedero monedero, BigDecimal montoDebitado) throws DatoInvalidoException {
        BigDecimal saldoAnterior = monedero.getSaldo().add(montoDebitado);
        boolean estabaArriba = saldoAnterior.compareTo(Config.UMBRAL_SALDO_BAJO) > 0;
        if (estabaArriba && monedero.estaEnSaldoBajo()) {
            crear(monedero.getPropietario(), TipoAlerta.SALDO_BAJO,
                    "El monedero " + monedero.getNombre() + " quedó con saldo bajo: "
                            + monedero.getSaldo().toPlainString());
        }
    }

    public void alertarAtipica(Transaccion transaccion, String detalle) throws DatoInvalidoException {
        crear(transaccion.getOrigen().getPropietario(), TipoAlerta.ATIPICA,
                "Transacción " + transaccion.getId() + " potencialmente sospechosa: " + detalle);
    }

    public void alertarFalloProgramada(TransaccionProgramada programada, String motivo) throws DatoInvalidoException {
        crear(programada.getOrigen().getPropietario(), TipoAlerta.SALDO_BAJO,
                "No se ejecutó la programada " + programada.getId() + " del "
                        + programada.getProximaEjecucion() + ": " + motivo);
    }

    public List<Alerta> alertasDe(String idCliente) throws ClienteNoEncontradoException {
        Cliente cliente = clienteRepositorio.buscarPorId(idCliente)
                .orElseThrow(() -> new ClienteNoEncontradoException(idCliente));
        return cliente.getAlertas().stream()
                .sorted(Comparator.comparing(Alerta::getFecha).thenComparing(Alerta::getId).reversed())
                .toList();
    }

    private void crear(Cliente cliente, TipoAlerta tipo, String mensaje) throws DatoInvalidoException {
        cliente.agregarAlerta(new Alerta(tipo, mensaje, reloj.ahora()));
    }
}
