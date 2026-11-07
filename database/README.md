# T07 — Modelo y tabla de usuarios

El script 001_crear_usuarios.sql prepara la base inmoconecta y su tabla usuarios.
Todavía no hay conexión JDBC ni creación de cuentas desde el formulario.

## Qué representa cada parte

- Usuario.java es un record: una clase Java de datos con campos inmutables y
  métodos de lectura como nombre() y correo(). Representa una fila guardada.
- Rol.java restringe los roles en Java a COMPRADOR y AGENTE.
- id identifica cada fila; AUTO_INCREMENT permite que MySQL lo asigne.
- NOT NULL impide valores nulos. No sustituye las validaciones de T09.
- UNIQUE impide repetir el correo ya normalizado por Java.
- telefono admite 15 dígitos más el signo +; no se guarda como número porque
  puede contener ceros iniciales.
- contrasena_hash reserva espacio para el hash codificado con salt y parámetros.
  Su algoritmo se elegirá en T09. No se incluyen usuarios ni claves de ejemplo.

La colación binaria compara el correo normalizado sin equiparar letras
acentuadas con letras sin acento. La normalización a minúsculas se hará en Java.
MySQL cuenta caracteres y Java unidades UTF-16: T09 aplicará los límites del
contrato antes de insertar. Usar modo SQL estricto para evitar truncamientos.

## Ejecutar paso a paso en MySQL Workbench

1. Abrir una conexión al servidor MySQL local.
2. Consultar `SELECT VERSION(), @@SESSION.sql_mode;` y comprobar que el modo
   contiene STRICT_TRANS_TABLES o STRICT_ALL_TABLES antes de continuar.
3. Abrir 001_crear_usuarios.sql mediante File > Open SQL Script.
4. Ejecutar el script completo una sola vez en el entorno de desarrollo.
5. Ejecutar estas consultas de comprobación:

```sql
SHOW CREATE TABLE inmoconecta.usuarios;
SHOW INDEX FROM inmoconecta.usuarios;
SELECT COUNT(*) AS total_usuarios FROM inmoconecta.usuarios;
```

En una instalación nueva se espera una tabla vacía y el índice único
uq_usuarios_correo con Non_unique = 0.

Si usuarios ya existe, CREATE TABLE fallará deliberadamente. Revisar su
estructura antes de hacer cambios; no borrar datos para repetir este paso.

## Compilar el modelo desde la raíz del repositorio

```text
javac -encoding UTF-8 -d backend/out backend/src/com/inmoconecta/modelo/Rol.java backend/src/com/inmoconecta/modelo/Usuario.java
```

## Verificación local realizada

El script se ejecutó mediante MySQL Workbench en MySQL Community Server 8.4.9,
con STRICT_TRANS_TABLES activo. Antes de ejecutarlo se comprobó que la base
inmoconecta no existía.

- SHOW COLUMNS devolvió las siete columnas previstas, todas NOT NULL.
- id tiene clave primaria y AUTO_INCREMENT.
- SHOW INDEX mostró uq_usuarios_correo sobre correo, con Non_unique = 0.
- SELECT COUNT(*) devolvió 0: la tabla se creó vacía.
- El modelo Java compiló correctamente con javac.

Pendiente publicar y revisar este avance mediante commit y pull request.
T08 conectará Java con MySQL y T09 aplicará las reglas y el hash antes de
guardar usuarios. Estas comprobaciones no prueban todavía el registro completo.
