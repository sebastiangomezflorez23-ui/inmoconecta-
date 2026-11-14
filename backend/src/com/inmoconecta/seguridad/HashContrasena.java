package com.inmoconecta.seguridad;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/** PBKDF2 del proveedor criptográfico del JDK; no implementamos el algoritmo. */
public final class HashContrasena {
    private static final int ITERACIONES = 600_000;
    private static final SecureRandom AZAR = new SecureRandom();

    public static String generar(String clave) {
        byte[] salt = new byte[16];
        AZAR.nextBytes(salt);
        return "pbkdf2-sha256$" + ITERACIONES + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(derivar(clave, salt));
    }

    public static boolean verificar(String clave, String codificado) {
        try {
            String[] partes = codificado.split("\\$", -1);
            if (partes.length != 4 || !partes[0].equals("pbkdf2-sha256")
                    || !partes[1].equals(String.valueOf(ITERACIONES))) return false;
            byte[] salt = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            if (salt.length != 16 || esperado.length != 32) return false;
            return MessageDigest.isEqual(esperado, derivar(clave, salt));
        } catch (IllegalArgumentException e) { return false; }
    }

    private static byte[] derivar(String clave, byte[] salt) {
        char[] caracteres = clave.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(caracteres, salt, ITERACIONES, 256);
        Arrays.fill(caracteres, '\0');
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo proteger la contraseña.", e);
        } finally { spec.clearPassword(); }
    }
}
