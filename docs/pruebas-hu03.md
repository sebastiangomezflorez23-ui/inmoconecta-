# HU-03: acceso y evidencia

18/11/2026. Rama HU-03-inicio-sesion. Java 21, HttpServer y JDBC, sin Spring Boot.

Actualización local: migración 003 aplicada y verificada el 18/11/2026; cuatro usuarios preservados. Respaldo y comprobación en [evidencia de migración](evidencias/hu03/migracion-local.md). No repetirla en esta base. La suite JDBC real sigue pendiente de ejecución con la contraseña introducida por el usuario en su terminal.

## Implementado

- POST /api/login y GET /api/sesion; usuario público, rol leído en servidor.
- Tres fallos consecutivos bloquean la cuenta durante 15 minutos por defecto.
- Contraseña verificada con PBKDF2 existente; contador bajo bloqueo de fila y transacción JDBC.
- Sesión aleatoria de 32 bytes; solo SHA-256 en MySQL; vence a los 30 minutos por defecto.
- Cookie HttpOnly, SameSite=Strict, Path=/; Secure cuando el origen sea HTTPS. El servidor actual solo ofrece HTTP local en 127.0.0.1.
- POST requiere Origin exactamente http://127.0.0.1:PUERTO, incluso clientes curl; no admitir Origin ausente, null ni ajeno. GET de sesión permite ausencia de Origin, pero rechaza uno ajeno y Sec-Fetch-Site cross-site.
- Pantallas login.html y cuenta.html con estados de carga/error, rol, comprobación de sesión y enlaces desde inicio/registro.
- HU-04 conserva pendiente logout; los paneles de publicaciones y moderación no están implementados.

Configurar ACCESO_BLOQUEO_MINUTOS y ACCESO_SESION_MINUTOS (1 a 1440) antes de iniciar Java para cambiar los tiempos. El texto del bloqueo se genera con el valor del servidor. La configuración por defecto sigue pendiente de aceptación del equipo.

## Verificado sin MySQL

- Compilación de todos los fuentes y pruebas con JDK 21.
- PruebasAcceso: 8 comprobaciones, incluidos frontera de expiración y reinicio del contador.
- PruebasT09: 53 comprobaciones existentes del registro correctas.
- acceso-http.cjs: HTTP Java real + repositorio en memoria + Chrome. Tres roles, respuesta sin secretos, cookies, sesión ausente/alterada, JSON inválido/duplicado, origen ajeno/ausente, bloqueo tras tres fallos, fallo SQL simulado, red y envío único.
- registro-http.cjs: regresión del formulario de registro correcta.
- Capturas en docs/evidencias/hu03: login en 360/390/768/1440 px y cuenta. Revisadas visualmente móvil y escritorio. Las cuentas de estas capturas son simuladas.

Estas pruebas NO verifican el SQL ni acreditan concurrencia transaccional, permisos o reinicios con MySQL real.

## Siguiente validación local

1. Completado en esta base local: respaldo y aplicación de database/003_preparar_acceso.sql. Para otra instalación, respaldar y revisar antes de aplicarla una vez. No repetir ALTER TABLE en la base actual.
2. En Git Bash, desde la raíz del proyecto, ejecutar `bash backend/scripts/probar-hu03-mysql.sh`. La contraseña se introduce solo en la terminal. Requiere los JAR locales usados por iniciar-t09.sh.
3. La prueba crea dos usuarios ficticios con correo único, comprueba acceso, rollback, tres fallos concurrentes y sesión vencida. Elimina sus sesiones al terminar, conserva sus usuarios como evidencia. No modifica usuarios anteriores. Esta suite está preparada y compilada, pero NO ejecutada todavía.
4. Iniciar `bash backend/scripts/iniciar-t09.sh`, abrir http://127.0.0.1:8080/login.html y usar una cuenta de prueba registrada por la interfaz.
5. Comprobar acceso y bloqueo antes/después de un reinicio real de Java. Para comprobar expiración en desarrollo se pueden usar tiempos de 1 minuto; después volver a los valores acordados.
6. Revisar PR e integrar antes de marcar T19/T20/T21 y HU-03 como Listo. ADMIN requiere cuenta administrada; no se crea desde el registro ni desde este avance.

Para repetir navegador: compilar backend/src y backend/test, ejecutar `java -cp 'backend/out;backend/lib/*' com.inmoconecta.PruebasAcceso` y `node frontend/tests/acceso-http.cjs` con Playwright/Chrome disponibles. El fixture no forma parte del servidor de producción.
