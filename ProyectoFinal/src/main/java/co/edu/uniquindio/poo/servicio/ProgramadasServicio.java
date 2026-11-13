package co.edu.uniquindio.poo.servicio;

import co.edu.uniquindio.poo.excepcion.ClienteNoEncontradoException;
import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.excepcion.MonederoException;
import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;
import co.edu.uniquindio.poo.excepcion.SaldoInsuficienteException;
import co.edu.uniquindio.poo.modelo.Categoria;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Frecuencia;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.TipoTransaccion;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.modelo.TransaccionProgramada;
import co.edu.uniquindio.poo.repositorio.ClienteRepositorio;
import co.edu.uniquindio.poo.util.Reloj;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;

public class ProgramadasServicio {
    private static final Comparator<TransaccionProgramada> POR_FECHA_DE_EJECUCION =
            Comparator.comparing(TransaccionProgramada::getProximaEjecucion)
                    .thenComparing(TransaccionProgramada::getId);

    private final ClienteRepositorio clienteRepositorio;
    private final MonederoServicio monederoServicio;
    private final AlertasServicio alertasServicio;
    private final Reloj reloj;

    public ProgramadasServicio(ClienteRepositorio clienteRepositorio, MonederoServicio monederoServicio,
                               AlertasServicio alertasServicio, Reloj reloj) {
        this.clienteRepositorio = Objects.requireNonNull(clienteRepositorio);
        this.monederoServicio = Objects.requireNonNull(monederoServicio);
        this.alertasServicio = Objects.requireNonNull(alertasServicio);
        this.reloj = Objects.requireNonNull(reloj);
    }

    public record ResultadoEjecucion(TransaccionProgramada programada, LocalDate fechaProgramada,
                                     Transaccion transaccion, String error) {
        public boolean exitosa() {
            return error == null;
        }

        @Override
        public String toString() {
            return fechaProgramada + " " + programada.getId() + " " + programada.getTipo() + " "
                    + programada.getMonto().toPlainString() + " -> "
                    + (exitosa() ? "ejecutada como " + transaccion.getId() : "FALLÓ: " + error);
        }
    }

    public TransaccionProgramada programarDeposito(String idMonedero, BigDecimal monto, Frecuencia frecuencia,
                                                   LocalDate primeraEjecucion, String descripcion)
            throws DatoInvalidoException, MontoInvalidoException {
        Monedero monedero = monederoServicio.buscarMonedero(idMonedero);
        validarFecha(primeraEjecucion);
        TransaccionProgramada programada = new TransaccionProgramada(TipoTransaccion.DEPOSITO, monto, monedero,
                null, null, frecuencia, primeraEjecucion, descripcion);
        monedero.getPropietario().agregarProgramada(programada);
        return programada;
    }

    public TransaccionProgramada programarTransferencia(String idOrigen, String idDestino, BigDecimal monto,
                                                        Categoria categoria, Frecuencia frecuencia,
                                                        LocalDate primeraEjecucion, String descripcion)
            throws DatoInvalidoException, MontoInvalidoException {
        Monedero origen = monederoServicio.buscarMonedero(idOrigen);
        Monedero destino = monederoServicio.buscarMonedero(idDestino);
        validarFecha(primeraEjecucion);
        TransaccionProgramada programada = new TransaccionProgramada(TipoTransaccion.TRANSFERENCIA, monto, origen,
                destino, categoria, frecuencia, primeraEjecucion, descripcion);
        origen.getPropietario().agregarProgramada(programada);
        return programada;
    }

    public List<TransaccionProgramada> listarProgramadas(String idCliente) throws ClienteNoEncontradoException {
        Cliente cliente = clienteRepositorio.buscarPorId(idCliente)
                .orElseThrow(() -> new ClienteNoEncontradoException(idCliente));
        return cliente.getProgramadas().stream()
                .sorted(POR_FECHA_DE_EJECUCION)
                .toList();
    }

    public List<ResultadoEjecucion> ejecutarPendientes() throws DatoInvalidoException {
        LocalDate hoy = reloj.hoy();
        PriorityQueue<TransaccionProgramada> cola = new PriorityQueue<>(POR_FECHA_DE_EJECUCION);
        clienteRepositorio.listar().stream()
                .flatMap(cliente -> cliente.getProgramadas().stream())
                .filter(programada -> programada.estaPendienteEn(hoy))
                .forEach(cola::add);

        List<ResultadoEjecucion> resultados = new ArrayList<>();
        while (!cola.isEmpty()) {
            TransaccionProgramada programada = cola.poll();
            resultados.add(ejecutar(programada));
            programada.avanzarProximaEjecucion();
            if (programada.estaPendienteEn(hoy)) {
                cola.add(programada);
            }
        }
        return resultados;
    }

    private ResultadoEjecucion ejecutar(TransaccionProgramada programada) throws DatoInvalidoException {
        LocalDate fecha = programada.getProximaEjecucion();
        String descripcion = "Programada " + programada.getId()
                + (programada.getDescripcion().isEmpty() ? "" : ": " + programada.getDescripcion());
        try {
            Transaccion transaccion = switch (programada.getTipo()) {
                case DEPOSITO -> monederoServicio.depositar(programada.getOrigen().getId(),
                        programada.getMonto(), descripcion);
                case TRANSFERENCIA -> monederoServicio.transferir(programada.getOrigen().getId(),
                        programada.getDestino().getId(), programada.getMonto(), programada.getCategoria(),
                        descripcion);
                case RETIRO -> throw new DatoInvalidoException("tipo", "no se permiten retiros programados");
            };
            return new ResultadoEjecucion(programada, fecha, transaccion, null);
        } catch (SaldoInsuficienteException e) {
            alertasServicio.alertarFalloProgramada(programada, e.getMessage());
            return new ResultadoEjecucion(programada, fecha, null, e.getMessage());
        } catch (MonederoException e) {
            return new ResultadoEjecucion(programada, fecha, null, e.getMessage());
        }
    }

    private void validarFecha(LocalDate primeraEjecucion) throws DatoInvalidoException {
        if (primeraEjecucion != null && primeraEjecucion.isBefore(reloj.hoy())) {
            throw new DatoInvalidoException("fecha de primera ejecución",
                    "no puede ser anterior a hoy (" + reloj.hoy() + ")");
        }
    }
}
