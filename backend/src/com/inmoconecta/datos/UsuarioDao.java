package com.inmoconecta.datos;

import com.inmoconecta.registro.DatosRegistro;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class UsuarioDao implements RepositorioUsuarios {
    @Override public long insertar(DatosRegistro datos, String hash) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, apellido, telefono, correo, contrasena_hash, rol)"
                + " VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionBD.abrir();
             PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, datos.nombre());
            consulta.setString(2, datos.apellido());
            consulta.setString(3, datos.telefono());
            consulta.setString(4, datos.correo());
            consulta.setString(5, hash);
            consulta.setString(6, datos.rol());
            consulta.executeUpdate();
            try (ResultSet claves = consulta.getGeneratedKeys()) {
                if (!claves.next()) throw new SQLException("Identificador no disponible.");
                return claves.getLong(1);
            }
        }
    }
}
