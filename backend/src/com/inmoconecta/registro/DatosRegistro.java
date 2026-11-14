package com.inmoconecta.registro;

/** Datos internos: nunca serializar ni registrar este objeto. */
public record DatosRegistro(String nombre, String apellido, String telefono,
                            String correo, String contrasena, String rol) {
    @Override public String toString() { return "DatosRegistro[datos ocultos]"; }
}
