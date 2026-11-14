# Evidencia T09 — 14/11/2026

Compilación realizada con Java 21 y Gson 2.14.0.
Pruebas automatizadas: 53 comprobaciones correctas contra un servidor HTTP
temporal en loopback y un repositorio simulado en memoria.

Comprobado:
- Campos obligatorios, roles, longitudes, normalización y clave sin recortar.
- Salt aleatorio, verificación del hash y rechazo de clave incorrecta.
- Respuestas 201 para ambos roles y 409 simulado para correo repetido.
- Rechazo de null, números, campos extra/repetidos y JSON mal formado.
- Límite 16 KiB, Content-Type, método HTTP y cabecera Allow.
- Respuesta 500 ante fallo SQL simulado, sin detalles internos.
- Dos peticiones simultáneas: 201 y 409 con repositorio simulado.
- Ninguna inserción simulada adicional en casos inválidos y ruta exacta.

Pendiente:
- Descargar y ejecutar el paquete en el equipo del compañero.
- Crear la cuenta MySQL limitada en el equipo de Sebastián.
- Verificar UsuarioDao con Connector/J y MySQL real.
- Correo único concurrente y persistencia después de reiniciar contra MySQL real.
- Registrar resultados de esas pruebas y revisar el PR.
No se han ejecutado migraciones ni creado usuarios MySQL desde este paquete.
No se han creado cuentas reales ni modificado el frontend.
