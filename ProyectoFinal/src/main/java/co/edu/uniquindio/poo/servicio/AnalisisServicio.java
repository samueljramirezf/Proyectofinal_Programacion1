package co.edu.uniquindio.poo.servicio;

import co.edu.uniquindio.poo.excepcion.ClienteNoEncontradoException;
import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.modelo.Categoria;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.EstadoTransaccion;
import co.edu.uniquindio.poo.modelo.Transaccion;
import co.edu.uniquindio.poo.repositorio.ClienteRepositorio;
import co.edu.uniquindio.poo.util.Config;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class AnalisisServicio {
    private static final MathContext PRECISION = MathContext.DECIMAL64;
    private static final BigDecimal CIEN = new BigDecimal("100");

    private final ClienteRepositorio clienteRepositorio;
    private final AlertasServicio alertasServicio;

    public AnalisisServicio(ClienteRepositorio clienteRepositorio, AlertasServicio alertasServicio) {
        this.clienteRepositorio = Objects.requireNonNull(clienteRepositorio);
        this.alertasServicio = Objects.requireNonNull(alertasServicio);
    }

    public boolean evaluarAtipica(Transaccion nueva) throws DatoInvalidoException {
        Cliente cliente = nueva.getOrigen().getPropietario();
        List<BigDecimal> montos = transaccionesHechasPor(cliente).stream()
                .filter(t -> t != nueva)
                .filter(t -> t.getEstado() != EstadoTransaccion.REVERTIDA)
                .map(Transaccion::getMonto)
                .toList();

        if (montos.size() < Config.MINIMO_TRANSACCIONES_ANALISIS) {
            return false;
        }

        BigDecimal n = BigDecimal.valueOf(montos.size());
        BigDecimal promedio = montos.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(n, PRECISION);
        BigDecimal varianza = montos.stream()
                .map(x -> x.subtract(promedio).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(n, PRECISION);
        BigDecimal desviacion = varianza.sqrt(PRECISION);
        BigDecimal umbral = promedio.add(Config.FACTOR_DESVIACION_ATIPICA.multiply(desviacion));

        if (nueva.getMonto().compareTo(umbral) <= 0) {
            return false;
        }
        nueva.marcarComoSospechosa();
        alertasServicio.alertarAtipica(nueva, "monto " + nueva.getMonto().toPlainString()
                + " supera el umbral " + Config.redondear(umbral).toPlainString()
                + " (promedio " + Config.redondear(promedio).toPlainString()
                + " + 2 × desviación " + Config.redondear(desviacion).toPlainString() + ")");
        return true;
    }

    public Map<Categoria, BigDecimal> gastoPorCategoria(String idCliente) throws ClienteNoEncontradoException {
        return gastosDe(buscarCliente(idCliente)).stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategoriaGasto().orElseThrow(),
                        () -> new EnumMap<>(Categoria.class),
                        Collectors.reducing(Config.redondear(BigDecimal.ZERO), Transaccion::getMonto, BigDecimal::add)));
    }

    public Optional<Categoria> categoriaMayorGasto(String idCliente) throws ClienteNoEncontradoException {
        return gastoPorCategoria(idCliente).entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    public Map<Categoria, BigDecimal> porcentajePorCategoria(String idCliente) throws ClienteNoEncontradoException {
        Map<Categoria, BigDecimal> gastos = gastoPorCategoria(idCliente);
        BigDecimal total = gastos.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<Categoria, BigDecimal> porcentajes = new EnumMap<>(Categoria.class);
        if (total.signum() == 0) {
            return porcentajes;
        }
        gastos.forEach((categoria, monto) ->
                porcentajes.put(categoria, monto.multiply(CIEN).divide(total, 2, RoundingMode.HALF_EVEN)));
        return porcentajes;
    }

    public Map<YearMonth, BigDecimal> gastoPorMes(String idCliente) throws ClienteNoEncontradoException {
        return gastosDe(buscarCliente(idCliente)).stream()
                .sorted(Comparator.comparing(Transaccion::getFecha))
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.getFecha()),
                        LinkedHashMap::new,
                        Collectors.reducing(Config.redondear(BigDecimal.ZERO), Transaccion::getMonto, BigDecimal::add)));
    }

    public BigDecimal promedioGastoMensual(String idCliente) throws ClienteNoEncontradoException {
        Map<YearMonth, BigDecimal> porMes = gastoPorMes(idCliente);
        if (porMes.isEmpty()) {
            return Config.redondear(BigDecimal.ZERO);
        }
        BigDecimal total = porMes.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(porMes.size()), Config.ESCALA_DINERO, Config.REDONDEO_DINERO);
    }

    public String generarReporte(String idCliente) throws ClienteNoEncontradoException {
        Cliente cliente = buscarCliente(idCliente);
        Map<Categoria, BigDecimal> gastos = gastoPorCategoria(idCliente);
        Map<Categoria, BigDecimal> porcentajes = porcentajePorCategoria(idCliente);
        BigDecimal total = gastos.values().stream().reduce(Config.redondear(BigDecimal.ZERO), BigDecimal::add);

        StringBuilder reporte = new StringBuilder();
        reporte.append("Reporte de gastos de ").append(cliente.getNombre()).append('\n');
        if (gastos.isEmpty()) {
            return reporte.append("  Sin gastos registrados.\n").toString();
        }
        gastos.forEach((categoria, monto) -> reporte.append(String.format("  %-16s %10s  (%s %%)%n",
                categoria, monto.toPlainString(), porcentajes.get(categoria).toPlainString())));
        reporte.append(String.format("  %-16s %10s%n", "TOTAL", total.toPlainString()));
        categoriaMayorGasto(idCliente).ifPresent(c ->
                reporte.append("  Categoría de mayor gasto: ").append(c).append('\n'));
        gastoPorMes(idCliente).forEach((mes, monto) ->
                reporte.append("  Gasto en ").append(mes).append(": ").append(monto.toPlainString()).append('\n'));
        reporte.append("  Promedio de gasto mensual: ").append(promedioGastoMensual(idCliente).toPlainString()).append('\n');
        return reporte.toString();
    }

    private List<Transaccion> transaccionesHechasPor(Cliente cliente) {
        return cliente.getMonederos().stream()
                .flatMap(monedero -> monedero.getHistorial().stream())
                .distinct()
                .filter(t -> t.getOrigen().getPropietario().equals(cliente))
                .toList();
    }

    private List<Transaccion> gastosDe(Cliente cliente) {
        return transaccionesHechasPor(cliente).stream()
                .filter(t -> t.getEstado() == EstadoTransaccion.COMPLETADA)
                .filter(Transaccion::esGasto)
                .toList();
    }

    private Cliente buscarCliente(String idCliente) throws ClienteNoEncontradoException {
        return clienteRepositorio.buscarPorId(idCliente)
                .orElseThrow(() -> new ClienteNoEncontradoException(idCliente));
    }
}
