# T08 — Prueba de conexión Java / MySQL

Requisitos: Java 21, MySQL local iniciado, tabla de T07 creada y Connector/J
descargado según lib/README.md. Esta prueba no modifica datos.

## Ejecutar desde Git Bash en la raíz del repositorio

```bash
javac -encoding UTF-8 -d backend/out backend/src/com/inmoconecta/datos/ConexionBD.java backend/src/com/inmoconecta/PruebaConexion.java
export DB_USER=root
java -cp 'backend/out;backend/lib/*' com.inmoconecta.PruebaConexion
```

Escribe la contraseña en el aviso interactivo, nunca en el código ni en un
comando guardado en el historial. Si Git Bash no ofrece consola a Java, usa:

```bash
winpty java -cp 'backend/out;backend/lib/*' com.inmoconecta.PruebaConexion
```

En PowerShell, la asignación equivalente es `$env:DB_USER = 'root'`; el comando
java es el mismo. DB_PASSWORD es una alternativa por variable de entorno para
la ejecución del servidor. Los archivos .env no se cargan automáticamente.

Resultado esperado en la base recién creada:

```text
Conexión JDBC correcta. Base: inmoconecta
Usuarios existentes: 0
```

ConexionBD abre la conexión; PreparedStatement ejecuta la consulta y
try-with-resources cierra resultado, consulta y conexión incluso si ocurre un
error. Se usan tiempos máximos de conexión y lectura de cinco segundos.

La URL está limitada al servidor local. sslMode=REQUIRED exige cifrado, pero
no verifica la identidad del certificado: esta configuración es solo para
desarrollo local. No habilitamos recuperación de claves públicas.

root se usa aquí únicamente para comprobar tu instalación existente. Antes
de conectar el registro, preparar un usuario de aplicación con permisos
limitados sobre inmoconecta, sin usar root en el servidor web.

No se implementan todavía inserciones ni el endpoint de registro. El estado
/api/estado sigue comprobando únicamente HTTP. La prueba con la contraseña
real debe ejecutarse localmente antes de dar T08 por terminada.
