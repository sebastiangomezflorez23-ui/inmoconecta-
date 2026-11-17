# Historias de usuario y plan de desarrollo de InmoConecta

Actualización posterior: el backlog ya fue creado y los PR #6/#7 están integrados. Consultar [claves y estado actualizado](jira-seguimiento.md). El diagnóstico siguiente conserva el corte anterior a esa comprobación.

Plan de continuidad • 17 de noviembre de 2026

El proyecto ya permite navegar desde el inicio hasta un registro conectado a Java y MySQL. El siguiente incremento debe incorporar inicio de sesión, cierre de sesión y permisos antes de permitir publicaciones reales. La página de inicio actual se conserva y evoluciona hacia el catálogo aprobado y el mapa descritos en el alcance original.


## Estado comprobado

| Área | Evidencia y situación |
| --- | --- |
| Registro | Formulario conectado; comprador y agente creados con HTTP 201 y duplicados rechazados con 409. Hash PBKDF2 con salt. Pendiente prueba documentada tras reiniciar Java y revisión de integración. |
| Inicio | Tres propiedades ficticias, filtros por operación y municipio, navegación y diseño adaptable. No es un catálogo persistente ni un mapa. |
| Pruebas | 53 comprobaciones del backend y suites de navegador. Se distinguen pruebas en memoria de las dos altas reales en MySQL. |
| Repositorio | Commits 3821b y af061 publicados en codex/hu02-inicio-demo. Publicación de la rama confirmada; integración en main no confirmada. |
| Jira | HU-01 y HU-02 En curso; T10 Listo; T11 En curso. T09 figura Por hacer aunque existe implementación y evidencia: debe corregirse tras revisar su cierre. |

## Alcance y restricciones

Se mantiene Java 21 con HttpServer, JDBC, MySQL, HTML, CSS y JavaScript, sin Spring Boot por indicación docente. La entrega actual es local. Pagos, firma de contratos, verificación legal de inmuebles y aplicación móvil nativa siguen fuera de alcance.

El alcance completo conserva mapa, publicaciones, favoritos, contacto y administración. La entrega local por etapas no equivale a cumplir todavía la nube ni el objetivo de disponibilidad del documento original.


## Comparación con los documentos originales

Jira_historias_de_usuario.docx define 17 historias. InmoConecta.docx exige búsqueda geográfica, publicaciones moderadas, contacto, favoritos y acceso seguro. La implementación actual cubre parte de ese alcance; una demo visual no sustituye estos requisitos.

| Requisito original | Situación y decisión |
| --- | --- |
| Registro y roles | Registro real implementado. COMPRADOR agrupa comprador y arrendatario; AGENTE agrupa agente y propietario. ADMIN será aprovisionado de forma controlada, nunca por autorregistro. |
| Login y bloqueo | Pendiente. Mantener bloqueo tras 3 fallos. Se propone duración de 15 minutos, a confirmar antes de implementar. |
| Mapa y filtros | Pendiente mapa. Los filtros demo no cubren rango de precio, tipo de inmueble, habitaciones ni zona visible. |
| Publicar y fotos | Pendiente modelo Propiedad, formularios, almacenamiento de imágenes y autorización por propietario. |
| Administración | Pendiente rol ADMIN, cola de revisión, aprobación, rechazo con motivo, edición y retirada. |
| Favoritos y contacto | Pendientes persistencia de favoritos, mensajes, bandeja del agente y enlace WhatsApp. |
| Nube y disponibilidad | Pendiente despliegue, HTTPS, monitoreo y respaldos. No afirmar 99 % en un entorno local. |
| UML y Figma | Los documentos los contemplan, pero esta revisión no acredita diagramas ni prototipos terminados. Planificar diseño y contrastarlo con lo construido. |

## Numeración sin perder trazabilidad

Se conservan HU-01 Registro y HU-02 Inicio de Jira. El login del documento original pasa a HU-03. Las nuevas claves NMCNCT2 serán asignadas por Jira al crear las historias; los números siguientes son identificadores funcionales propuestos, no tickets existentes.

