package com.inmoconecta.datos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Conexión de desarrollo local; las credenciales no forman parte de la URL. */
public final class ConexionBD {
    private ConexionBD() { }

    public static Connection abrir() throws SQLException {
        return abrir(variableObligatoria("DB_PASSWORD"));
    }

    public static Connection abrir(String password) throws SQLException {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Falta la contraseña de MySQL.");
        }
        Properties opciones = new Properties();
        opciones.setProperty("user", variableObligatoria("DB_USER"));
        opciones.setProperty("password", password);
        opciones.setProperty("sslMode", "REQUIRED");
        opciones.setProperty("connectTimeout", "5000");
        opciones.setProperty("socketTimeout", "5000");
        return DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/inmoconecta", opciones);
    }

    private static String variableObligatoria(String nombre) {
        String valor = System.getenv(nombre);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Configura la variable " + nombre + ".");
        }
        return valor;
    }
}
