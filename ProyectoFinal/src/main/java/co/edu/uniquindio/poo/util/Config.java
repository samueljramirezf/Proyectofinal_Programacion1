package co.edu.uniquindio.poo.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public final class Config {
    private Config() {
    }

    public static final LocalDateTime FECHA_INICIO_RELOJ = LocalDateTime.of(2026, 11, 1, 8, 0);

    public static final int ESCALA_DINERO = 2;

    public static final RoundingMode REDONDEO_DINERO = RoundingMode.HALF_EVEN;

    public static final BigDecimal UNIDADES_POR_BLOQUE_PUNTOS = new BigDecimal("100");
    public static final int PUNTOS_POR_BLOQUE_DEPOSITO = 1;
    public static final int PUNTOS_POR_BLOQUE_RETIRO = 2;
    public static final int PUNTOS_POR_BLOQUE_TRANSFERENCIA = 3;

    public static final BigDecimal COMISION_DEPOSITO = new BigDecimal("0.00");
    public static final BigDecimal COMISION_RETIRO = new BigDecimal("0.01");
    public static final BigDecimal COMISION_TRANSFERENCIA = new BigDecimal("0.02");

    public static final BigDecimal DESCUENTO_RANGO_BRONCE = new BigDecimal("0.00");
    public static final BigDecimal DESCUENTO_RANGO_PLATA = new BigDecimal("0.05");
    public static final BigDecimal DESCUENTO_RANGO_ORO = new BigDecimal("0.10");
    public static final BigDecimal DESCUENTO_RANGO_PLATINO = new BigDecimal("0.20");

    public static final int COSTO_DESCUENTO_TRANSFERENCIA = 100;
    public static final int COSTO_RETIROS_SIN_COMISION = 500;
    public static final int COSTO_BONO_SALDO = 1000;

    public static final BigDecimal DESCUENTO_BENEFICIO_TRANSFERENCIA = new BigDecimal("0.10");

    public static final int DIAS_RETIROS_SIN_COMISION = 30;

    public static final BigDecimal MONTO_BONO_SALDO = new BigDecimal("50.00");

    public static final BigDecimal UMBRAL_SALDO_BAJO = new BigDecimal("500.00");

    public static final int MINIMO_TRANSACCIONES_ANALISIS = 5;

    public static final BigDecimal FACTOR_DESVIACION_ATIPICA = new BigDecimal("2");

    public static BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(ESCALA_DINERO, REDONDEO_DINERO);
    }
}
