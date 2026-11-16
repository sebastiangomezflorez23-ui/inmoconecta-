package com.inmoconecta;

import com.inmoconecta.registro.RegistroHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashSet;

/** Fixture de pruebas: HTTP real y almacenamiento temporal, nunca MySQL. */
public final class ServidorPruebaInterfaz {
    public static void main(String[] args) throws Exception {
        var correos = new HashSet<String>();
        var servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", new InterfazHandler(Path.of("frontend")));
        servidor.createContext("/api/usuarios", new RegistroHandler((datos, hash) -> {
            if (datos.correo().equals("fallo@example.com")) throw new SQLException("fallo de prueba");
            if (!correos.add(datos.correo())) throw new SQLException("duplicado", "23000", 1062);
            return correos.size();
        }));
        servidor.start();
        System.out.println(servidor.getAddress().getPort());
    }
}
