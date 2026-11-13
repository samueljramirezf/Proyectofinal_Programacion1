package co.edu.uniquindio.poo.util;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Reloj {
    private LocalDateTime ahora;

    public Reloj() {
        this(Config.FECHA_INICIO_RELOJ);
    }

    public Reloj(LocalDateTime inicio) {
        this.ahora = Objects.requireNonNull(inicio, "La fecha inicial del reloj no puede ser nula");
    }

    public LocalDateTime ahora() {
        return ahora;
    }

    public LocalDate hoy() {
        return ahora.toLocalDate();
    }

    public void avanzarDias(int dias) throws DatoInvalidoException {
        if (dias < 0) {
            throw new DatoInvalidoException("días", "el reloj no puede retroceder");
        }
        ahora = ahora.plusDays(dias);
    }
}
