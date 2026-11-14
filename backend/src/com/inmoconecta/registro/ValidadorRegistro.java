package com.inmoconecta.registro;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ValidadorRegistro {
    public static final Set<String> CAMPOS =
            Set.of("nombre", "apellido", "telefono", "correo", "contrasena", "rol");

    public record Resultado(DatosRegistro datos, Map<String, String> errores) {}

    public static Resultado validar(Map<String, String> entrada) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (String campo : CAMPOS) {
            String valor = entrada.get(campo);
            if (valor == null || valor.strip().isEmpty()) errores.put(campo, "Completa este campo.");
        }
        for (String campo : entrada.keySet()) {
            if (!CAMPOS.contains(campo)) errores.put("general", "Hay campos desconocidos.");
        }
        String nombre = texto(entrada, "nombre").strip();
        String apellido = texto(entrada, "apellido").strip();
        String telefono = texto(entrada, "telefono").strip();
        String correo = texto(entrada, "correo").strip().toLowerCase(Locale.ROOT);
        String clave = texto(entrada, "contrasena");
        String rol = texto(entrada, "rol");
        if (nombre.length() > 80) errores.put("nombre", "Máximo 80 caracteres.");
        if (apellido.length() > 80) errores.put("apellido", "Máximo 80 caracteres.");
        String digitos = telefono.replaceAll("[^0-9]", "");
        if (telefono.length() > 25 || !telefono.matches("\\+?[0-9 ()-]+")
                || digitos.length() < 7 || digitos.length() > 15) {
            errores.put("telefono", "Escribe un teléfono de 7 a 15 dígitos.");
        }
        if (correo.length() > 254 || !correo.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            errores.put("correo", "Escribe un correo válido.");
        }
        if (clave.length() < 12 || clave.length() > 128) {
            errores.put("contrasena", "La contraseña debe tener entre 12 y 128 caracteres.");
        }
        if (!Set.of("COMPRADOR", "AGENTE").contains(rol)) errores.put("rol", "Rol no permitido.");
        DatosRegistro datos = new DatosRegistro(nombre, apellido,
                (telefono.startsWith("+") ? "+" : "") + digitos, correo, clave, rol);
        return new Resultado(datos, Map.copyOf(errores));
    }

    private static String texto(Map<String, String> datos, String campo) {
        String valor = datos.get(campo);
        return valor == null ? "" : valor;
    }
}
