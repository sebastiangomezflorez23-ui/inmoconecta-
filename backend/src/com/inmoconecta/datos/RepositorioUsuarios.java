package com.inmoconecta.datos;

import com.inmoconecta.registro.DatosRegistro;
import java.sql.SQLException;

@FunctionalInterface
public interface RepositorioUsuarios {
    long insertar(DatosRegistro datos, String hash) throws SQLException;
}
