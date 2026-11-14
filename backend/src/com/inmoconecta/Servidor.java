package com.inmoconecta;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import com.inmoconecta.datos.UsuarioDao;
import com.inmoconecta.registro.RegistroHandler;
import java.util.concurrent.Executors;

/** Servidor académico local con registro JDBC. */
public class Servidor {
    public static void main(String[] args) throws IOException {
        String usuario = System.getenv("DB_USER");
        String clave = System.getenv("DB_PASSWORD");
        if (usuario == null || usuario.isBlank() || usuario.equalsIgnoreCase("root")
                || clave == null || clave.isEmpty()) {
            System.err.println("Configura DB_USER (distinto de root) y DB_PASSWORD para iniciar el registro.");
            System.exit(1);
        }
        int puerto = args.length == 0 ? 8080 : Integer.parseInt(args[0]);
        if (puerto < 1 || puerto > 65535) {
            throw new IllegalArgumentException("Puerto permitido: 1 a 65535.");
        }
        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", puerto), 0);
        servidor.createContext("/", Servidor::atender);
        servidor.createContext("/api/usuarios", new RegistroHandler(new UsuarioDao()));
        var ejecutor = Executors.newFixedThreadPool(4);
        servidor.setExecutor(ejecutor);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            servidor.stop(0);
            ejecutor.shutdownNow();
        }));
        servidor.start();
        System.out.println("Servidor activo: http://127.0.0.1:" + puerto + "/api/estado");
        System.out.println("POST /api/usuarios disponible. Requiere DB_USER y DB_PASSWORD.");
        System.out.println("Para detener: Ctrl+C.");
    }

    private static void atender(HttpExchange solicitud) throws IOException {
        try {
            // La comparación exacta evita aceptar otras rutas con el mismo prefijo.
            if (!"/api/estado".equals(solicitud.getRequestURI().getPath())) {
                responder(solicitud, 404, "{\"error\":\"Ruta no encontrada\"}");
            } else if (!"GET".equals(solicitud.getRequestMethod())) {
                solicitud.getResponseHeaders().set("Allow", "GET");
                responder(solicitud, 405, "{\"error\":\"Metodo no permitido\"}");
            } else {
                responder(solicitud, 200, "{\"estado\":\"activo\",\"registroDisponible\":true,\"baseDatosVerificada\":false}");
            }
        } finally {
            solicitud.close();
        }
    }

    private static void responder(HttpExchange solicitud, int codigo, String json) throws IOException {
        byte[] contenido = json.getBytes(StandardCharsets.UTF_8);
        solicitud.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        solicitud.getResponseHeaders().set("Cache-Control", "no-store");
        solicitud.sendResponseHeaders(codigo, contenido.length);
        solicitud.getResponseBody().write(contenido);
    }
}
