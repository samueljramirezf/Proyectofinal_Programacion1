package co.edu.uniquindio.poo.servicio;

import co.edu.uniquindio.poo.excepcion.ClienteNoEncontradoException;
import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;
import co.edu.uniquindio.poo.excepcion.PuntosInsuficientesException;
import co.edu.uniquindio.poo.modelo.Beneficio;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.MovimientoPuntos;
import co.edu.uniquindio.poo.modelo.Rango;
import co.edu.uniquindio.poo.modelo.TipoBeneficio;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.repositorio.ClienteRepositorio;
import co.edu.uniquindio.poo.util.Config;
import co.edu.uniquindio.poo.util.Reloj;
import co.edu.uniquindio.poo.util.Validaciones;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PuntosServicio {
    private final ClienteRepositorio clienteRepositorio;
    private final Reloj reloj;

    public PuntosServicio(ClienteRepositorio clienteRepositorio, Reloj reloj) {
        this.clienteRepositorio = Objects.requireNonNull(clienteRepositorio);
        this.reloj = Objects.requireNonNull(reloj);
    }

    public int otorgarPuntos(Transaccion transaccion) throws DatoInvalidoException {
        Validaciones.noNulo(transaccion, "transacción");
        int puntos = transaccion.calcularPuntos();
        emisorDe(transaccion).ganarPuntos(puntos, transaccion, reloj.ahora());
        return puntos;
    }

    public void validarReversionPuntos(Transaccion transaccion) throws PuntosInsuficientesException {
        Cliente emisor = emisorDe(transaccion);
        int puntos = transaccion.calcularPuntos();
        if (puntos > emisor.getPuntosDisponibles()) {
            throw new PuntosInsuficientesException(emisor.getPuntosDisponibles(), puntos);
        }
    }

    public void revertirPuntos(Transaccion transaccion) throws PuntosInsuficientesException, DatoInvalidoException {
        emisorDe(transaccion).revertirPuntos(transaccion.calcularPuntos(), transaccion, reloj.ahora());
    }

    public Rango consultarRango(String idCliente) throws ClienteNoEncontradoException {
        return buscarCliente(idCliente).getRango();
    }

    public List<MovimientoPuntos> historialPuntos(String idCliente) throws ClienteNoEncontradoException {
        return buscarCliente(idCliente).getMovimientosPuntos().stream()
                .sorted(Comparator.comparing(MovimientoPuntos::getFecha))
                .toList();
    }

    public List<Beneficio> beneficiosVigentes(String idCliente) throws ClienteNoEncontradoException {
        return buscarCliente(idCliente).getBeneficios().stream()
                .filter(b -> b.estaVigente(reloj.hoy()))
                .toList();
    }

    public Beneficio canjear(String idCliente, TipoBeneficio tipo, String idMonederoBono)
            throws ClienteNoEncontradoException, PuntosInsuficientesException,
            DatoInvalidoException, MontoInvalidoException {
        Cliente cliente = buscarCliente(idCliente);
        Validaciones.noNulo(tipo, "tipo de beneficio");

        Monedero monederoBono = null;
        if (tipo == TipoBeneficio.BONO_SALDO) {
            monederoBono = cliente.buscarMonedero(idMonederoBono)
                    .orElseThrow(() -> new DatoInvalidoException("monedero",
                            "el cliente no tiene el monedero " + idMonederoBono));
        }
        Optional<Beneficio> vigenteParaExtender = tipo.isUsoUnico()
                ? Optional.empty()
                : buscarVigente(cliente, tipo);

        String descripcion = "Canje: " + tipo.getDescripcion()
                + (monederoBono != null ? " -> " + monederoBono.getId() : "");
        cliente.canjearPuntos(tipo.getCostoPuntos(), descripcion, reloj.ahora());

        if (vigenteParaExtender.isPresent()) {
            Beneficio existente = vigenteParaExtender.get();
            existente.extenderVigencia(tipo.getDiasVigencia());
            return existente;
        }
        Beneficio nuevo = new Beneficio(tipo, reloj.hoy());
        cliente.agregarBeneficio(nuevo);
        if (monederoBono != null) {
            monederoBono.acreditar(Config.MONTO_BONO_SALDO);
            nuevo.marcarUsado();
        }
        return nuevo;
    }

    public boolean tieneRetirosSinComision(Cliente cliente) {
        return buscarVigente(cliente, TipoBeneficio.RETIROS_SIN_COMISION).isPresent();
    }

    public Optional<Beneficio> buscarDescuentoTransferencia(Cliente cliente) {
        return buscarVigente(cliente, TipoBeneficio.DESCUENTO_TRANSFERENCIA);
    }

    private Optional<Beneficio> buscarVigente(Cliente cliente, TipoBeneficio tipo) {
        return cliente.getBeneficios().stream()
                .filter(b -> b.getTipo() == tipo)
                .filter(b -> b.estaVigente(reloj.hoy()))
                .findFirst();
    }

    private Cliente emisorDe(Transaccion transaccion) {
        return transaccion.getOrigen().getPropietario();
    }

    private Cliente buscarCliente(String idCliente) throws ClienteNoEncontradoException {
        return clienteRepositorio.buscarPorId(idCliente)
                .orElseThrow(() -> new ClienteNoEncontradoException(idCliente));
    }
}
