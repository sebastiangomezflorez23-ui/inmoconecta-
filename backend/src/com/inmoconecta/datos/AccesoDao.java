package com.inmoconecta.datos;

import com.inmoconecta.acceso.PoliticaAcceso;
import com.inmoconecta.acceso.RepositorioAcceso;
import com.inmoconecta.seguridad.HashContrasena;
import java.sql.*;
import java.time.LocalDateTime;

/** Serializa los intentos por cuenta. Ninguna cookie se emite antes del commit. */
public final class AccesoDao implements RepositorioAcceso {
    private static final String FICTICIO = HashContrasena.generar("Cuenta inexistente de comprobacion");

    @Override public Usuario autenticar(String correo, String clave, String tokenHash) throws SQLException {
        try (Connection c = ConexionBD.abrir()) {
            c.setAutoCommit(false);
            try {
                Usuario usuario = null;
                try (var consulta = c.prepareStatement("SELECT id, nombre, rol, contrasena_hash, intentos_fallidos, bloqueado_hasta FROM usuarios WHERE correo = ? FOR UPDATE")) {
                    consulta.setString(1, correo);
                    try (var r = consulta.executeQuery()) {
                        if (r.next()) {
                            long id = r.getLong("id");
                            String nombre = r.getString("nombre"), rol = r.getString("rol");
                            var estado = new PoliticaAcceso.Estado(r.getInt("intentos_fallidos"), r.getObject("bloqueado_hasta", LocalDateTime.class));
                            // Obtener el reloj después de adquirir el bloqueo de la fila.
                            LocalDateTime ahora = ahora(c);
                            boolean coincide = HashContrasena.verificar(clave, r.getString("contrasena_hash"));
                            if (!estado.bloqueado(ahora)) {
                                if (coincide) {
                                    actualizar(c, id, new PoliticaAcceso.Estado(0, null));
                                    try (var insertar = c.prepareStatement("INSERT INTO sesiones (token_hash, usuario_id, creada_en, vence_en) VALUES (?, ?, ?, ?)")) {
                                        insertar.setString(1, tokenHash); insertar.setLong(2, id);
                                        insertar.setObject(3, ahora);
                                        insertar.setObject(4, ahora.plusMinutes(PoliticaAcceso.SESION_MINUTOS));
                                        insertar.executeUpdate();
                                    }
                                    usuario = new Usuario(id, nombre, rol);
                                } else actualizar(c, id, estado.fallar(ahora));
                            }
                        } else HashContrasena.verificar(clave, FICTICIO);
                    }
                }
                try (var limpiar = c.prepareStatement("DELETE FROM sesiones WHERE vence_en <= UTC_TIMESTAMP(6) LIMIT 100")) {
                    limpiar.executeUpdate();
                }
                c.commit();
                return usuario;
            } catch (SQLException | RuntimeException e) {
                try { c.rollback(); } catch (SQLException fallo) { e.addSuppressed(fallo); }
                throw e;
            }
        }
    }

    @Override public Usuario consultar(String tokenHash) throws SQLException {
        try (var c = ConexionBD.abrir(); var q = c.prepareStatement("SELECT u.id, u.nombre, u.rol FROM sesiones s JOIN usuarios u ON u.id = s.usuario_id WHERE s.token_hash = ? AND s.vence_en > UTC_TIMESTAMP(6)")) {
            q.setString(1, tokenHash);
            try (var r = q.executeQuery()) {
                return r.next() ? new Usuario(r.getLong(1), r.getString(2), r.getString(3)) : null;
            }
        }
    }

    private static LocalDateTime ahora(Connection c) throws SQLException {
        try (var q = c.prepareStatement("SELECT UTC_TIMESTAMP(6)"); var r = q.executeQuery()) {
            r.next(); return r.getObject(1, LocalDateTime.class);
        }
    }
    private static void actualizar(Connection c, long id, PoliticaAcceso.Estado estado) throws SQLException {
        try (var q = c.prepareStatement("UPDATE usuarios SET intentos_fallidos = ?, bloqueado_hasta = ? WHERE id = ?")) {
            q.setInt(1, estado.fallos()); q.setObject(2, estado.hasta()); q.setLong(3, id); q.executeUpdate();
        }
    }
}