| Documento original | Plan actualizado |
| --- | --- |
| HU-01 | HU-01 Registro existente |
| HU-02 y HU-03 | HU-03 Login y HU-04 Cerrar sesión |
| HU-04 y HU-05 | HU-09 Catálogo y HU-10 Mapa |
| HU-06 | HU-11 Detalle |
| HU-07 y HU-08 | HU-05 Publicar y HU-06 Fotografías |
| HU-09 y HU-10 | HU-07 Gestionar publicaciones propias |
| HU-11 y HU-12 | HU-15 Favoritos |
| HU-13 y HU-15 | HU-12 Enviar mensajes y HU-13 Bandeja |
| HU-14 | HU-14 WhatsApp |
| HU-16 y HU-17 | HU-08 Administración |

## Orden de implementación

Las etapas son incrementos verificables, no fechas prometidas. La duración y la distribución entre compañeros deben acordarse según disponibilidad. Trabajar una historia funcional a la vez y reservar tiempo para pruebas y revisión.

| Etapa | Trabajo | Resultado demostrable |
| --- | --- | --- |
| 0 | Cerrar evidencias HU-01 y HU-02; T04, T05, T09, T11 y T16 | Registro persistente tras reinicio; PR revisado; checklist actualizado. |
| 1 | HU-03 y HU-04; migraciones de roles y sesiones | Entrar y salir con permisos de comprador, agente y administrador. |
| 2 | HU-05, HU-06, HU-07 y HU-08 | Agente publica con fotos; administrador aprueba; otro agente no puede modificarla. |
| 3 | HU-09 y HU-11; después HU-10 | Inicio muestra datos aprobados de MySQL; filtros, detalle y mapa coordinados. |
| 4 | HU-12, HU-13 y HU-14 | Interesado contacta; agente ve mensajes; enlace WhatsApp funciona. |
| 5 | HU-15 | Favoritos privados que sobreviven al cierre de sesión. |
| 6 | Pruebas integrales, UML, despliegue y operación | Recorrido completo, evidencia docente y requisitos no funcionales medidos. |

## Dependencias técnicas que deben resolverse

- Migraciones incrementales: conservar usuarios y hashes existentes. Añadir estado de bloqueo y rol ADMIN; tablas de sesiones, propiedades, fotografías, mensajes y favoritos. No volver a ejecutar CREATE TABLE sobre tablas existentes.
- Separar operación VENTA o ARRIENDO de tipo de inmueble CASA o APARTAMENTO. Definir precio COP, área, habitaciones, baños, municipio, coordenadas y estado editorial.
- Las propiedades públicas solo pueden estar APROBADAS. Propietario y rol se obtienen de la sesión en el servidor, nunca de un identificador confiado al navegador.
- El frontend consumirá rutas del mismo origen. Las rutas privadas comprobarán sesión y permisos; los errores no expondrán SQL, contraseñas ni tokens.

## Primer paso al retomar

Reiniciar Java y ejecutar la verificación de persistencia ya preparada con frontend/tests/registro-mysql.cjs --persistencia. Revisar el PR de la rama publicada y cerrar pendientes de aceptación. A continuación comenzar HU-03; no construir publicaciones privadas sin autenticación.


## Historias y criterios de aceptación


### HU 01 Registro de usuario

Existente NMCNCT2-3 • En curso • Prioridad esencial

Como visitante quiero registrarme como comprador o agente para disponer de una cuenta.

- [VERIFICADO] El formulario valida seis campos y el servidor rechaza datos inválidos y rol ADMIN.
- [VERIFICADO] Alta real de ambos roles devuelve 201; repetir correo devuelve 409 sin nueva cuenta.
- [VERIFICADO] Contraseñas almacenadas con hash y salt; el navegador no confirma éxito antes del servidor.
- [PENDIENTE] Comprobar persistencia tras reiniciar y completar revisión de PR y reglas de aceptación.
Depende de la base y conexión existentes. Cerrar T04, T05, T09 y T11 según evidencia; no duplicar T10.


### HU 02 Página de inicio

Existente NMCNCT2-15 • En curso • Prioridad esencial

Como visitante quiero conocer la plataforma y sus opciones para comenzar la búsqueda y acceder al registro.

