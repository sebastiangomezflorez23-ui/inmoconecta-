# HU-02 — Inicio y exploración de propiedades (demo)

Fecha de verificación: 16 de noviembre de 2026.
Jira: [NMCNCT2-15](https://sebastiangomezflorez23.atlassian.net/browse/NMCNCT2-15).
Rama: `codex/hu02-inicio-demo`.

## Historia de usuario

Como visitante, quiero conocer InmoConecta desde su página de inicio para explorar propiedades de ejemplo y acceder al registro.

## Checklist de aceptación

- [x] CA01: al abrir el inicio, se muestran nombre y propósito de InmoConecta.
- [x] CA02: la navegación permite acceder a Inicio, Propiedades y Registro.
- [x] CA03: Registrarme abre el formulario existente y permite volver al inicio.
- [x] CA04: cada propiedad presenta ilustración, ubicación, precio en COP y operación.
- [x] CA05: se identifica explícitamente que las propiedades y precios son ficticios.
- [x] CA06: venta muestra 2 ejemplos; arriendo muestra 1; Envigado + arriendo muestra estado vacío; restablecer recupera los 3.
- [x] CA07: no hay desplazamiento horizontal a 320, 390, 768, 1024 y 1440 píxeles.
- [x] CA08: filtros etiquetados, navegación con teclado, enlace para saltar al contenido y foco visible.
- [ ] Revisión y aceptación del profesor; revisión de cambios e integración mediante PR (T16).

Los criterios anteriores son del alcance demo propuesto. No implican aprobación del profesor ni conformidad completa con un estándar de accesibilidad.

## Evidencia y validación

Prueba automatizada: `frontend/tests/inicio.cjs`, ejecutada con Node 24, Playwright y Chrome local.
Comprueba los casos CA02–CA08, carga de imágenes, ausencia de errores JavaScript, ausencia de solicitudes externas y validación del formulario sin crear cuentas.
Revisión visual de capturas de escritorio y móvil para CA01, CA04, CA05 y CA07.

- [Inicio escritorio](evidencias/hu02/inicio-escritorio.png)
- [Inicio móvil](evidencias/hu02/inicio-movil.png)
- [Sin resultados](evidencias/hu02/filtros-sin-resultados.png)
- [Formulario demo](evidencias/hu02/registro-demo.png)

Para repetir pruebas en un entorno con Node, Playwright y Google Chrome instalados:

```bash
node frontend/tests/inicio.cjs
```

La aplicación en sí no necesita Node, Java, MySQL, internet ni instalación de dependencias. Abre `frontend/index.html` en un navegador con JavaScript habilitado. La prueba usa `file://`, igual que la presentación local.

## Guion de presentación (3 minutos)

1. Abre `frontend/index.html`. Explica la historia: conocer la plataforma y explorar una selección.
2. Muestra propósito, navegación y la indicación de demo. Los datos e ilustraciones son ficticios.
3. Baja a Propiedades: hay 3 ejemplos con ubicación, precio y características.
4. Selecciona Comprar: aparecen 2. Selecciona Arrendar: aparece 1.
5. Con Arrendar, selecciona Envigado: aparece un mensaje sin resultados. Restablece.
6. Pulsa Registrarme. Envía vacío para mostrar validaciones. Usa datos ficticios completos: el mensaje aclara que no crea una cuenta.
7. Vuelve al inicio y muestra la captura móvil y este checklist.

## Organización de Jira

- T12 / NMCNCT2-16: inicio adaptable e identidad visual.
- T13 / NMCNCT2-17: catálogo demo y filtros.
- T14 / NMCNCT2-18: navegación al registro existente.
- T15 / NMCNCT2-19: verificación y evidencias.
- T16 / NMCNCT2-20: revisión docente e integración, pendiente.

HU-02 permanece En curso hasta revisión e integración. HU-01 y T10 mantienen su alcance: conectar el formulario con el registro real. No se cierra HU-01 por esta demo.

## Límites de la entrega

No hay publicaciones reales, detalles de inmuebles, contacto con agentes ni inicio de sesión. El catálogo reside en JavaScript, sin persistencia. El registro del navegador sigue en demostración; el backend Java/JDBC se trabaja por separado. Ilustraciones SVG originales incluidas en el repositorio, sin recursos externos.

Actualización T10: al servir la página desde Java, el formulario sí registra en MySQL. Al abrirla como archivo mantiene la demo. Véase [integración y pruebas actuales](t10-integracion.md).
