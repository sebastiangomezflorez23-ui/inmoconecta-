package com.inmoconecta;

import com.inmoconecta.acceso.PoliticaAcceso;
import java.time.LocalDateTime;

public final class PruebasAcceso {
    public static void main(String[] args) {
        var ahora = LocalDateTime.of(2026, 11, 18, 12, 0);
        var estado = new PoliticaAcceso.Estado(0, null);
        estado = estado.fallar(ahora); comprobar(estado.fallos() == 1 && !estado.bloqueado(ahora));
        estado = estado.fallar(ahora); comprobar(estado.fallos() == 2 && !estado.bloqueado(ahora));
        estado = estado.fallar(ahora); comprobar(estado.fallos() == 3 && estado.bloqueado(ahora));
        comprobar(estado.fallar(ahora.plusSeconds(1)).equals(estado));
        comprobar(estado.bloqueado(estado.hasta().minusNanos(1)));
        comprobar(!estado.bloqueado(estado.hasta()));
        var nuevo = estado.fallar(estado.hasta()); comprobar(nuevo.fallos() == 1 && nuevo.hasta() == null);
        comprobar(new PoliticaAcceso.Estado(0, null).fallar(ahora).fallos() == 1);
        System.out.println("OK: 8 comprobaciones de reglas de bloqueo; MySQL no probado.");
    }
    private static void comprobar(boolean condicion) { if (!condicion) throw new AssertionError(); }
}
