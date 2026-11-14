# T09 — Registro HTTP y JDBC
Avance sin Spring Boot. Se añade código al servidor Java 21 de T08.

## Arquitectura
RegistroHandler recibe JSON con Gson 2.14.0 en modo estricto, rechaza campos
extra, repetidos y no textuales y limita el cuerpo a 16 KiB.
ValidadorRegistro aplica las reglas de docs/contrato-registro.md.
HashContrasena usa la biblioteca criptográfica estándar del JDK
(SecretKeyFactory/PBKDF2WithHmacSHA256): 600 000 iteraciones, salt aleatorio de
16 bytes y resultado de 32 bytes. El formato guarda algoritmo, iteraciones,
salt y hash. No se escribe una implementación criptográfica propia.
UsuarioDao inserta con JDBC y PreparedStatement; el índice único resuelve
duplicados. RepositorioUsuarios permite simular solo la persistencia en pruebas.

Las respuestas solo incluyen id, nombre y rol, nunca el objeto interno completo.
El formulario continúa en modo demostración hasta T10.

## Compañero: probar sin MySQL
En Windows / Git Bash, desde la raíz:
```bash
bash backend/scripts/probar-t09.sh
```
El script descarga Gson desde Maven Central, compila todo y ejecuta las pruebas.
No se pide contraseña ni se usa MySQL. Requiere JDK 21 y acceso a Internet.
El JAR y backend/out están ignorados por Git.

## Sebastián: prueba real pendiente
1. Actualizar la rama del compañero después de publicar el PR.
2. Verificar que MySQL está iniciado y la tabla de T07 existe.
3. Como administrador, ejecutar database/002_usuario_aplicacion.sql UNA vez.
   MySQL mostrará una contraseña aleatoria; guardarla privadamente. Si la cuenta
   ya existe, detenerse y revisar, no eliminar cuentas.
4. Ejecutar las pruebas sin MySQL para compilar.
5. Ejecutar bash backend/scripts/iniciar-t09.sh y escribir la contraseña del
   usuario inmoconecta_app. El script no la guarda en un archivo ni historial.
6. En otra terminal, usar un correo ficticio nuevo y ejecutar:
```bash
curl -i http://127.0.0.1:8080/api/usuarios -H 'Content-Type: application/json' --data '{"nombre":"Ana","apellido":"Prueba","telefono":"+573001234567","correo":"t09-prueba@example.com","contrasena":"Solo prueba ficticia 123","rol":"COMPRADOR"}'
```
Esperar 201; repetir exactamente para comprobar 409. La contraseña de este
ejemplo es ficticia y no debe reutilizarse como contraseña real.
7. En Workbench:
```sql
SELECT id, correo, rol, contrasena_hash LIKE 'pbkdf2-sha256$600000$%' AS hash_codificado
FROM inmoconecta.usuarios WHERE correo = 't09-prueba@example.com';
```
Esperar una fila, hash_codificado = 1. No compartir el hash completo.
8. Reiniciar Java y consultar de nuevo: la fila debe seguir en MySQL.
9. Probar AGENTE, correo con mayúsculas/espacios, datos inválidos y un fallo de
   conexión controlado. Registrar resultados reales antes de integrar.
   La prueba crea datos ficticios; no borra ninguna fila automáticamente.

## Configuración y límites
DB_USER y DB_PASSWORD son variables de entorno. El servidor rechaza root al
arrancar; usa el usuario limitado. ConexionBD sigue usando MySQL local:3306,
cifrado obligatorio y tiempos máximos de conexión/lectura. No se conecta a una
base remota ni requiere compartir credenciales.
GET /api/estado conserva estado y registroDisponible; ahora el segundo es true
porque la ruta existe y se añade baseDatosVerificada:false. No es una prueba
de conexión MySQL.
POST /api/usuarios devuelve 201, 400, 409, 415, 405 y 500 según el contrato;
se añade 413/CUERPO_DEMASIADO_GRANDE para solicitudes de más de 16 KiB.
El contrato académico de contraseña de 12–128 unidades UTF-16 continúa pendiente
de confirmación docente; ajustar ambos lados si cambia.
Desarrollo local: no incluye login, CORS para el formulario, ni despliegue público.
El endpoint sirve la coincidencia exacta /api/usuarios y no sus subrutas.

## Dependencias y fuentes
- Gson 2.14.0: https://github.com/google/gson/releases
- Connector/J 9.7.0 ya definido en T08; el script de inicio lo descarga si falta.
- PBKDF2: https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
- Cuenta con contraseña generada: https://dev.mysql.com/doc/refman/8.4/en/create-user.html
PBKDF2 se eligió para usar el proveedor criptográfico de Java sin añadir
dependencias nativas; no se afirma cumplimiento FIPS.
Las instrucciones de arranque antiguas de backend/README.md compilaban solo
Servidor.java. Para este avance usar los scripts de T09 que compilan todas las clases.

## Jira
T09 / NMCNCT2-12 sigue pendiente de prueba real y revisión; no cerrar HU-01.
