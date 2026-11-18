package com.inmoconecta.acceso;

import java.time.Duration;
import java.time.LocalDateTime;

/** Reglas compartidas; los tiempos usados por el DAO son UTC. */
public final class PoliticaAcceso {
    public static final int BLOQUEO_MINUTOS = minutos("ACCESO_BLOQUEO_MINUTOS", 15);
    public static final int SESION_MINUTOS = minutos("ACCESO_SESION_MINUTOS", 30);
    public record Estado(int fallos, LocalDateTime hasta) {
        public boolean bloqueado(LocalDateTime ahora) { return hasta != null && hasta.isAfter(ahora); }
        public Estado fallar(LocalDateTime ahora) {
            if (bloqueado(ahora)) return this;
            int nuevos = hasta != null ? 1 : Math.min(fallos + 1, 3);
            return new Estado(nuevos, nuevos >= 3 ? ahora.plus(Duration.ofMinutes(BLOQUEO_MINUTOS)) : null);
        }
    }
    private static int minutos(String nombre, int defecto) {
        String valor = System.getenv(nombre);
        int minutos = valor == null ? defecto : Integer.parseInt(valor);
        if (minutos < 1 || minutos > 1440) throw new IllegalArgumentException(nombre + " debe estar entre 1 y 1440.");
        return minutos;
    }
    private PoliticaAcceso() { }
}
