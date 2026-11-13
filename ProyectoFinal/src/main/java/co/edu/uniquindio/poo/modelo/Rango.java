package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.util.Config;

import java.math.BigDecimal;
import java.util.Arrays;

public enum Rango {
    BRONCE(0, 500, Config.DESCUENTO_RANGO_BRONCE),
    PLATA(501, 1000, Config.DESCUENTO_RANGO_PLATA),
    ORO(1001, 5000, Config.DESCUENTO_RANGO_ORO),
    PLATINO(5001, Integer.MAX_VALUE, Config.DESCUENTO_RANGO_PLATINO);

    private final int puntosMinimos;
    private final int puntosMaximos;

    private final BigDecimal descuentoTransferencia;

    Rango(int puntosMinimos, int puntosMaximos, BigDecimal descuentoTransferencia) {
        this.puntosMinimos = puntosMinimos;
        this.puntosMaximos = puntosMaximos;
        this.descuentoTransferencia = descuentoTransferencia;
    }

    public static Rango desdePuntos(int puntos) {
        if (puntos < 0) {
            throw new IllegalArgumentException("Los puntos acumulados no pueden ser negativos: " + puntos);
        }
        return Arrays.stream(values())
                .filter(rango -> rango.contiene(puntos))
                .findFirst()
                .orElseThrow();
    }

    public boolean contiene(int puntos) {
        return puntos >= puntosMinimos && puntos <= puntosMaximos;
    }

    public int getPuntosMinimos() {
        return puntosMinimos;
    }

    public int getPuntosMaximos() {
        return puntosMaximos;
    }

    public BigDecimal getDescuentoTransferencia() {
        return descuentoTransferencia;
    }
}
