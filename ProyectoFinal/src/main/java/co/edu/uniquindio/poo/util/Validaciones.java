package co.edu.uniquindio.poo.util;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.excepcion.MontoInvalidoException;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class Validaciones {
    private static final Pattern PATRON_CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private Validaciones() {
    }

    public static <T> T noNulo(T valor, String campo) throws DatoInvalidoException {
        if (valor == null) {
            throw new DatoInvalidoException(campo, "no puede ser nulo");
        }
        return valor;
    }

    public static String textoObligatorio(String valor, String campo) throws DatoInvalidoException {
        if (valor == null || valor.isBlank()) {
            throw new DatoInvalidoException(campo, "es obligatorio");
        }
        return valor.trim();
    }

    public static String correo(String valor) throws DatoInvalidoException {
        String limpio = textoObligatorio(valor, "correo");
        if (!PATRON_CORREO.matcher(limpio).matches()) {
            throw new DatoInvalidoException("correo", "no tiene un formato válido");
        }
        return limpio;
    }

    public static BigDecimal montoPositivo(BigDecimal monto) throws MontoInvalidoException {
        if (monto == null) {
            throw new MontoInvalidoException("El monto no puede ser nulo");
        }
        BigDecimal redondeado = Config.redondear(monto);
        if (redondeado.signum() <= 0) {
            throw new MontoInvalidoException("El monto debe ser mayor que cero (recibido: " + monto.toPlainString() + ")");
        }
        return redondeado;
    }
}