- [VERIFICADO] Identidad, propósito y navegación entre Inicio, Propiedades y Registro.
- [VERIFICADO] Catálogo ficticio identificado; filtros por operación y municipio con vacío y restablecer.
- [VERIFICADO] Diseño comprobado en cinco anchos, etiquetas y foco de teclado.
- [PENDIENTE] Revisión docente e integración de T16. El catálogo real se implementa en HU-09 sin duplicar esta historia.
Conservar T12 a T15 Listo. Mantener la apertura como archivo solo como demo; el registro real se abre desde Java.


## Historias y criterios de aceptación


### HU 03 Inicio de sesión y bloqueo

Nueva • Por hacer • Prioridad esencial

Como usuario registrado quiero iniciar sesión para acceder a las funciones autorizadas de mi cuenta.

- Correo y contraseña válidos generan una sesión y muestran opciones según el rol; no se devuelve el hash.
- Credenciales inválidas generan un mensaje genérico; tras 3 fallos consecutivos se bloquea temporalmente la cuenta. Duración propuesta: 15 minutos, pendiente de acuerdo.
- Login exitoso reinicia el contador. El bloqueo se valida en servidor y se conserva al reiniciar Java.
- Las rutas privadas rechazan sesión ausente o vencida; el token no se guarda en localStorage. Cookie HttpOnly y SameSite; Secure en HTTPS.
Depende de HU-01. Tareas: migración de bloqueo y sesiones; verificación de hash; endpoint login; pantalla y pruebas de roles, intentos y vencimiento.


### HU 04 Cierre de sesión

Nueva • Por hacer • Prioridad esencial para acceso seguro

Como usuario autenticado quiero cerrar mi sesión para proteger mi cuenta en equipos compartidos.

- Cerrar sesión invalida la sesión en servidor y elimina la cookie.
- Volver atrás o recargar una ruta privada no devuelve contenido protegido tras cerrar sesión.
- Una sesión expirada exige autenticarse otra vez sin perder la seguridad del servidor.
- La navegación muestra el estado correcto y permite volver al inicio público.
Depende de HU-03. Tareas: logout, vencimiento, navegación autenticada y pruebas de reutilización del token. Se eleva la prioridad Should original por seguridad.


## Historias y criterios de aceptación


### HU 05 Publicar una propiedad

Nueva • Por hacer • Prioridad esencial

Como agente quiero registrar una propiedad para ofrecerla a las personas que buscan vivienda.

- Solo AGENTE autenticado puede crear; el servidor asigna el propietario desde la sesión.
- Se validan título, descripción, precio positivo en COP, operación, tipo de inmueble, habitaciones, baños y ubicación.
- La ubicación incluye municipio y coordenadas válidas; el formulario final permite situarla en mapa, aunque el primer incremento use coordenadas.
- La publicación se guarda en MySQL como PENDIENTE y no aparece en búsquedas públicas. Se confirma únicamente después de guardar.
Depende de HU-03 y HU-04. Tareas: migración Propiedad, DAO, endpoint protegido, formulario y pruebas. El selector de mapa se completa antes del cierre final de esta historia.


### HU 06 Fotografías de la propiedad

Nueva • Por hacer • Prioridad esencial

Como agente quiero añadir y ordenar fotografías para mostrar el inmueble que publico.

- El agente solo puede subir fotos a una propiedad propia; archivos de otros usuarios no son modificables.
- Propuesta: 1 a 8 fotos JPEG o PNG, máximo 5 MB por foto; validar contenido y tamaño en servidor, con mensajes claros.
- Puede eliminar y reordenar imágenes antes de enviar la publicación; la primera es la portada.
- Archivos y orden persisten tras recargar. Nombres internos seguros y ubicación de almacenamiento separada del código.
Depende de HU-05. Tareas: tabla de fotos, subida segura, interfaz de orden, límites y manejo de fallos. Acordar límites antes de implementar.


## Historias y criterios de aceptación


### HU 07 Gestionar publicaciones propias

Nueva • Por hacer • Prioridad esencial del recorrido agente

Como agente quiero consultar y actualizar mis propiedades para mantener la oferta vigente.

