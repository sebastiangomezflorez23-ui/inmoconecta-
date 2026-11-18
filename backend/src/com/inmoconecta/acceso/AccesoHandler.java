package com.inmoconecta.acceso;

import com.google.gson.Gson;
import com.google.gson.Strictness;
import com.google.gson.stream.*;
import com.sun.net.httpserver.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.security.*;
import java.util.*;

/** Autenticación local: la aplicación se sirve desde el mismo origen fijo. */
public final class AccesoHandler implements HttpHandler {
    private static final Gson JSON = new Gson();
    private static final SecureRandom AZAR = new SecureRandom();
    private static final String COOKIE = "inmoconecta_sesion";
    private final RepositorioAcceso repositorio;
    private final String origen;
    public AccesoHandler(RepositorioAcceso repositorio, String origen) {
        this.repositorio = repositorio; this.origen = origen;
    }
    @Override public void handle(HttpExchange s) throws IOException {
        try {
            String ruta = s.getRequestURI().getRawPath();
            boolean login = ruta.equals("/api/login");
            if (!login && !ruta.equals("/api/sesion")) { error(s, 404, "RUTA_NO_ENCONTRADA", "Ruta no encontrada."); return; }
            String metodo = login ? "POST" : "GET";
            if (!s.getRequestMethod().equals(metodo)) {
                s.getResponseHeaders().set("Allow", metodo); error(s, 405, "METODO_NO_PERMITIDO", "Método no permitido."); return;
            }
            var origenes = s.getRequestHeaders().get("Origin");
            if ((login && origenes == null) || (origenes != null && (origenes.size() != 1 || !origen.equals(origenes.getFirst())))
                    || "cross-site".equals(s.getRequestHeaders().getFirst("Sec-Fetch-Site"))) {
                error(s, 403, "ORIGEN_NO_PERMITIDO", "Abre la aplicación desde su dirección local."); return;
            }
            if (!login) {
                String token = token(s);
                var usuario = token == null ? null : repositorio.consultar(huella(token));
                if (usuario == null) { error(s, 401, "SESION_NO_DISPONIBLE", "Inicia sesión para continuar."); return; }
                responder(s, 200, Map.of("usuario", usuario)); return;
            }
            String tipo = s.getRequestHeaders().getFirst("Content-Type");
            if (tipo == null || !tipo.matches("(?i)^application/json(?:\\s*;\\s*charset\\s*=\\s*(?:utf-8|\"utf-8\"))?\\s*$")) {
                error(s, 415, "TIPO_CONTENIDO_NO_ADMITIDO", "Envía JSON UTF-8."); return;
            }
            byte[] bytes = s.getRequestBody().readNBytes(4097);
            if (bytes.length > 4096) { error(s, 413, "CUERPO_DEMASIADO_GRANDE", "Máximo 4 KiB."); return; }
            Map<String, String> datos;
            try { datos = leer(bytes); }
            catch (IOException | IllegalArgumentException | IllegalStateException e) {
                error(s, 400, "DATOS_INVALIDOS", "Escribe un correo válido y tu contraseña (máximo 128 caracteres)."); return;
            }
            byte[] aleatorio = new byte[32]; AZAR.nextBytes(aleatorio);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(aleatorio);
            var usuario = repositorio.autenticar(datos.get("correo"), datos.get("contrasena"), huella(token));
            if (usuario == null) {
                error(s, 401, "ACCESO_NO_DISPONIBLE", "No se pudo iniciar sesión. Revisa tus datos. Tras tres fallos, espera "
                        + PoliticaAcceso.BLOQUEO_MINUTOS + " minutos antes de intentar de nuevo."); return;
            }
            s.getResponseHeaders().set("Set-Cookie", COOKIE + "=" + token + "; Path=/; HttpOnly; SameSite=Strict; Max-Age="
                    + PoliticaAcceso.SESION_MINUTOS * 60 + (origen.startsWith("https://") ? "; Secure" : ""));
            responder(s, 200, Map.of("codigo", "SESION_INICIADA", "usuario", usuario));
        } catch (java.sql.SQLException | RuntimeException e) {
            error(s, 500, "ERROR_INTERNO", "No se pudo completar el acceso. Inténtalo más tarde.");
        } finally { s.close(); }
    }

    private static Map<String, String> leer(byte[] bytes) throws IOException {
        String texto = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
        Map<String, String> datos = new HashMap<>();
        try (var r = new JsonReader(new StringReader(texto))) {
            r.setStrictness(Strictness.STRICT); r.beginObject();
            while (r.hasNext()) {
                String campo = r.nextName();
                if (!Set.of("correo", "contrasena").contains(campo) || datos.containsKey(campo) || r.peek() != JsonToken.STRING)
                    throw new IllegalArgumentException();
                datos.put(campo, r.nextString());
            }
            r.endObject();
            if (r.peek() != JsonToken.END_DOCUMENT || datos.size() != 2) throw new IllegalArgumentException();
        }
        String correo = datos.get("correo").strip().toLowerCase(Locale.ROOT), clave = datos.get("contrasena");
        if (correo.length() > 254 || !correo.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+") || clave.isEmpty() || clave.length() > 128)
            throw new IllegalArgumentException();
        datos.put("correo", correo); return datos;
    }
    private static String token(HttpExchange s) {
        String encontrado = null;
        for (String cabecera : s.getRequestHeaders().getOrDefault("Cookie", List.of())) {
            for (String parte : cabecera.split(";")) {
                String[] par = parte.strip().split("=", 2);
                if (par[0].equals(COOKIE)) {
                    if (encontrado != null || par.length != 2 || !par[1].matches("[A-Za-z0-9_-]{43}")) return null;
                    encontrado = par[1];
                }
            }
        }
        return encontrado;
    }
    public static String huella(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.US_ASCII))); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    private static void error(HttpExchange s, int codigo, String clave, String mensaje) throws IOException {
        responder(s, codigo, Map.of("codigo", clave, "mensaje", mensaje));
    }
    private static void responder(HttpExchange s, int codigo, Object valor) throws IOException {
        byte[] bytes = JSON.toJson(valor).getBytes(StandardCharsets.UTF_8);
        s.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        s.getResponseHeaders().set("Cache-Control", "no-store");
        s.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        if (s.getRequestMethod().equals("HEAD")) { s.sendResponseHeaders(codigo, -1); return; }
        s.sendResponseHeaders(codigo, bytes.length); s.getResponseBody().write(bytes);
    }
}
