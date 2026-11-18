# Servidor de InmoConecta

Java 21 HttpServer, JDBC y MySQL; sin framework web ni Maven.

## Ejecutar desde Git Bash en la raíz

```bash
bash backend/scripts/iniciar-t09.sh
```

El script compila el servidor, prepara los JAR necesarios y solicita la contraseña
sin mostrarla. Usa inmoconecta_app, nunca root. Abre http://127.0.0.1:8080/.
Detén el servidor con Ctrl+C. No publiques credenciales en Git.

## Funciones y requisitos

- Inicio y catálogo ficticio, registro con validación y persistencia MySQL.
- HU-03: login y consulta de sesión implementados; validación MySQL pendiente.
- El registro requiere esquema 001 y permisos 002 ya configurados.
- El login requiere aplicar una sola vez database/003_preparar_acceso.sql,
  después de respaldo y revisión administrativa. No repetir la migración.
- GET /api/estado confirma que HTTP está activo; no verifica la base de datos.
- El servidor escucha únicamente en 127.0.0.1; sirve frontend desde el mismo origen.
- El catálogo continúa siendo una demo. Logout queda pendiente en HU-04.

## Pruebas

```bash
bash backend/scripts/probar-t09.sh
java -cp 'backend/out;backend/lib/*' com.inmoconecta.PruebasAcceso
```

El primer script compila fuentes y pruebas. Para HU-03 con MySQL real:

```bash
bash backend/scripts/probar-hu03-mysql.sh
```

Requiere migración 003 y JAR locales. Crea dos usuarios ficticios de evidencia;
no modifica usuarios anteriores. No se ha ejecutado aún en la base del usuario.

Consulta ../docs/pruebas-hu03.md para evidencias y verificaciones pendientes.