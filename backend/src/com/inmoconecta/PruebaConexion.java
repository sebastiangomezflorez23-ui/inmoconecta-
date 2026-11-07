package com.inmoconecta;

import com.inmoconecta.datos.ConexionBD;
import java.io.Console;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/** Consulta de solo lectura: no crea ni modifica usuarios. */
public final class PruebaConexion {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String password = System.getenv("DB_PASSWORD");
            if (password == null || password.isEmpty()) {
                Console consola = System.console();
                if (consola == null) {
                    throw new IllegalArgumentException(
                            "Usa una terminal interactiva o configura DB_PASSWORD localmente.");
                }
                char[] secreto = consola.readPassword("Contraseña MySQL (no se muestra): ");
                if (secreto == null) throw new IllegalArgumentException("Prueba cancelada.");
                password = new String(secreto);
                Arrays.fill(secreto, '\0');
            }
            try (Connection conexion = ConexionBD.abrir(password);
                 PreparedStatement consulta = conexion.prepareStatement(
                         "SELECT COUNT(*) FROM usuarios");
                 ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) throw new SQLException("Sin resultado");
                System.out.println("Conexión JDBC correcta. Base: " + conexion.getCatalog());
                System.out.println("Usuarios existentes: " + resultado.getLong(1));
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Falta MySQL Connector/J en backend/lib o en el classpath.");
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (SQLException e) {
            System.err.println("No se pudo consultar MySQL. Comprueba servicio, credenciales y tabla.");
            System.err.println("Código MySQL: " + e.getErrorCode() + "; SQLState: " + e.getSQLState());
            System.exit(1);
        }
    }
}