- La lista muestra solo mis propiedades con estado PENDIENTE, APROBADA, RECHAZADA o RETIRADA; los rechazos incluyen motivo.
- Editar datos o fotografías de una publicación aprobada la devuelve a PENDIENTE para revisión.
- Retirar requiere confirmación; la propiedad deja de ser pública. Se propone baja lógica para conservar mensajes y favoritos.
- Cambiar el id de la petición no permite consultar datos privados ni editar o retirar propiedades de otro agente.
Depende de HU-05 y HU-06. Tareas: panel, actualización, retirada y pruebas de autorización. Equivale a HU-09 y HU-10 originales.


### HU 08 Moderar publicaciones

Nueva • Por hacer • Prioridad esencial

Como administrador quiero revisar y gestionar publicaciones para controlar qué contenido se muestra.

- Solo un ADMIN aprovisionado de forma controlada accede a la cola y a las acciones administrativas.
- Aprobar hace pública la propiedad; rechazar exige motivo visible al agente. La operación persiste en MySQL.
- Puede editar o retirar cualquier publicación con confirmación de retirada; se registra actor, fecha y acción.
- Una propiedad pendiente, rechazada o retirada no es visible por catálogo público ni por acceso directo a su detalle.
Depende de HU-03, HU-05 y HU-06. Tareas: migración ADMIN, alta controlada, panel, cambios de estado y auditoría. Mantener prohibido el autorregistro ADMIN.


## Historias y criterios de aceptación


### HU 09 Catálogo real y filtros

Nueva • Por hacer • Prioridad esencial

Como visitante quiero explorar y filtrar propiedades aprobadas para encontrar opciones que me interesen.

- Sin cuenta se consultan propiedades APROBADAS de MySQL; los datos ficticios no se presentan como ofertas reales.
- Se combinan municipio o zona, rango de precio, operación, tipo de inmueble y habitaciones; se valida mínimo menor o igual al máximo.
- Se muestran carga, fallo, resultados vacíos y restablecimiento; los resultados se paginan de forma estable.
- Cada tarjeta muestra portada, precio, ubicación y enlace a detalle; los mismos filtros se reutilizan al añadir el mapa.
Depende de HU-08 y reutiliza HU-02. Tareas: consulta parametrizada, endpoint paginado, conexión frontend y pruebas de filtros y visibilidad.


### HU 10 Búsqueda en mapa

Nueva • Por hacer • Prioridad esencial del alcance original

Como visitante quiero buscar en un mapa para encontrar propiedades dentro de una zona visible.

- El mapa permite mover y ampliar; consulta solo propiedades aprobadas dentro de los límites visibles.
- Lista y marcadores aplican los mismos filtros; seleccionar un marcador identifica la propiedad y permite ir al detalle.
- Sin resultados se puede limpiar filtros; si falla el proveedor del mapa se conserva una lista y un aviso comprensible.
- Validar búsquedas menores de 2 segundos bajo un escenario acordado. Propuesta: 1000 propiedades, 100 búsquedas, p95 y medición separada de carga del proveedor.
Depende de HU-09 y coordenadas de HU-05. Tareas: elegir proveedor según costo/licencia, consulta geográfica, mapa, sincronización y medición. La propuesta p95 no sustituye la aprobación docente del criterio original.


## Historias y criterios de aceptación


### HU 11 Detalle de propiedad

Nueva • Por hacer • Prioridad esencial

Como visitante quiero ver toda la información de una propiedad para decidir si deseo contactar al agente.

- Se muestran título, descripción, precio, operación, tipo, habitaciones, baños, fotos, municipio y ubicación en mapa.
- Solo se expone información pública del agente; nunca credenciales, hashes ni datos administrativos.
- Una propiedad no pública o inexistente no revela contenido y muestra un estado comprensible.
- Galería y navegación funcionan en móvil y teclado; las acciones de contacto se conectan con HU-12 y HU-14.
Depende de HU-06, HU-08 y HU-09; ubicación completa requiere HU-10. Tareas: endpoint público, vista detalle, galería, enlace a contacto y pruebas de acceso directo.


### HU 12 Enviar consulta al agente

Nueva • Por hacer • Prioridad esencial

Como interesado quiero enviar una consulta desde una propiedad para pedir información o una visita.

