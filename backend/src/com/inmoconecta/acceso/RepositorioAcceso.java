package com.inmoconecta.acceso;

import java.sql.SQLException;

public interface RepositorioAcceso {
    record Usuario(long id, String nombre, String rol) { }
    Usuario autenticar(String correo, String clave, String tokenHash) throws SQLException;
    Usuario consultar(String tokenHash) throws SQLException;
}
