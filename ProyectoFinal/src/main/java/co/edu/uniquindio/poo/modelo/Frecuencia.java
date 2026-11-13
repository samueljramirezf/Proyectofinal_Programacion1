package co.edu.uniquindio.poo.modelo;

import java.time.LocalDate;

public enum Frecuencia {
    DIARIA {
        @Override
        public LocalDate siguienteFecha(LocalDate desde) {
            return desde.plusDays(1);
        }
    },
    SEMANAL {
        @Override
        public LocalDate siguienteFecha(LocalDate desde) {
            return desde.plusWeeks(1);
        }
    },
    MENSUAL {
        @Override
        public LocalDate siguienteFecha(LocalDate desde) {
            return desde.plusMonths(1);
        }
    };

    public abstract LocalDate siguienteFecha(LocalDate desde);
}