- El formulario solicita nombre, correo o teléfono de contacto y mensaje; valida campos y límites en servidor.
- El servidor asocia el mensaje a la propiedad aprobada y obtiene el agente propietario sin confiar en un id enviado por el cliente.
- Confirma recepción solo después de persistir; un error permite reintentar sin afirmar que se envió.
- Aplica límite de frecuencia y escape de contenido. Se propone permitir visitantes, sin exigir cuenta, conforme al alcance de contacto público.
Depende de HU-11. Tareas: tabla Mensaje, endpoint, validación, control de frecuencia y formulario. Confirmar acceso sin cuenta y límites de longitud.


## Historias y criterios de aceptación


### HU 13 Bandeja de consultas

Nueva • Por hacer • Prioridad esencial

Como agente quiero consultar mis mensajes para responder a las personas interesadas.

- Solo el propietario recibe acceso a los mensajes de sus propiedades.
- Cada mensaje muestra inmueble, remitente, contacto y fecha; distingue leído de no leído.
- Marcar leído persiste y no modifica mensajes de otros agentes; el contenido se presenta como texto seguro.
- Existe estado vacío y paginación. La bandeja no promete envío de correo ni chat en tiempo real.
Depende de HU-12 y HU-03. Tareas: listado privado, estado leído, bandeja y pruebas de propiedad.


### HU 14 Contacto por WhatsApp

Nueva • Por hacer • Prioridad deseable

Como interesado quiero abrir WhatsApp desde el detalle para contactar al agente.

- El botón usa el teléfono público válido del agente y un texto codificado con título y referencia del inmueble.
- Abre la aplicación o versión web disponible; no envía mensajes automáticamente.
- Si el teléfono no es válido, no muestra un enlace roto y permite usar el formulario de contacto.
- El enlace funciona desde móvil y escritorio sin exponer datos privados adicionales.
Depende de HU-11. Tareas: normalizar teléfono público, construir enlace y probar navegación. No requiere API de WhatsApp para este alcance.


## Historias y criterios de aceptación


### HU 15 Favoritos

Nueva • Por hacer • Prioridad deseable

Como usuario registrado quiero guardar y consultar favoritos para comparar propiedades después.

- Agregar o quitar favorito requiere sesión; sin sesión se ofrece iniciar sesión.
- Puede marcar desde mapa y detalle; la combinación usuario y propiedad es única y persiste en MySQL.
- La lista privada muestra portada, título, precio y ubicación; otros usuarios no acceden a ella.
- Si un inmueble se retira, el favorito indica que ya no está disponible sin exponer información no pública.
Depende de HU-03, HU-10 y HU-11. Tareas: tabla Favorito, endpoints privados, controles en mapa/detalle y listado. Reúne HU-11 y HU-12 originales.


### Criterio de cierre por historia

Una historia nueva comienza Por hacer. Los criterios anteriores son pendientes de implementación, salvo los marcados VERIFICADO en HU-01 y HU-02. El responsable debe adjuntar evidencia por criterio y resolver revisión antes de marcar Listo.


## Organización propuesta en Jira

Sí, este plan se puede organizar en InmoConecta2, proyecto NMCNCT2. Conservar las claves actuales, las tareas T01 a T16 y su historial. Crear HU-03 a HU-15 solo después de comprobar que no existan duplicados. Este documento prepara el backlog; no representa nuevas historias ya creadas en Jira.

| Épica | Historias |
| --- | --- |
| Cuentas y acceso existente NMCNCT2-2 | HU-01, HU-03 y HU-04 |
| Inicio y búsqueda | HU-02, HU-09, HU-10 y HU-11 |
| Publicación y administración | HU-05, HU-06, HU-07 y HU-08 |
| Contacto | HU-12, HU-13 y HU-14 |
| Favoritos | HU-15 |

## Reglas del tablero

