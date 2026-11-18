# Migración local HU-03 — 18/11/2026

Ejecutada desde la conexión administrativa ya abierta en MySQL Workbench,
InmoConecta local, MySQL 8.4.9, localhost:3306.

## Antes

- Tabla usuarios con siete columnas originales; rol COMPRADOR/AGENTE.
- No existía tabla sesiones ni columnas de bloqueo.
- Cuatro usuarios existentes.

## Respaldo local

Se creó inmoconecta_respaldo_20261118_hu03.usuarios mediante CREATE TABLE LIKE
e INSERT SELECT. Conteos verificados: originales 4, respaldo 4.
Es una copia en la misma instancia, no un respaldo externo contra pérdida del disco.
Los datos y hashes permanecen en MySQL; no se exportaron a Git.

## Aplicación y comprobación

Se ejecutó database/003_preparar_acceso.sql una vez.

- Usuarios: nueve columnas; rol admite ADMIN; intentos_fallidos tinyint unsigned
  NOT NULL DEFAULT 0; bloqueado_hasta datetime(6) nullable.
- Tabla sesiones creada, con cero sesiones al terminar.
- SHOW GRANTS de inmoconecta_app@localhost: SELECT/INSERT/DELETE sobre sesiones;
  SELECT/INSERT sobre usuarios y UPDATE restringido a intentos_fallidos/bloqueado_hasta.
- Comparación posterior de id, nombre, apellido, teléfono, correo, hash y rol
  entre original y respaldo: usuarios 4, respaldo 4, filas intactas 4.
- No se crearon cuentas ADMIN ni se cambiaron contraseñas.

## Pendiente

Esta evidencia valida migración y conservación de datos. No reemplaza la ejecución
de PruebasAccesoMysql, las pruebas de navegador contra MySQL ni el reinicio de Java.
La contraseña de la aplicación no está disponible en la terminal del asistente.
Ejecutar en Git Bash `bash backend/scripts/probar-hu03-mysql.sh` y escribirla solo
en la terminal. No repetir la migración 003 en esta base.
