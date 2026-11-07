package com.inmoconecta.modelo;

/**
 * Representa un usuario ya persistido. No es el cuerpo JSON de registro.
 * La validación, normalización y generación del hash corresponden a T09.
 * No serializar esta clase completa en las respuestas HTTP.
 */
public record Usuario(
        long id,
        String nombre,
        String apellido,
        String telefono,
        String correo,
        String contrasenaHash,
        Rol rol) {

    // El toString automático de un record incluiría el hash y datos personales.
    @Override
    public String toString() {
        return "Usuario[id=" + id + ", rol=" + rol + "]";
    }
}
