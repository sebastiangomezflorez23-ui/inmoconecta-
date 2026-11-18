# InmoConecta

Proyecto académico de una plataforma web para publicar y buscar
propiedades en venta o arriendo.

## Tecnologías previstas

- Interfaz: HTML, CSS y JavaScript.
- Servidor: Java 21 con HttpServer del JDK y acceso a MySQL mediante JDBC.
- Base de datos: MySQL.

## Inicio — HU-02 (demo visible)

Abre `frontend/index.html` con doble clic. Incluye catálogo ficticio, filtros por operación y municipio, diseño adaptable y acceso al formulario. Funciona sin MySQL ni internet.

Consulta [historia, checklist y guion](docs/hu02-inicio-demo.md) y las capturas en `docs/evidencias/hu02/`.

## Primer avance — HU-01 Registro de usuario

Se construyó un formulario con:
- Nombre y apellido.
- Teléfono.
- Correo electrónico.
- Contraseña.
- Rol: comprador/arrendatario o agente/propietario.

## Estado actual

Formulario adaptable a móvil y escritorio, con validaciones en JavaScript,
mensajes por campo y opción para mostrar u ocultar la contraseña.
Cuando se abre como archivo, es una demostración: no crea cuentas ni envía datos.
El backend Java/JDBC incluye registro y el formulario ya se conecta al abrirlo desde el servidor. Consulta [T10: ejecución y pruebas](docs/t10-integracion.md). La apertura como archivo conserva la demostración sin envío.

HU-03 incorpora inicio de sesión y página de cuenta, con bloqueo por intentos y cookie de sesión. Pruebas HTTP y navegador con repositorio simulado verificadas; la migración 003 y las pruebas MySQL del acceso siguen pendientes. Consulta [ejecución y checklist HU-03](docs/pruebas-hu03.md). El cierre manual de sesión se implementará en HU-04.

## Cómo probar este avance

Abre `frontend/registro.html` en tu navegador para probar HU-01. No requiere instalaciones ni servidor.
También puedes abrirlo con Live Server desde VS Code si ya tienes esa extensión.

1. Pulsa “Validar registro” con el formulario vacío: aparecen errores y el foco
   se ubica en el primer campo pendiente.
2. Prueba un correo sin arroba, un teléfono con letras y una contraseña corta:
   cada campo muestra el error correspondiente.
3. Completa nombre y apellido, teléfono `3001234567`, correo `prueba@example.com`,
   una contraseña ficticia de 12 o más caracteres y uno de los roles.
4. Al validar, debe indicar que los datos son válidos, pero que NO creó una cuenta.
5. Repite con el otro rol; comprueba el botón Mostrar/Ocultar y navega con Tab.
6. Reduce el ancho de la ventana: los campos deben acomodarse sin desplazamiento horizontal.

Usa datos ficticios durante las pruebas.

## Reglas propuestas para acordar con el compañero

- Todos los campos son obligatorios; nombre y apellido admiten hasta 80 caracteres.
- Teléfono: entre 7 y 15 dígitos, con espacios, guiones, paréntesis y prefijo + opcional.
- Correo: formato con dominio y máximo 254 caracteres.
- Contraseña: entre 12 y 128 caracteres, sin modificar los espacios de la contraseña.
- Roles técnicos: `COMPRADOR` y `AGENTE`. El registro público no ofrece administrador.

Estas reglas deben repetirse en el servidor: las validaciones del navegador se pueden eludir.
La longitud de contraseña es una propuesta de este avance, pendiente de acuerdo del equipo.

## Plan original del backend (ver estado actualizado en Jira)

1. Ejecutar el servidor Java 21 y preparar la conexión a MySQL mediante JDBC y configuración local.
2. Acordar el contrato del endpoint de registro antes de conectar el formulario.
3. Crear la tabla de usuarios con correo único y almacenamiento del hash de contraseña.
4. Validar los datos y roles en el servidor, rechazar correos repetidos y generar
   hash con salt mediante una biblioteca apropiada.
5. Conectar el formulario mediante `fetch` y manejar respuestas de éxito y error.
6. Probar la integración completa antes de marcar HU-01 como terminada.

El archivo `frontend/js/registro.js` contiene comentarios para identificar validación,
mensajes, visibilidad de contraseña y envío del formulario.

## Organización

- frontend/: interfaz de usuario.
- backend/: servidor Java.
