package co.edu.uniquindio.poo.servicio;

import co.edu.uniquindio.poo.excepcion.ClienteNoEncontradoException;
import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;
import co.edu.uniquindio.poo.excepcion.PuntosInsuficientesException;
import co.edu.uniquindio.poo.excepcion.SaldoInsuficienteException;
import co.edu.uniquindio.poo.excepcion.TransaccionNoReversibleException;
import co.edu.uniquindio.poo.modelo.Beneficio;
import co.edu.uniquindio.poo.modelo.Categoria;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Deposito;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.Retiro;
import co.edu.uniquindio.poo.modelo.TipoMonedero;
import co.edu.uniquindio.poo.modelo.TipoTransaccion;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.modelo.Transferencia;
import co.edu.uniquindio.poo.repositorio.ClienteRepositorio;
import co.edu.uniquindio.poo.repositorio.TransaccionRepositorio;
import co.edu.uniquindio.poo.util.Config;
import co.edu.uniquindio.poo.util.Reloj;
import co.edu.uniquindio.poo.util.Validaciones;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class MonederoServicio {
    private static final Comparator<Transaccion> MAS_RECIENTE_PRIMERO =
            Comparator.comparing(Transaccion::getFecha)
                    .thenComparing(Transaccion::getId)
                    .reversed();

    private final ClienteRepositorio clienteRepositorio;
    private final TransaccionRepositorio transaccionRepositorio;
    private final PuntosServicio puntosServicio;
    private final AnalisisServicio analisisServicio;
    private final AlertasServicio alertasServicio;
    private final Reloj reloj;

    public MonederoServicio(ClienteRepositorio clienteRepositorio, TransaccionRepositorio transaccionRepositorio,
                            PuntosServicio puntosServicio, AnalisisServicio analisisServicio,
                            AlertasServicio alertasServicio, Reloj reloj) {
        this.clienteRepositorio = Objects.requireNonNull(clienteRepositorio);
        this.transaccionRepositorio = Objects.requireNonNull(transaccionRepositorio);
        this.puntosServicio = Objects.requireNonNull(puntosServicio);
        this.analisisServicio = Objects.requireNonNull(analisisServicio);
        this.alertasServicio = Objects.requireNonNull(alertasServicio);
        this.reloj = Objects.requireNonNull(reloj);
    }

    public Cliente registrarCliente(String identificacion, String nombre, String correo, String telefono,
                                    String contrasena) throws DatoInvalidoException {
        Cliente cliente = new Cliente(identificacion, nombre, correo, telefono, contrasena);
        if (clienteRepositorio.existe(cliente.getIdentificacion())) {
            throw new DatoInvalidoException("identificación",
                    "ya existe un cliente con identificación " + cliente.getIdentificacion());
        }
        clienteRepositorio.guardar(cliente);
        return cliente;
    }

    public Cliente buscarCliente(String identificacion) throws ClienteNoEncontradoException {
        return clienteRepositorio.buscarPorId(identificacion)
                .orElseThrow(() -> new ClienteNoEncontradoException(identificacion));
    }

    public Monedero crearMonedero(String idCliente, String nombre, TipoMonedero tipo)
            throws ClienteNoEncontradoException, DatoInvalidoException {
        return buscarCliente(idCliente).abrirMonedero(nombre, tipo);
    }

    public Monedero buscarMonedero(String idMonedero) throws DatoInvalidoException {
        return clienteRepositorio.listar().stream()
                .flatMap(cliente -> cliente.getMonederos().stream())
                .filter(monedero -> monedero.getId().equals(idMonedero))
                .findFirst()
                .orElseThrow(() -> new DatoInvalidoException("monedero", "no existe el monedero " + idMonedero));
    }

    public Deposito depositar(String idMonedero, BigDecimal monto, String descripcion)
            throws DatoInvalidoException, MontoInvalidoException {
        Monedero monedero = buscarMonedero(idMonedero);
        Deposito deposito = new Deposito(monto, monedero, reloj.ahora(), descripcion);
        deposito.aplicarComision(BigDecimal.ONE);
        monedero.acreditar(deposito.getMonto());
        completarYRegistrar(deposito, monedero);
        return deposito;
    }

    public Retiro retirar(String idMonedero, BigDecimal monto, Categoria categoria, String descripcion)
            throws DatoInvalidoException, MontoInvalidoException, SaldoInsuficienteException {
        Monedero origen = buscarMonedero(idMonedero);
        Retiro retiro = new Retiro(monto, origen, categoria, reloj.ahora(), descripcion);
        retiro.aplicarComision(factorRetiro(origen.getPropietario()));
        origen.debitar(retiro.getMontoTotal());
        completarYRegistrar(retiro, origen);
        alertasServicio.revisarSaldoBajo(origen, retiro.getMontoTotal());
        return retiro;
    }

    public Transferencia transferir(String idOrigen, String idDestino, BigDecimal monto, Categoria categoria,
                                    String descripcion)
            throws DatoInvalidoException, MontoInvalidoException, SaldoInsuficienteException {
        Monedero origen = buscarMonedero(idOrigen);
        Monedero destino = buscarMonedero(idDestino);
        Transferencia transferencia = new Transferencia(monto, origen, destino, categoria, reloj.ahora(), descripcion);

        Optional<Beneficio> beneficio = beneficioAplicable(transferencia);
        transferencia.aplicarComision(factorTransferencia(origen.getPropietario(), beneficio.isPresent()));

        origen.debitar(transferencia.getMontoTotal());
        destino.acreditar(transferencia.getMonto());
        completarYRegistrar(transferencia, origen, destino);
        alertasServicio.revisarSaldoBajo(origen, transferencia.getMontoTotal());

        beneficio.ifPresent(Beneficio::marcarUsado);
        return transferencia;
    }

    public BigDecimal cotizarComision(TipoTransaccion tipo, String idOrigen, String idDestino, BigDecimal monto)
            throws DatoInvalidoException, MontoInvalidoException {
        Validaciones.noNulo(tipo, "tipo de transacción");
        Monedero origen = buscarMonedero(idOrigen);
        Cliente cliente = origen.getPropietario();
        return switch (tipo) {
            case DEPOSITO -> new Deposito(monto, origen, reloj.ahora(), "")
                    .aplicarComision(BigDecimal.ONE);
            case RETIRO -> new Retiro(monto, origen, Categoria.OTROS, reloj.ahora(), "")
                    .aplicarComision(factorRetiro(cliente));
            case TRANSFERENCIA -> {
                Transferencia prueba = new Transferencia(monto, origen, buscarMonedero(idDestino),
                        Categoria.OTROS, reloj.ahora(), "");
                yield prueba.aplicarComision(factorTransferencia(cliente, beneficioAplicable(prueba).isPresent()));
            }
        };
    }

    public Transaccion revertir(String idTransaccion)
            throws DatoInvalidoException, TransaccionNoReversibleException, SaldoInsuficienteException,
            PuntosInsuficientesException, MontoInvalidoException {
        Transaccion transaccion = transaccionRepositorio.buscarPorId(idTransaccion)
                .orElseThrow(() -> new DatoInvalidoException("transacción", "no existe la transacción " + idTransaccion));

        if (!transaccion.puedeRevertirse()) {
            throw new TransaccionNoReversibleException("La transacción " + idTransaccion
                    + " no se puede revertir porque está " + transaccion.getEstado());
        }
        puntosServicio.validarReversionPuntos(transaccion);

        transaccion.revertir();

        puntosServicio.revertirPuntos(transaccion);
        transaccion.marcarRevertida();
        return transaccion;
    }

    public BigDecimal consultarSaldo(String idMonedero) throws DatoInvalidoException {
        return buscarMonedero(idMonedero).getSaldo();
    }

    public BigDecimal saldoTotal(String idCliente) throws ClienteNoEncontradoException {
        return buscarCliente(idCliente).getMonederos().stream()
                .map(Monedero::getSaldo)
                .reduce(Config.redondear(BigDecimal.ZERO), BigDecimal::add);
    }

    public List<Transaccion> historial(String idCliente) throws ClienteNoEncontradoException, DatoInvalidoException {
        return historialFiltrado(idCliente, null, null, null, null);
    }

    public List<Transaccion> historialFiltrado(String idCliente, TipoTransaccion tipo, Categoria categoria,
                                               LocalDate desde, LocalDate hasta)
            throws ClienteNoEncontradoException, DatoInvalidoException {
        Cliente cliente = buscarCliente(idCliente);
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new DatoInvalidoException("rango de fechas", "la fecha inicial es posterior a la final");
        }

        Predicate<Transaccion> filtro = t -> true;
        if (tipo != null) {
            filtro = filtro.and(t -> t.getTipo() == tipo);
        }
        if (categoria != null) {
            filtro = filtro.and(t -> t.getCategoriaGasto().filter(c -> c == categoria).isPresent());
        }
        if (desde != null) {
            filtro = filtro.and(t -> !t.getFecha().toLocalDate().isBefore(desde));
        }
        if (hasta != null) {
            filtro = filtro.and(t -> !t.getFecha().toLocalDate().isAfter(hasta));
        }

        return cliente.getMonederos().stream()
                .flatMap(monedero -> monedero.getHistorial().stream())
                .distinct()
                .filter(filtro)
                .sorted(MAS_RECIENTE_PRIMERO)
                .toList();
    }

    private void completarYRegistrar(Transaccion transaccion, Monedero... monederos) throws DatoInvalidoException {
        transaccion.marcarCompletada();
        for (Monedero monedero : monederos) {
            monedero.registrarTransaccion(transaccion);
        }
        transaccionRepositorio.guardar(transaccion);
        puntosServicio.otorgarPuntos(transaccion);
        analisisServicio.evaluarAtipica(transaccion);
    }

    private BigDecimal factorRetiro(Cliente cliente) {
        return puntosServicio.tieneRetirosSinComision(cliente) ? BigDecimal.ZERO : BigDecimal.ONE;
    }

    private BigDecimal factorTransferencia(Cliente cliente, boolean usaBeneficio) {
        BigDecimal factor = BigDecimal.ONE.subtract(cliente.getRango().getDescuentoTransferencia());
        if (usaBeneficio) {
            factor = factor.multiply(BigDecimal.ONE.subtract(Config.DESCUENTO_BENEFICIO_TRANSFERENCIA));
        }
        return factor;
    }

    private Optional<Beneficio> beneficioAplicable(Transferencia transferencia) {
        if (transferencia.esEntreMonederosPropios()) {
            return Optional.empty();
        }
        return puntosServicio.buscarDescuentoTransferencia(transferencia.getOrigen().getPropietario());
    }
}
