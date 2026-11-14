package com.inmoconecta;

import com.google.gson.Gson;
import com.inmoconecta.registro.*;
import com.inmoconecta.seguridad.HashContrasena;
import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.net.http.*;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/** Repositorio simulado solo en pruebas. No requiere MySQL ni credenciales. */
public final class PruebasT09 {
    private static int casos;
    private static URI uri;
    private static final HttpClient CLIENTE = HttpClient.newHttpClient();
    private static final Gson JSON = new Gson();

    private static Map<String,String> datos() {
        return new HashMap<>(Map.of("nombre", " Ana ", "apellido", "Gómez", "telefono",
                "+57 (300) 123-4567", "correo", " ANA@example.com ",
                "contrasena", "  Frase de prueba 123  ", "rol", "COMPRADOR"));
    }
    private static void comprobar(boolean resultado, String nombre) {
        if (!resultado) throw new AssertionError(nombre);
        casos++;
        System.out.println("OK: " + nombre);
    }
    private static HttpResponse<String> pedir(String metodo, String cuerpo, String tipo) throws Exception {
        return CLIENTE.send(HttpRequest.newBuilder(uri).header("Content-Type", tipo)
                .method(metodo, HttpRequest.BodyPublishers.ofString(cuerpo)).build(),
                HttpResponse.BodyHandlers.ofString());
    }
    private static void esperar(int status, String cuerpo, String nombre) throws Exception {
        var respuesta = pedir("POST", cuerpo, "application/json");
        comprobar(respuesta.statusCode() == status, nombre + " HTTP " + respuesta.statusCode());
        comprobar(!respuesta.body().contains("Frase de prueba") &&
                !respuesta.body().contains("pbkdf2-sha256"), nombre + " sin secretos");
    }

    public static void main(String[] args) throws Exception {
        var normal = ValidadorRegistro.validar(datos());
        comprobar(normal.errores().isEmpty(), "datos válidos");
        comprobar(normal.datos().correo().equals("ana@example.com") &&
                normal.datos().telefono().equals("+573001234567") &&
                normal.datos().nombre().equals("Ana"), "normalización");
        comprobar(normal.datos().contrasena().equals(datos().get("contrasena")), "clave intacta");
        for (String campo : ValidadorRegistro.CAMPOS) {
            var entrada = datos(); entrada.remove(campo);
            comprobar(!ValidadorRegistro.validar(entrada).errores().isEmpty(), "obligatorio " + campo);
        }
        for (String rol : List.of("ADMIN", "agente", " AGENTE")) {
            var entrada = datos(); entrada.put("rol", rol);
            comprobar(!ValidadorRegistro.validar(entrada).errores().isEmpty(), "rechazar rol " + rol);
        }
        for (int n : List.of(11, 12, 128, 129)) {
            var entrada = datos(); entrada.put("contrasena", "x".repeat(n));
            comprobar(ValidadorRegistro.validar(entrada).errores().isEmpty() == (n >= 12 && n <= 128),
                    "límite contraseña " + n);
        }
        var espacios = datos(); espacios.put("contrasena", " ".repeat(12));
        comprobar(!ValidadorRegistro.validar(espacios).errores().isEmpty(), "clave solo espacios");
        var largo = datos(); largo.put("nombre", "a".repeat(81));
        comprobar(!ValidadorRegistro.validar(largo).errores().isEmpty(), "nombre largo");
        var telefono = datos(); telefono.put("telefono", "123456");
        comprobar(!ValidadorRegistro.validar(telefono).errores().isEmpty(), "teléfono corto");
        String clave = datos().get("contrasena");
        String hash1 = HashContrasena.generar(clave), hash2 = HashContrasena.generar(clave);
        comprobar(!hash1.equals(hash2), "salt aleatorio");
        comprobar(HashContrasena.verificar(clave, hash1) &&
                HashContrasena.verificar(clave, hash2), "verificar hashes");
        comprobar(!HashContrasena.verificar("incorrecta", hash1), "rechazar contraseña incorrecta");
        comprobar(!HashContrasena.verificar(clave, "formato inválido"), "rechazar hash inválido");

        var correos = ConcurrentHashMap.<String>newKeySet();
        var contador = new AtomicLong();
        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var ejecutor = Executors.newFixedThreadPool(4);
        servidor.setExecutor(ejecutor);
        servidor.createContext("/api/usuarios", new RegistroHandler((d, hash) -> {
            if (d.correo().equals("fallo@example.com")) throw new SQLException("dato interno", "08001", 0);
            if (!hash.startsWith("pbkdf2-sha256$")) throw new AssertionError("Falta hash");
            if (!correos.add(d.correo())) throw new SQLException("duplicado", "23000", 1062);
            return contador.incrementAndGet();
        }));
        servidor.start();
        uri = URI.create("http://127.0.0.1:" + servidor.getAddress().getPort() + "/api/usuarios");
        try {
            esperar(201, JSON.toJson(datos()), "crear COMPRADOR simulado");
            esperar(409, JSON.toJson(datos()), "correo duplicado simulado");
            var agente = datos(); agente.put("correo", "agente@example.com"); agente.put("rol", "AGENTE");
            esperar(201, JSON.toJson(agente), "crear AGENTE simulado");
            esperar(400, "{}", "vacío");
            esperar(400, "{", "JSON incompleto");
            esperar(400, "{\"nombre\":null}", "null");
            esperar(400, "{\"nombre\":12}", "número");
            esperar(400, "{\"extra\":\"x\"}", "campo desconocido");
            esperar(400, "{\"nombre\":\"a\",\"nombre\":\"b\"}", "clave repetida");
            esperar(400, JSON.toJson(datos()) + " {}", "dos objetos");
            esperar(400, "{'nombre':'Ana'}", "JSON no estricto");
            esperar(413, "x".repeat(16385), "cuerpo grande");
            comprobar(pedir("POST", "{}", "text/plain").statusCode() == 415, "tipo incorrecto");
            var metodo = pedir("GET", "", "application/json");
            comprobar(metodo.statusCode() == 405 &&
                    metodo.headers().firstValue("Allow").orElse("").equals("POST"), "método y Allow");
            var fallo = datos(); fallo.put("correo", "fallo@example.com");
            var respuestaFallo = pedir("POST", JSON.toJson(fallo), "application/json");
            comprobar(respuestaFallo.statusCode() == 500 &&
                    !respuestaFallo.body().contains("dato interno"), "fallo SQL sin detalles");

            var concurrente = datos(); concurrente.put("correo", "concurrente@example.com");
            String cuerpo = JSON.toJson(concurrente);
            var peticion = HttpRequest.newBuilder(uri).header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(cuerpo)).build();
            var uno = CLIENTE.sendAsync(peticion, HttpResponse.BodyHandlers.ofString());
            var dos = CLIENTE.sendAsync(peticion, HttpResponse.BodyHandlers.ofString());
            var estados = new HashSet<>(List.of(uno.get().statusCode(), dos.get().statusCode()));
            comprobar(estados.equals(Set.of(201,409)), "concurrencia con repositorio simulado");
            comprobar(contador.get() == 3, "sin inserciones en casos inválidos");
            uri = URI.create(uri.toString() + "/extra");
            comprobar(pedir("POST", "{}", "application/json").statusCode() == 404, "ruta exacta");
        } finally {
            servidor.stop(0); ejecutor.shutdownNow(); CLIENTE.close();
        }
        System.out.println("TOTAL: " + casos + " comprobaciones correctas. MySQL NO probado.");
    }
}
