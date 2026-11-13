package co.edu.uniquindio.poo.repositorio;

import co.edu.uniquindio.poo.modelo.Cliente;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ClienteRepositorio {
    private final Map<String, Cliente> clientes = new LinkedHashMap<>();

    public void guardar(Cliente cliente) {
        clientes.put(cliente.getIdentificacion(), cliente);
    }

    public Optional<Cliente> buscarPorId(String identificacion) {
        return Optional.ofNullable(clientes.get(identificacion));
    }

    public List<Cliente> listar() {
        return List.copyOf(clientes.values());
    }

    public boolean existe(String identificacion) {
        return clientes.containsKey(identificacion);
    }
}
