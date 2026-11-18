package com.inmoconecta;

import com.inmoconecta.acceso.*;
import com.inmoconecta.seguridad.HashContrasena;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

/** SOLO pruebas: repositorio simulado. No verifica SQL, permisos ni persistencia MySQL. */
public final class ServidorPruebaAcceso {
    public static final class Memoria implements RepositorioAcceso {
        private final String hash = HashContrasena.generar("Frase ficticia de acceso");
        private final Map<String, PoliticaAcceso.Estado> estados = new HashMap<>();
        private final Map<String, Usuario> sesiones = new HashMap<>();
        public synchronized Usuario autenticar(String correo, String clave, String tokenHash) throws SQLException {
            if (correo.equals("fallo@example.com")) throw new SQLException("secreto interno de prueba");
            String rol = switch(correo) { case "comprador@example.com" -> "COMPRADOR"; case "agente@example.com" -> "AGENTE"; case "admin@example.com" -> "ADMIN"; default -> null; };
            boolean coincide = HashContrasena.verificar(clave, hash);
            if (rol == null) return null;
            var ahora = LocalDateTime.now();
            var estado = estados.getOrDefault(correo, new PoliticaAcceso.Estado(0, null));
            if (estado.bloqueado(ahora)) return null;
            if (!coincide) { estados.put(correo, estado.fallar(ahora)); return null; }
            estados.put(correo, new PoliticaAcceso.Estado(0, null));
            var usuario = new Usuario(rol.equals("COMPRADOR") ? 1 : rol.equals("AGENTE") ? 2 : 3, "Ana Prueba", rol);
            sesiones.put(tokenHash, usuario); return usuario;
        }
        public synchronized Usuario consultar(String tokenHash) { return sesiones.get(tokenHash); }
    }
    public static void main(String[] args) throws Exception {
        var servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var acceso = new AccesoHandler(new Memoria(), "http://127.0.0.1:" + servidor.getAddress().getPort());
        servidor.createContext("/api/login", acceso); servidor.createContext("/api/sesion", acceso);
        servidor.createContext("/", new InterfazHandler(Path.of("frontend")));
        servidor.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(4));
        servidor.start(); System.out.println(servidor.getAddress().getPort());
    }
}
