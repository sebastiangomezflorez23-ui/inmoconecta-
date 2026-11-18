package com.inmoconecta;

import com.inmoconecta.acceso.AccesoHandler;
import com.inmoconecta.datos.*;
import com.inmoconecta.registro.DatosRegistro;
import com.inmoconecta.seguridad.HashContrasena;
import java.util.UUID;
import java.util.concurrent.*;

/** Integración real optativa. Crea dos usuarios ficticios y conserva sus filas como evidencia. */
public final class PruebasAccesoMysql {
    public static void main(String[] args) throws Exception {
        // Preflight: fallar antes de insertar usuarios si falta la migración 003.
        try (var c = ConexionBD.abrir(); var q = c.prepareStatement("SELECT u.intentos_fallidos, u.bloqueado_hasta, s.vence_en FROM usuarios u LEFT JOIN sesiones s ON s.usuario_id=u.id LIMIT 0")) { q.executeQuery().close(); }
        String sufijo = UUID.randomUUID().toString();
        String correo = "hu03-" + sufijo + "@example.com", paralelo = "hu03-par-" + sufijo + "@example.com";
        String clave = "Prueba ficticia " + UUID.randomUUID();
        String hash = HashContrasena.generar(clave);
        var usuarios = new UsuarioDao();
        long id = usuarios.insertar(new DatosRegistro("Prueba HU03", "Integracion", "3001234567", correo, clave, "COMPRADOR"), hash);
        long idPar = usuarios.insertar(new DatosRegistro("Prueba HU03", "Concurrencia", "3001234567", paralelo, clave, "AGENTE"), hash);
        var dao = new AccesoDao();
        String token = AccesoHandler.huella(UUID.randomUUID().toString());
        try {
            comprobar(dao.autenticar(correo, "incorrecta", token) == null, "fallo sin sesión");
            comprobar(dao.autenticar(correo, clave, token).id() == id, "acceso correcto");
            comprobar(dao.consultar(token).id() == id, "sesión persistida");
            comprobar(dao.consultar("0".repeat(64)) == null, "sesión desconocida");
            comprobar(dao.autenticar(correo, "incorrecta", token) == null, "nuevo fallo");
            try { dao.autenticar(correo, clave, token); throw new AssertionError("Debía fallar token duplicado"); }
            catch (java.sql.SQLException esperado) { comprobar(esperado.getErrorCode() == 1062, "fallo de inserción controlado"); }
            try (var c = ConexionBD.abrir(); var q = c.prepareStatement("SELECT intentos_fallidos FROM usuarios WHERE id=?")) {
                q.setLong(1,id); try(var r=q.executeQuery()) { r.next(); comprobar(r.getInt(1)==1,"rollback conserva contador previo"); }
            }
            try (var grupo = Executors.newFixedThreadPool(3)) {
                var salida = new java.util.ArrayList<Future<?>>();
                for (int i=0;i<3;i++) salida.add(grupo.submit(() -> { try { comprobar(dao.autenticar(paralelo,"incorrecta",AccesoHandler.huella(UUID.randomUUID().toString()))==null,"fallo concurrente"); } catch(Exception e) { throw new RuntimeException(e); } }));
                for(var futuro:salida) futuro.get(30, TimeUnit.SECONDS);
            }
            comprobar(dao.autenticar(paralelo,clave,AccesoHandler.huella(UUID.randomUUID().toString()))==null,"bloqueo después de tres fallos concurrentes");
            try(var c=ConexionBD.abrir(); var q=c.prepareStatement("SELECT intentos_fallidos, bloqueado_hasta > UTC_TIMESTAMP(6) FROM usuarios WHERE id=?")) {
                q.setLong(1,idPar); try(var r=q.executeQuery()) { r.next(); comprobar(r.getInt(1)==3 && r.getBoolean(2),"bloqueo persistido en MySQL"); }
            }
            String vencida = AccesoHandler.huella(UUID.randomUUID().toString());
            try(var c=ConexionBD.abrir(); var q=c.prepareStatement("INSERT INTO sesiones (token_hash,usuario_id,creada_en,vence_en) VALUES (?,?,UTC_TIMESTAMP(6)-INTERVAL 2 HOUR,UTC_TIMESTAMP(6)-INTERVAL 1 HOUR)")) {
                q.setString(1,vencida); q.setLong(2,id); q.executeUpdate();
            }
            comprobar(dao.consultar(vencida)==null,"sesión vencida denegada");
            System.out.println("OK: integración MySQL de HU03. Reinicio Java y administrador aún requieren prueba separada.");
        } finally {
            try(var c=ConexionBD.abrir(); var q=c.prepareStatement("DELETE FROM sesiones WHERE usuario_id IN (?,?)")) {q.setLong(1,id); q.setLong(2,idPar); q.executeUpdate();}
            System.out.println("Usuarios ficticios conservados: " + correo + " / " + paralelo);
        }
    }
    private static void comprobar(boolean condicion,String nombre) {if(!condicion) throw new AssertionError(nombre); System.out.println("OK: "+nombre);}
}
