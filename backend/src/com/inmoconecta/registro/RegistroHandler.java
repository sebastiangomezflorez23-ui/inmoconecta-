package com.inmoconecta.registro;

import com.google.gson.Gson;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.inmoconecta.datos.RepositorioUsuarios;
import com.inmoconecta.seguridad.HashContrasena;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RegistroHandler implements HttpHandler {
    private static final int MAX_BYTES = 16_384;
    private static final Gson JSON = new Gson();
    private final RepositorioUsuarios repositorio;

    public RegistroHandler(RepositorioUsuarios repositorio) { this.repositorio = repositorio; }

    @Override public void handle(HttpExchange solicitud) throws IOException {
        try {
            if (!"/api/usuarios".equals(solicitud.getRequestURI().getPath())) {
                error(solicitud, 404, "RUTA_NO_ENCONTRADA", "Ruta no encontrada."); return;
            }
            if (!"POST".equals(solicitud.getRequestMethod())) {
                solicitud.getResponseHeaders().set("Allow", "POST");
                error(solicitud, 405, "METODO_NO_PERMITIDO", "Usa POST."); return;
            }
            String tipo = solicitud.getRequestHeaders().getFirst("Content-Type");
            if (tipo == null || !tipo.matches("(?i)^application/json(?:\\s*;\\s*charset\\s*=\\s*(?:utf-8|\"utf-8\"))?\\s*$")) {
                error(solicitud, 415, "TIPO_CONTENIDO_NO_ADMITIDO", "Envía JSON UTF-8."); return;
            }
            byte[] bytes = solicitud.getRequestBody().readNBytes(MAX_BYTES + 1);
            if (bytes.length > MAX_BYTES) {
                error(solicitud, 413, "CUERPO_DEMASIADO_GRANDE", "Máximo 16 KiB."); return;
            }
            Map<String, String> entrada;
            try {
                String cuerpo = StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
                        .decode(ByteBuffer.wrap(bytes)).toString();
                entrada = leer(cuerpo);
            } catch (DatosInvalidos e) {
                error(solicitud, 400, "DATOS_INVALIDOS", "Envía los seis campos de texto requeridos."); return;
            } catch (IOException | IllegalStateException e) {
                error(solicitud, 400, "JSON_INVALIDO", "El cuerpo debe ser un objeto JSON válido."); return;
            }
            var validacion = ValidadorRegistro.validar(entrada);
            if (!validacion.errores().isEmpty()) {
                responder(solicitud, 400, Map.of("codigo", "DATOS_INVALIDOS",
                        "mensaje", "Revisa los campos indicados.", "errores", validacion.errores())); return;
            }
            try {
                DatosRegistro datos = validacion.datos();
                String hash = HashContrasena.generar(datos.contrasena());
                long id = repositorio.insertar(datos, hash);
                responder(solicitud, 201, Map.of("codigo", "USUARIO_CREADO",
                        "mensaje", "Usuario registrado correctamente.",
                        "usuario", Map.of("id", id, "nombre", datos.nombre(), "rol", datos.rol())));
            } catch (SQLException e) {
                // El esquema solo tiene UNIQUE de correo además del id generado.
                if (e.getErrorCode() == 1062 && "23000".equals(e.getSQLState())) {
                    error(solicitud, 409, "CORREO_DUPLICADO", "Este correo ya está registrado.");
                } else error(solicitud, 500, "ERROR_INTERNO", "No se pudo completar el registro.");
            } catch (RuntimeException e) {
                error(solicitud, 500, "ERROR_INTERNO", "No se pudo completar el registro.");
            }
        } finally { solicitud.close(); }
    }

    private static Map<String, String> leer(String cuerpo) throws IOException, DatosInvalidos {
        try (JsonReader lector = new JsonReader(new StringReader(cuerpo))) {
            lector.setStrictness(Strictness.STRICT);
            if (lector.peek() != JsonToken.BEGIN_OBJECT) throw new DatosInvalidos();
            Map<String, String> campos = new LinkedHashMap<>();
            lector.beginObject();
            while (lector.hasNext()) {
                String nombre = lector.nextName();
                if (campos.containsKey(nombre) || !ValidadorRegistro.CAMPOS.contains(nombre)
                        || lector.peek() != JsonToken.STRING) throw new DatosInvalidos();
                campos.put(nombre, lector.nextString());
            }
            lector.endObject();
            if (lector.peek() != JsonToken.END_DOCUMENT) throw new IOException("Contenido adicional.");
            return campos;
        }
    }

    private static final class DatosInvalidos extends Exception { }

    private static void error(HttpExchange s, int status, String codigo, String mensaje) throws IOException {
        responder(s, status, Map.of("codigo", codigo, "mensaje", mensaje));
    }

    private static void responder(HttpExchange s, int status, Object valor) throws IOException {
        byte[] bytes = JSON.toJson(valor).getBytes(StandardCharsets.UTF_8);
        s.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        s.getResponseHeaders().set("Cache-Control", "no-store");
        s.sendResponseHeaders(status, bytes.length);
        s.getResponseBody().write(bytes);
    }
}
