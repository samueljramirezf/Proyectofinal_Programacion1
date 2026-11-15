package co.edu.uniquindio.poo;

import co.edu.uniquindio.poo.excepcion.MonederoException;
import co.edu.uniquindio.poo.modelo.Categoria;
import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Frecuencia;
import co.edu.uniquindio.poo.modelo.Monedero;
import co.edu.uniquindio.poo.modelo.TipoMonedero;
import co.edu.uniquindio.poo.notificacion.Notificador;
import co.edu.uniquindio.poo.notificacion.NotificadorGmail;
import co.edu.uniquindio.poo.repositorio.ClienteRepositorio;
import co.edu.uniquindio.poo.repositorio.TransaccionRepositorio;
import co.edu.uniquindio.poo.servicio.AlertasServicio;
import co.edu.uniquindio.poo.servicio.AnalisisServicio;
import co.edu.uniquindio.poo.servicio.MonederoServicio;
import co.edu.uniquindio.poo.servicio.ProgramadasServicio;
import co.edu.uniquindio.poo.servicio.PuntosServicio;
import co.edu.uniquindio.poo.util.Reloj;

import java.math.BigDecimal;
import java.util.List;

public class ContextoApp {
    private final Reloj reloj;
    private final MonederoServicio monederoServicio;
    private final PuntosServicio puntosServicio;
    private final AnalisisServicio analisisServicio;
    private final AlertasServicio alertasServicio;
    private final ProgramadasServicio programadasServicio;
    private final boolean correoActivo;

    private Cliente clienteActual;

    public ContextoApp() {
        reloj = new Reloj();
        ClienteRepositorio clientes = new ClienteRepositorio();
        TransaccionRepositorio transacciones = new TransaccionRepositorio();

        String correo = System.getenv("MONEDERO_CORREO");
        String clave = System.getenv("MONEDERO_CLAVE_CORREO");
        correoActivo = correo != null && clave != null;
        List<Notificador> notificadores = correoActivo
                ? List.of(new NotificadorGmail(correo, clave))
                : List.of();
        alertasServicio = new AlertasServicio(clientes, reloj, notificadores);
        analisisServicio = new AnalisisServicio(clientes, alertasServicio);
        puntosServicio = new PuntosServicio(clientes, reloj);
        monederoServicio = new MonederoServicio(clientes, transacciones, puntosServicio, analisisServicio,
                alertasServicio, reloj);
        programadasServicio = new ProgramadasServicio(clientes, monederoServicio, alertasServicio, reloj);
    }

    public void precargarDatos() throws MonederoException {
        monederoServicio.registrarCliente("1001", "Ana Gómez", "ana@correo.com", "3001112233", "clave1");
        monederoServicio.registrarCliente("2002", "Luis Pérez", "luis@correo.com", "3004445566", "clave2");
        monederoServicio.registrarCliente("3003", "María Ruiz", "maria@correo.com", "3007778899", "clave3");
        monederoServicio.registrarCliente("4004", "Carlos Díaz", "carlos@correo.com", "3001234567", "clave4");
        Monedero anaAhorros = monederoServicio.crearMonedero("1001", "Ahorros", TipoMonedero.AHORROS);
        Monedero anaGastos = monederoServicio.crearMonedero("1001", "Gastos", TipoMonedero.GASTOS_DIARIOS);
        Monedero luisGastos = monederoServicio.crearMonedero("2002", "Gastos", TipoMonedero.GASTOS_DIARIOS);
        Monedero mariaAhorros = monederoServicio.crearMonedero("3003", "Ahorros", TipoMonedero.AHORROS);
        Monedero carlosPrincipal = monederoServicio.crearMonedero("4004", "Principal", TipoMonedero.AHORROS);

        for (String monto : new String[]{"100", "200", "100", "200", "100"}) {
            monederoServicio.depositar(anaAhorros.getId(), new BigDecimal(monto), "Depósito");
        }
        monederoServicio.depositar(anaAhorros.getId(), new BigDecimal("300"), "Depósito grande (atípico)");
        monederoServicio.depositar(luisGastos.getId(), new BigDecimal("1500"), "Sueldo");
        monederoServicio.depositar(mariaAhorros.getId(), new BigDecimal("800"), "Ahorro inicial");

        for (int i = 0; i < 4; i++) {
            monederoServicio.depositar(carlosPrincipal.getId(), new BigDecimal("40000"), "Venta");
        }

        reloj.avanzarDias(3);
        monederoServicio.retirar(anaAhorros.getId(), new BigDecimal("150"), Categoria.COMIDA, "Mercado");
        monederoServicio.retirar(luisGastos.getId(), new BigDecimal("80"), Categoria.TRANSPORTE, "Taxi");

        reloj.avanzarDias(4);
        monederoServicio.transferir(anaAhorros.getId(), luisGastos.getId(), new BigDecimal("200"),
                Categoria.TRANSPORTE, "Pago viaje");
        monederoServicio.transferir(anaAhorros.getId(), anaGastos.getId(), new BigDecimal("100"),
                Categoria.OTROS, "A mi monedero de gastos");
        monederoServicio.transferir(luisGastos.getId(), mariaAhorros.getId(), new BigDecimal("300"),
                Categoria.SERVICIOS, "Arriendo");

        programadasServicio.programarDeposito(anaGastos.getId(), new BigDecimal("50"), Frecuencia.SEMANAL,
                reloj.hoy().plusDays(2), "Mesada semanal");
        programadasServicio.programarTransferencia(anaGastos.getId(), luisGastos.getId(), new BigDecimal("300"),
                Categoria.ENTRETENIMIENTO, Frecuencia.MENSUAL, reloj.hoy().plusDays(4), "Cuota (no le alcanza)");
    }

    public void iniciarSesion(Cliente cliente) {
        this.clienteActual = cliente;
    }

    public void cerrarSesion() {
        this.clienteActual = null;
    }

    public Cliente getClienteActual() {
        return clienteActual;
    }

    public boolean isCorreoActivo() {
        return correoActivo;
    }

    public Reloj getReloj() {
        return reloj;
    }

    public MonederoServicio getMonederoServicio() {
        return monederoServicio;
    }

    public PuntosServicio getPuntosServicio() {
        return puntosServicio;
    }

    public AnalisisServicio getAnalisisServicio() {
        return analisisServicio;
    }

    public AlertasServicio getAlertasServicio() {
        return alertasServicio;
    }

    public ProgramadasServicio getProgramadasServicio() {
        return programadasServicio;
    }
}
