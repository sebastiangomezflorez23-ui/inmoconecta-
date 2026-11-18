# Seguimiento del plan en Jira

Actualizado 17/11/2026. Las claves siguientes ya existen en NMCNCT2. No volver a crearlas.

Actualización 18/11/2026: T18 tiene migración local aplicada, respaldo de cuatro usuarios y conservación comprobada. Evidencia docs/evidencias/hu03/migracion-local.md. T19/T21 requieren aún prueba JDBC real y reinicio; no confundir aplicación SQL con validación completa del login.

## Épicas

- Cuentas y acceso: NMCNCT2-2
- Inicio y búsqueda: NMCNCT2-21
- Publicación y administración: NMCNCT2-22
- Contacto: NMCNCT2-23
- Favoritos: NMCNCT2-24

## Historias

| Historia | Clave | Estado de planificación |
| --- | --- | --- |
| HU-01 Registro de usuario | NMCNCT2-3 | En curso |
| HU-02 Página de inicio | NMCNCT2-15 | En curso |
| HU-03 Inicio de sesión y bloqueo | NMCNCT2-25 | En curso |
| HU-04 Cierre de sesión | NMCNCT2-26 | Por hacer |
| HU-05 Publicar una propiedad | NMCNCT2-27 | Por hacer |
| HU-06 Fotografías de la propiedad | NMCNCT2-28 | Por hacer |
| HU-07 Gestionar publicaciones propias | NMCNCT2-29 | Por hacer |
| HU-08 Moderar publicaciones | NMCNCT2-30 | Por hacer |
| HU-09 Catálogo real y filtros | NMCNCT2-31 | Por hacer |
| HU-10 Búsqueda en mapa | NMCNCT2-32 | Por hacer |
| HU-11 Detalle de propiedad | NMCNCT2-33 | Por hacer |
| HU-12 Enviar consulta al agente | NMCNCT2-34 | Por hacer |
| HU-13 Bandeja de consultas | NMCNCT2-35 | Por hacer |
| HU-14 Contacto por WhatsApp | NMCNCT2-36 | Por hacer |
| HU-15 Favoritos | NMCNCT2-37 | Por hacer |

## Primer incremento de acceso

- T17: NMCNCT2-38
- T18: NMCNCT2-39
- T19: NMCNCT2-40
- T20: NMCNCT2-41
- T21: NMCNCT2-42
- T22: NMCNCT2-43
- T23: NMCNCT2-44

Actualización 18/11/2026: T17 contrato implementado con valores configurables pendientes de aceptación. T18 migración aplicada con respaldo y conservación de datos verificados. T19/T20/T21 En curso: login y cuenta implementados, pruebas HTTP/navegador y reglas correctas; falta integración MySQL real, revisión e integración del PR. T22/T23 pendientes: logout aún no implementado. Detalle en docs/pruebas-hu03.md.

Se crearon 24 dependencias entre historias. Se conservan T01–T16 y las claves existentes. No se iniciaron sprints ni se asignaron personas sin acordar capacidad. T09 pasó a Listo después de confirmar implementación, pruebas y PR #6 integrado. La comprobación tras reiniciar Java no tiene evidencia nueva.

Rama de preparación renombrada: HU-03-inicio-sesion, basada en origin/main (2061b). PR #6 integrado 14/11 y PR #7 integrado 16/11, comprobados por API GitHub el 17/11. La revisión docente sigue pendiente; no se ha demostrado un reinicio específico de Java.


T24 UML/diseño: NMCNCT2-45. T25 despliegue/no funcionales: NMCNCT2-46. Ambas Por hacer, etapa 6.

## Sprint 1 - Inicio, registro y acceso

Sprint existente 68, tablero 69. Preparado, sin iniciar; fechas y capacidad pendientes de acordar.

Objetivo: demostrar inicio, registro persistente en MySQL, inicio y cierre seguro de sesión.

| Orden | Historia | Trabajo restante |
| --- | --- | --- |
| 1 | HU-01 / NMCNCT2-3 | Cerrar T04, T05 y T11 con evidencia y revisión; comprobar persistencia tras reinicio real. |
| 2 | HU-02 / NMCNCT2-15 | Completar revisión docente T16 de la demo integrada. |
| 3 | HU-03 / NMCNCT2-25 | T17 contrato, T18 migración, T19 autenticación, T20 interfaz y T21 pruebas. |
| 4 | HU-04 / NMCNCT2-26 | T22 salida/vencimiento y T23 pruebas de acceso privado. |

HU-05 a HU-15 y T24/T25 permanecen en backlog. Conservar estados reales y subtareas existentes; no dar por aceptada una historia por incluirla en el sprint.

## Ramas por historia

Convención acordada: HU-XX-descripcion, sin prefijo codex. Ramas del trabajo nuevo:

- HU-03-inicio-sesion: rama actual, contiene preparación del contrato y migración.
- HU-04-cierre-sesion: creada desde origin/main; actualizarla con main después de integrar HU-03 antes de implementar el cierre.

Las ramas antiguas conservan su nombre histórico. Para siguientes historias crear su rama desde main actualizado al comenzar, por ejemplo HU-05-publicar-propiedad. No crear ramas futuras con bases desactualizadas.
