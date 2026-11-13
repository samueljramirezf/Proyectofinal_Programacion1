package co.edu.uniquindio.poo.repositorio;

import co.edu.uniquindio.poo.modelo.Transaccion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TransaccionRepositorio {
    private final Map<String, Transaccion> transacciones = new LinkedHashMap<>();

    public void guardar(Transaccion transaccion) {
        transacciones.put(transaccion.getId(), transaccion);
    }

    public Optional<Transaccion> buscarPorId(String id) {
        return Optional.ofNullable(transacciones.get(id));
    }

    public List<Transaccion> listar() {
        return List.copyOf(transacciones.values());
    }
}
