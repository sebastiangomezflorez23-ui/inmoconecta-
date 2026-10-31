package com.inmoconecta;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/** Base académica HTTP: todavía no registra usuarios ni conecta MySQL. */
public class Servidor {
    public static void main(String[] args) throws IOException {
        int puerto = args.length == 0 ? 8080 : Integer.parseInt(args[0]);
        if (puerto < 1 || puerto > 65535) {
            throw new IllegalArgumentException("Puerto permitido: 1 a 65535.");
        }
        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", puerto), 0);
        servidor.createContext("/", Servidor::atender);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> servidor.stop(0)));
        servidor.start();
        System.out.println("Servidor activo: http://127.0.0.1:" + puerto + "/api/estado");
        System.out.println("Registro y MySQL pendientes. Para detener: Ctrl+C.");
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
                responder(solicitud, 200, "{\"estado\":\"activo\",\"registroDisponible\":false}");
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
