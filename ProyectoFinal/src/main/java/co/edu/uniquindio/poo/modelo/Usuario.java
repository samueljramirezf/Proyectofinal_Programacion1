package co.edu.uniquindio.poo.modelo;

import co.edu.uniquindio.poo.excepcion.DatoInvalidoException;
import co.edu.uniquindio.poo.util.Validaciones;

import java.util.Objects;

public abstract class Usuario {
    private final String identificacion;
    private final String nombre;
    private final String correo;
    private final String telefono;
    private final String contrasena;

    protected Usuario(String identificacion, String nombre, String correo, String telefono, String contrasena)
            throws DatoInvalidoException {
        this.identificacion = Validaciones.textoObligatorio(identificacion, "identificación");
        this.nombre = Validaciones.textoObligatorio(nombre, "nombre");
        this.correo = Validaciones.correo(correo);
        this.telefono = Validaciones.textoObligatorio(telefono, "teléfono");
        this.contrasena = Validaciones.textoObligatorio(contrasena, "contraseña");
    }

    public abstract String getRol();

    public boolean verificarContrasena(String intento) {
        return contrasena.equals(intento);
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTelefono() {
        return telefono;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        return identificacion.equals(((Usuario) o).identificacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identificacion);
    }

    @Override
    public String toString() {
        return getRol() + " " + nombre + " (" + identificacion + ")";
    }
}
