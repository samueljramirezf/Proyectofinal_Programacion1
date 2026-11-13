package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.util.Validaciones;

import java.time.LocalDate;

public class Beneficio {
    private static int contadorIds = 1;

    private final String id;
    private final TipoBeneficio tipo;
    private final LocalDate fechaCanje;
    private LocalDate fechaVencimiento;
    private boolean usado;

    public Beneficio(TipoBeneficio tipo, LocalDate fechaCanje) throws DatoInvalidoException {
        this.tipo = Validaciones.noNulo(tipo, "tipo de beneficio");
        this.fechaCanje = Validaciones.noNulo(fechaCanje, "fecha de canje");
        this.fechaVencimiento = tipo.getDiasVigencia() > 0
                ? fechaCanje.plusDays(tipo.getDiasVigencia())
                : null;
        this.id = "B-" + String.format("%04d", contadorIds++);
    }

    public boolean estaVigente(LocalDate hoy) {
        boolean vencido = fechaVencimiento != null && hoy.isAfter(fechaVencimiento);
        return !usado && !vencido;
    }

    public void marcarUsado() {
        if (!tipo.isUsoUnico()) {
            throw new IllegalStateException("El beneficio " + tipo + " es por tiempo, no se consume");
        }
        if (usado) {
            throw new IllegalStateException("El beneficio " + id + " ya fue usado");
        }
        usado = true;
    }

    public void extenderVigencia(int dias) {
        if (fechaVencimiento == null) {
            throw new IllegalStateException("El beneficio " + tipo + " no vence por tiempo");
        }
        if (dias <= 0) {
            throw new IllegalArgumentException("Los días a extender deben ser positivos: " + dias);
        }
        fechaVencimiento = fechaVencimiento.plusDays(dias);
    }

    public String getId() {
        return id;
    }

    public TipoBeneficio getTipo() {
        return tipo;
    }

    public LocalDate getFechaCanje() {
        return fechaCanje;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public boolean isUsado() {
        return usado;
    }

    @Override
    public String toString() {
        return id + " " + tipo.getDescripcion()
                + (fechaVencimiento != null ? " (vence " + fechaVencimiento + ")" : "")
                + (usado ? " [USADO]" : "");
    }
}
