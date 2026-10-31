# Servidor de InmoConecta

Base académica en Java 21 con HttpServer del JDK, sin framework web ni Maven.
El acceso futuro a MySQL será mediante JDBC y MySQL Connector/J.

## Ejecutar desde la raíz del repositorio

En Git Bash o PowerShell:

```text
javac --add-modules jdk.httpserver -encoding UTF-8 -d backend/out backend/src/com/inmoconecta/Servidor.java
java --add-modules jdk.httpserver -cp backend/out com.inmoconecta.Servidor
```

Abre http://127.0.0.1:8080/api/estado en el navegador. Respuesta esperada:

```json
{"estado":"activo","registroDisponible":false}
```

Detén con Ctrl+C. Si el puerto está ocupado, añade otro puerto al comando:

```text
java --add-modules jdk.httpserver -cp backend/out com.inmoconecta.Servidor 8081
```

## Explicación

- Servidor.java inicia el servidor HTTP y atiende la ruta de comprobación.
- javac compila; java ejecuta. Las clases en out se excluyen de Git.
- GET /api/estado comprueba HTTP, no la conexión a MySQL.
- Una ruta inexistente devuelve 404; un método no admitido devuelve 405.
- El servidor escucha solo en el equipo local.

El formulario se sigue abriendo desde frontend/index.html. Todavía no se conecta
al servidor. No hay registro, autenticación ni persistencia implementados.

## Continuación paso a paso

1. T05: acordar contrato HTTP, campos JSON, roles, límites y errores.
2. T07: crear Usuario y el esquema MySQL con correo único.
3. T08: incorporar Connector/J y configurar JDBC mediante variables de entorno.
   Usar PreparedStatement y try-with-resources; no subir credenciales.
4. T09: separar atención HTTP, validación y acceso a datos en clases Java.
   Aplicar hash con salt mediante biblioteca; usar un analizador JSON mantenido,
   no expresiones regulares para interpretar JSON.
5. T10: conectar fetch y configurar origen permitido si se sirve por separado.
6. T11: probar ambos roles, duplicados, errores y persistencia.

La base local debe revisarse y publicarse mediante commit y PR antes de cerrar T06.
