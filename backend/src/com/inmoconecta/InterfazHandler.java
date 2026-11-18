package com.inmoconecta;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Solo publica los archivos de la interfaz; nunca el repositorio ni sus secretos. */
public final class InterfazHandler implements HttpHandler {
    private static final Map<String, String> ARCHIVOS = Map.ofEntries(
        Map.entry("/", "index.html"), Map.entry("/index.html", "index.html"),
        Map.entry("/registro.html", "registro.html"),
        Map.entry("/login.html", "login.html"), Map.entry("/cuenta.html", "cuenta.html"),
        Map.entry("/js/acceso.js", "js/acceso.js"),
        Map.entry("/css/inicio.css", "css/inicio.css"), Map.entry("/css/styles.css", "css/styles.css"),
        Map.entry("/js/inicio.js", "js/inicio.js"), Map.entry("/js/registro.js", "js/registro.js"),
        Map.entry("/assets/casa.svg", "assets/casa.svg"),
        Map.entry("/assets/apartamento.svg", "assets/apartamento.svg"),
        Map.entry("/assets/interior.svg", "assets/interior.svg"));
    private final Path raiz;
    public InterfazHandler(Path raiz) { this.raiz = raiz.toAbsolutePath().normalize(); }

    @Override public void handle(HttpExchange s) throws IOException {
        try {
            String archivo = ARCHIVOS.get(s.getRequestURI().getRawPath());
            if (archivo == null) { error(s, 404, "Ruta no encontrada."); return; }
            if (!s.getRequestMethod().equals("GET") && !s.getRequestMethod().equals("HEAD")) {
                s.getResponseHeaders().set("Allow", "GET, HEAD");
                error(s, 405, "Método no permitido."); return;
            }
            Path destino = raiz.resolve(archivo);
            if (!Files.isRegularFile(destino) || !destino.toRealPath().startsWith(raiz.toRealPath())) {
                error(s, 404, "Archivo no encontrado."); return;
            }
            String tipo = archivo.endsWith(".html") ? "text/html; charset=utf-8"
                : archivo.endsWith(".css") ? "text/css; charset=utf-8"
                : archivo.endsWith(".js") ? "text/javascript; charset=utf-8" : "image/svg+xml";
            s.getResponseHeaders().set("Content-Type", tipo);
            s.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
            s.getResponseHeaders().set("Cache-Control", "no-store");
            s.getResponseHeaders().set("Content-Security-Policy", "default-src 'self'; img-src 'self'; script-src 'self'; style-src 'self'; connect-src 'self'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'");
            if (s.getRequestMethod().equals("HEAD")) { s.sendResponseHeaders(200, -1); return; }
            byte[] bytes = Files.readAllBytes(destino);
            s.sendResponseHeaders(200, bytes.length);
            s.getResponseBody().write(bytes);
        } finally { s.close(); }
    }
    private static void error(HttpExchange s, int codigo, String texto) throws IOException {
        byte[] bytes = texto.getBytes(StandardCharsets.UTF_8);
        s.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        if (s.getRequestMethod().equals("HEAD")) { s.sendResponseHeaders(codigo, -1); return; }
        s.sendResponseHeaders(codigo, bytes.length);
        s.getResponseBody().write(bytes);
    }
}