- Usar Por hacer, En curso y Listo existentes. Añadir En revisión si se decide configurar el flujo; mientras tanto mantener En curso con etiqueta pendiente-revision.
- Cada historia incluye rol, objetivo, criterios, dependencias, tareas técnicas y evidencia. Crear subtareas para modelo/API, interfaz, pruebas y documentación cuando corresponda.
- Asignar nuevas tareas desde T17 sin renumerar las existentes. Jira generará claves NMCNCT2 independientes de los títulos T y HU.
- Enlazar dependencias con bloquea o depende de. El siguiente sprint toma el cierre de la etapa 0 y HU-03/HU-04 según capacidad; no meter todo el backlog en el sprint.
- Asignar personas por acuerdo. Quien no tenga MySQL puede implementar interfaz y pruebas simuladas; otra persona ejecuta la integración real. No atribuir pruebas reales a quien no las ejecutó.

## Correcciones concretas del estado actual

- T09 NMCNCT2-12 no debe permanecer Por hacer: hay código y pruebas. Revisar su criterio y actualizar a En curso o Listo según evidencia e integración.
- Actualizar HU-01: T10 está Listo y existen altas MySQL. T11 permanece En curso por reinicio y revisión. T05 conserva el acuerdo pendiente sobre reglas.
- Actualizar HU-02 y T16 con la rama ya publicada. No confundir push con merge ni marcar aceptación docente sin revisión.
- Las claves de las nuevas historias se registrarán junto a su identificador HU en este plan después de crear el backlog. No crear un proyecto adicional.

## Definición de hecho y entrega funcional

Una historia queda Listo cuando sus criterios están implementados, las pruebas apropiadas pasan, se documenta la evidencia, no hay errores críticos conocidos y el cambio fue revisado e integrado. Demo, simulación, MySQL real y aceptación docente deben identificarse por separado.

- Interfaz usable en móvil y escritorio, controles etiquetados, navegación de teclado, estados de carga, vacío y error.
- Validación y autorización en servidor, consultas parametrizadas, secretos fuera de Git y contraseñas con hash y salt.
- Migraciones conservan datos; operaciones inválidas no crean registros; se prueban permisos entre dos usuarios distintos.
- Pruebas integrales del recorrido: registrar → entrar → publicar con fotos → aprobar → buscar en lista/mapa → ver detalle → contactar → revisar mensaje → guardar favorito → salir.

## Nube y requisitos medibles

El objetivo original de 99 % de disponibilidad se evaluará solo tras desplegar. Propuesta: medir disponibilidad HTTP durante un mes de 30 días; 99 % permite hasta 7 h 12 min de indisponibilidad. Acordar ventana, exclusiones y monitoreo con el profesor. Una captura de servidor encendido no acredita disponibilidad mensual.

El requisito de búsquedas menores de 2 segundos exige datos y condiciones reproducibles. Documentar cantidad de propiedades, concurrencia, equipo, conexión, proveedor y percentiles. Antes de cerrar HU-10, acordar si el criterio aplica a cada búsqueda o a un percentil; no declarar cumplimiento con datos de ejemplo mínimos.

Planificar una tarea técnica de despliegue: elegir nube y costo, configurar HTTPS, sesiones seguras, secretos, MySQL y almacenamiento de imágenes persistentes, respaldos y prueba de restauración. Si la entrega del semestre se limita a local, registrar ese cambio de alcance con el profesor; no eliminar silenciosamente el requisito original.


## Decisiones antes del próximo incremento

- Confirmar duración de bloqueo y vencimiento de sesión; propuesta de bloqueo de 15 minutos tras 3 fallos.
- Confirmar límites de fotografías y mensajes, baja lógica de publicaciones y acceso de visitantes al contacto.
- Acordar proveedor de mapa y condiciones de evaluación de rendimiento; no es necesario contratarlo antes de construir sesiones.
- Confirmar fecha de entrega y cuáles etapas exige el profesor. Este plan no asigna plazos ni puntos sin estimación del equipo.

## Fuentes y evidencia de trabajo

Referencias: Jira_historias_de_usuario.docx e InmoConecta.docx aportados por el equipo; consulta del proyecto NMCNCT2; código de la rama codex/hu02-inicio-demo; docs/hu02-inicio-demo.md; docs/t10-integracion.md; docs/evidencias/t10/mysql-resultados.json. La persistencia tras reinicio y el merge siguen sin evidencia confirmada en este corte.

Continuación recomendada: verificar etapa 0, sincronizar Jira con este plan y comenzar HU-03. Mantener este documento junto al código como referencia del alcance y actualizarlo al cerrar cada incremento.
