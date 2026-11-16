# T10 — Formulario conectado al registro Java

## Ejecutar en Git Bash

Desde la raíz del proyecto:

```bash
bash backend/scripts/iniciar-t09.sh
```

Introduce la contraseña de `inmoconecta_app` en la terminal. El script descarga las bibliotecas si faltan, compila el código actual y arranca Java. No guarda la contraseña en archivos. Deben estar instalados Java 21 y MySQL, con la base y el usuario de aplicación configurados según database/README.md.

Abre **http://127.0.0.1:8080/**. Inicio, recursos y formulario los sirve Java. No uses doble clic ni Live Server para el registro real: el envío es al mismo origen `/api/usuarios`. Al abrir como archivo se conserva la validación demo sin envío.

## Comportamiento implementado

- Validación de campos antes del envío; contraseña sin recortar.
- POST JSON con los seis campos del contrato.
- Botón y campos deshabilitados durante el envío; evita envíos simultáneos.
- HTTP 201 y código USUARIO_CREADO: confirma creación y limpia formulario/contraseña.
- HTTP 409: señala correo repetido y enfoca ese campo.
- HTTP 400: presenta errores del servidor por campo usando texto, sin interpretar HTML.
- HTTP 500: mensaje general, sin detalles internos.
- Error de red, respuesta ilegible o tiempo agotado: no afirma éxito; permite reintentar y avisa que el primer intento pudo completarse.
- No almacena contraseñas en navegador ni archivos. Inicio de sesión pendiente.
- Java sirve únicamente una lista explícita de recursos; no publica .env, fuentes, pruebas ni archivos del repositorio.

## Verificación realizada

- Compilación Java correcta.
- 53 comprobaciones T09 satisfactorias (repositorio simulado).
- Suite `frontend/tests/inicio.cjs`: filtros, teclado, navegación, validación demo, cinco anchos y ausencia de solicitudes externas al abrir archivo.
- Suite `frontend/tests/registro-http.cjs`: navegador contra handlers Java reales y repositorio en memoria. 201 para ambos roles, 409 duplicado, 400 por campo, 500, fallo de red y envío único; rutas privadas devuelven 404, POST a archivo 405 y HEAD 200.
- Captura de prueba HTTP en `docs/evidencias/t10/registro-http-prueba.png`. Usa repositorio de prueba, no acredita persistencia MySQL.

Para repetir las suites de navegador se necesitan Node, Playwright y Chrome. Compilar primero `backend/src` y `backend/test`; ejecutar `node frontend/tests/registro-http.cjs`. Se puede indicar JAVA_EXE si Java no está en PATH. Las suites cierran su navegador y servidor de prueba al terminar.

## Verificación MySQL de esta integración

Verificado con frontend/tests/registro-mysql.cjs contra el servidor real del usuario: COMPRADOR id 3 y AGENTE id 5 creados con HTTP 201. Tras recargar el formulario y repetir cada correo, ambos recibieron HTTP 409. Contraseña del formulario vacía tras éxito. Resultados sin contraseñas en docs/evidencias/t10/mysql-resultados.json y capturas mysql-comprador.png y mysql-agente.png. Se conservaron los dos usuarios ficticios de prueba. Pendiente comprobar tras reiniciar Java y revisión de integración.

El catálogo continúa ficticio. Esta entrega implementa el recorrido de registro de HU-01; no incluye autenticación, publicaciones reales ni contacto con agentes.

