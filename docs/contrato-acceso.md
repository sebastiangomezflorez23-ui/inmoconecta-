# HU-03 y HU-04 — Contrato de acceso

Preparación del 17/11/2026. Jira: HU-03 NMCNCT2-25 y HU-04 NMCNCT2-26.
Este contrato y la migración son preparación; aún no existen los endpoints ni la pantalla de login.

## Decisiones del primer incremento

- Mantener Java 21 HttpServer, JDBC y MySQL; reutilizar HashContrasena.verificar.
- El requisito original exige bloqueo tras 3 fallos consecutivos. Valor de trabajo propuesto: 15 minutos. Al expirar, el siguiente intento inicia una nueva secuencia; un éxito reinicia el contador.
- Sesión propuesta de 30 minutos absolutos, sin renovación automática inicial. Un logout invalida la sesión actual. Son valores configurables que se revisarán con el equipo antes de cerrar T17.
- COMPRADOR, AGENTE y ADMIN se leen desde MySQL, nunca del cuerpo del login. El registro público sigue admitiendo únicamente COMPRADOR y AGENTE.
- La migración amplía el esquema para ADMIN, pero no crea un administrador. Su aprovisionamiento será controlado y no incorporará credenciales a Git.
- No mostrar un panel de publicaciones como funcional hasta implementar sus historias.

## API propuesta

| Ruta | Método | Entrada y resultado |
| --- | --- | --- |
| /api/login | POST | JSON con correo y contrasena. 200 y cookie si autenticación correcta. |
| /api/sesion | GET | Lee cookie; 200 con id, nombre y rol públicos; 401 si ausente, vencida o inválida. |
| /api/logout | POST | Invalida la sesión y elimina cookie. 204 también si ya no existe. |

Login acepta únicamente dos campos de texto. Normaliza correo igual que registro; no recorta la contraseña. Limitar tamaño de petición y rechazar campos extra, JSON inválido y tipos incorrectos de forma consistente con RegistroHandler.

Respuestas:

- 200: codigo SESION_INICIADA y usuario {id, nombre, rol}; sin token en JSON ni hash.
- 400: DATOS_INVALIDOS; error de formato antes de autenticación.
- 401: ACCESO_NO_DISPONIBLE; mismo mensaje para cuenta desconocida, contraseña incorrecta y cuenta bloqueada: «No se pudo iniciar sesión. Revisa tus datos. Tras tres fallos, espera 15 minutos antes de intentar de nuevo». No confirmar la existencia de un correo ni revelar su contador.
- 403: origen de petición no permitido o permiso insuficiente en una ruta privada.
- 405/415: método o Content-Type incorrecto.
- 500: fallo interno genérico. No crear una sesión si la operación de base de datos falla.

El mensaje genérico informa el tiempo de bloqueo sin permitir consultar el estado de cuentas ajenas. El contador y la fecha de bloqueo solo existen en servidor. Para un correo desconocido, realizar una verificación de hash ficticio para reducir diferencias de tiempo; no afirmar resistencia completa a enumeración solo por igualar mensajes.

## Persistencia y concurrencia

Migración propuesta: database/003_preparar_acceso.sql, una sola ejecución administrativa. Aún no aplicada. Preserva datos actuales y solo concede UPDATE de los campos de bloqueo a la cuenta de aplicación; no permite cambiar roles.

Implementar login en una transacción con SELECT ... FOR UPDATE del usuario. Dentro de ella: comprobar bloqueo y expiración, verificar contraseña, actualizar contador o reiniciarlo y crear sesión si corresponde. Commit antes de devolver cookie. En error, rollback y cierre de conexión. Las pruebas simultáneas deben demostrar que no se pierden intentos ni se emiten sesiones a una cuenta ya bloqueada.

Usar tiempos UTC para campos de bloqueo y sesiones. Guardar únicamente SHA-256 de un token aleatorio de al menos 32 bytes; el token original se entrega solo en cookie. Limpiar sesiones vencidas con una operación acotada. No almacenar tokens en localStorage, URLs ni registros de diagnóstico.

## Cookie y protección de peticiones

Cookie propuesta: inmoconecta_sesion; Path=/; HttpOnly; SameSite=Strict; Max-Age=1800. Secure obligatorio en despliegue HTTPS; desarrollo limitado a loopback HTTP. No fijar Domain.

Login y logout validarán el Origin contra el origen local configurado; rechazar Origin null o ajeno. Definir una política explícita para clientes sin Origin y usar pruebas HTTP coherentes con ella; no permitir un comodín CORS. Las futuras mutaciones privadas deberán usar la misma defensa y, si el alcance exige otros orígenes, un mecanismo CSRF explícito.

Respuestas privadas con Cache-Control: no-store. El HTML de un panel no es una barrera: toda API privada comprueba sesión y rol. Logout elimina la cookie con los mismos atributos y Max-Age=0. Una cookie robada, vencida o revocada no debe autorizar acciones.

## Pruebas de aceptación pendientes

1. Credenciales correctas para cada rol devuelven usuario público y cookie; nunca secretos.
2. Tres fallos bloquean; una contraseña correcta durante el bloqueo no inicia sesión; expiración permite reintentar.
3. Bloqueo persiste tras reiniciar Java y los fallos concurrentes se contabilizan correctamente.
4. Sesión válida permite consultar /api/sesion; alterada o vencida devuelve 401.
5. Origen ajeno no puede iniciar/cerrar sesiones ni realizar mutaciones.
6. Logout revoca y volver atrás/recargar no recupera datos privados.
7. Fallo MySQL no muestra éxito ni emite cookie.
8. Pruebas HTTP en memoria se etiquetan por separado de MySQL real. Revisar permisos de dos cuentas distintas.

## Continuación concreta

T17 NMCNCT2-38: revisar este contrato y resolver duración de bloqueo/sesión.
T18 NMCNCT2-39: revisar y aplicar migración cuando se vaya a ejecutar la integración, registrando evidencia.
T19 NMCNCT2-40: implementar repositorio de acceso, servicio transaccional y handlers.
T20 NMCNCT2-41: construir login y navegación.
T21 NMCNCT2-42: automatizar pruebas y comprobar MySQL.
T22/T23 NMCNCT2-43/44: cierre de sesión y pruebas de acceso revocado.
