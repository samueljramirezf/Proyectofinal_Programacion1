package co.edu.uniquindio.poo.test;

import co.edu.uniquindio.poo.modelo.Cliente;
import co.edu.uniquindio.poo.modelo.Categoria;
import co.edu.uniquindio.poo.modelo.TipoMonedero;
import co.edu.uniquindio.poo.servicio.MonederoServicio;
import co.edu.uniquindio.poo.servicio.PuntosServicio;
import co.edu.uniquindio.poo.servicio.AnalisisServicio;
import co.edu.uniquindio.poo.servicio.AlertasServicio;
import co.edu.uniquindio.poo.util.Reloj;
import co.edu.uniquindio.poo.repositorio.ClienteRepositorio;
import co.edu.uniquindio.poo.repositorio.TransaccionRepositorio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MonederoServicioTest {

    private MonederoServicio monederoServicio;
    private String idCliente;
    private String idMonedero;
    private String idSegundoCliente;
    private String idSegundoMonedero;

    @BeforeEach
    void setUp() throws Exception {

        ClienteRepositorio clienteRepositorio = new ClienteRepositorio();
        TransaccionRepositorio transaccionRepositorio = new TransaccionRepositorio();

        Reloj reloj = new Reloj(LocalDateTime.of(2026, 11, 1, 8, 0));

        AlertasServicio alertasServicio =
                new AlertasServicio(clienteRepositorio, reloj, List.of());

        PuntosServicio puntosServicio =
                new PuntosServicio(clienteRepositorio, reloj);

        AnalisisServicio analisisServicio =
                new AnalisisServicio(clienteRepositorio, alertasServicio);

        monederoServicio = new MonederoServicio(
                clienteRepositorio,
                transaccionRepositorio,
                puntosServicio,
                analisisServicio,
                alertasServicio,
                reloj
        );

        Cliente cliente = monederoServicio.registrarCliente(
                "123456789",
                "Cliente Uno",
                "cliente1@correo.com",
                "3001234567",
                "Clave123"
        );

        idCliente = cliente.getIdentificacion();

        monederoServicio.crearMonedero(
                idCliente,
                "Monedero principal",
                TipoMonedero.AHORROS
        );

        idMonedero = cliente.getMonederos().get(0).getId();

        Cliente segundoCliente = monederoServicio.registrarCliente(
                "987654321",
                "Cliente Dos",
                "cliente2@correo.com",
                "3007654321",
                "Clave123"
        );

        idSegundoCliente = segundoCliente.getIdentificacion();

        monederoServicio.crearMonedero(
                idSegundoCliente,
                "Monedero secundario",
                TipoMonedero.AHORROS
        );

        idSegundoMonedero = segundoCliente.getMonederos().get(0).getId();
    }

    @Test
    void testDepositar() throws Exception {

        monederoServicio.depositar(
                idMonedero,
                new BigDecimal("50000"),
                "Depósito de prueba"
        );

        assertEquals(
                0,
                new BigDecimal("50000.00").compareTo(
                        monederoServicio.consultarSaldo(idMonedero)
                )
        );

        assertEquals(1, monederoServicio.historial(idCliente).size());
    }

    @Test
    void testRetirar() throws Exception {

        monederoServicio.depositar(
                idMonedero,
                new BigDecimal("100000"),
                "Depósito inicial"
        );

        monederoServicio.retirar(
                idMonedero,
                new BigDecimal("20000"),
                Categoria.COMIDA,
                "Compra de alimentos"
        );

        assertEquals(
                0,
                new BigDecimal("79800.00").compareTo(
                        monederoServicio.consultarSaldo(idMonedero)
                )
        );

        assertEquals(2, monederoServicio.historial(idCliente).size());
    }

    @Test
    void testTransferir() throws Exception {

        monederoServicio.depositar(
                idMonedero,
                new BigDecimal("100000"),
                "Depósito inicial"
        );

        monederoServicio.transferir(
                idMonedero,
                idSegundoMonedero,
                new BigDecimal("20000"),
                Categoria.OTROS,
                "Transferencia de prueba"
        );

        System.out.println("Saldo origen: " +
                monederoServicio.consultarSaldo(idMonedero));

        System.out.println("Saldo destino: " +
                monederoServicio.consultarSaldo(idSegundoMonedero));
    }
}