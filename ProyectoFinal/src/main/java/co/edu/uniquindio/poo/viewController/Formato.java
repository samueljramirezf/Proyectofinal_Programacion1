package co.edu.uniquindio.poo.viewController;

import co.edu.uniquindio.poo.modelo.Monedero;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public final class Formato {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Formato() {
    }

    public static String dinero(BigDecimal valor) {
        return "$ " + valor.toPlainString();
    }

    public static String fecha(LocalDate fecha) {
        return fecha.format(FECHA);
    }

    public static String fechaHora(LocalDateTime fecha) {
        return fecha.format(FECHA_HORA);
    }

    public static StringConverter<Monedero> monederoConSaldo() {
        return conversor(m -> m.getId() + " · " + m.getNombre() + " (" + dinero(m.getSaldo()) + ")");
    }

    public static StringConverter<Monedero> monederoConPropietario() {
        return conversor(m -> m.getId() + " · " + m.getNombre() + " — " + m.getPropietario().getNombre());
    }

    private static StringConverter<Monedero> conversor(Function<Monedero, String> texto) {
        return new StringConverter<>() {
            @Override
            public String toString(Monedero monedero) {
                return monedero == null ? "" : texto.apply(monedero);
            }

            @Override
            public Monedero fromString(String cadena) {
                return null;
            }
        };
    }
}
