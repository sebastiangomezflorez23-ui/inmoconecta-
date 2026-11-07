# T05 — Contrato de registro de usuarios (HU-01)

Este documento define el comportamiento previsto para conectar el formulario,
el servidor Java y MySQL. No significa que el registro esté implementado:
actualmente solo funciona GET /api/estado y el formulario es una demostración.

## Solicitud

Ruta propuesta: POST /api/usuarios. Cuerpo JSON con Content-Type: application/json.
Todos los campos son obligatorios y de tipo texto; se rechazan valores null,
campos desconocidos y tipos distintos de texto. No enviar datos en la URL.

```json
{
  "nombre": "Ana",
  "apellido": "Gomez",
  "telefono": "+57 300 123 4567",
  "correo": "ana@example.com",
  "contrasena": "Ejemplo solo para pruebas 123",
  "rol": "COMPRADOR"
}
```

Los datos del ejemplo son ficticios.

## Reglas

| Campo | Regla del contrato |
| --- | --- |
| nombre | Quitar espacios exteriores; entre 1 y 80 unidades UTF-16. Permitir tildes y nombres compuestos. |
| apellido | Misma regla que nombre. |
| telefono | Quitar espacios exteriores; máximo 25 unidades UTF-16; de 7 a 15 dígitos ASCII. Se permiten espacios, paréntesis, guiones y un + solo al principio. Guardar sin separadores, conservando el + si existe. |
| correo | Quitar espacios exteriores; máximo 254 unidades UTF-16; sin espacios, una @ y dominio con punto. Normalizar a minúsculas para almacenamiento y unicidad, como decisión de este proyecto. |
| contrasena | Entre 12 y 128 unidades UTF-16; rechazar si solo contiene espacios. No recortar ni transformar antes de generar el hash. |
| rol | Valores exactos COMPRADOR o AGENTE, sin espacios adicionales. COMPRADOR representa comprador/arrendatario; AGENTE representa agente/propietario. |

Las longitudes usan la medida de String.length de JavaScript y Java para que
ambos lados coincidan; algunos símbolos ocupan dos unidades. La longitud de
contraseña es una propuesta académica pendiente de validación con el profesor.

El servidor debe repetir todas las validaciones aunque el formulario ya las
haya comprobado. La normalización del correo y teléfono, el límite telefónico
en JavaScript y el envío de JSON se implementarán en las tareas posteriores.

## Respuestas

Todas las respuestas llevan Content-Type: application/json; charset=utf-8.

| HTTP | codigo | Cuándo se devuelve |
| --- | --- | --- |
| 201 | USUARIO_CREADO | La inserción en MySQL terminó correctamente. |
| 400 | DATOS_INVALIDOS | Campos ausentes, desconocidos, tipos o valores inválidos. |
| 400 | JSON_INVALIDO | El cuerpo no se puede interpretar como un objeto JSON. |
| 409 | CORREO_DUPLICADO | Ya existe el correo normalizado. |
| 415 | TIPO_CONTENIDO_NO_ADMITIDO | El cuerpo no se declara como application/json; se permite charset UTF-8. |
| 405 | METODO_NO_PERMITIDO | La ruta de registro recibe otro método; incluir Allow: POST. |
| 500 | ERROR_INTERNO | Falla inesperada o de base de datos; no confirmar creación. |

Ejemplo de éxito:

```json
{
  "codigo": "USUARIO_CREADO",
  "mensaje": "Usuario registrado correctamente.",
  "usuario": { "id": 1, "nombre": "Ana", "rol": "COMPRADOR" }
}
```

Ejemplo de validación:

```json
{
  "codigo": "DATOS_INVALIDOS",
  "mensaje": "Revisa los campos indicados.",
  "errores": { "correo": "Escribe un correo válido." }
}
```

Los demás errores devuelven codigo y mensaje; no devuelven usuario ni datos
recibidos. Los nombres de errores por campo coinciden con la solicitud.

## Persistencia y seguridad previstas

- MySQL asigna el identificador y aplica una restricción UNIQUE al correo
  normalizado. Dos solicitudes simultáneas no deben crear correos duplicados.
- Guardar hash de contraseña con salt mediante una biblioteca apropiada,
  nunca la contraseña en texto plano. Elegir algoritmo y parámetros en T09.
- Usar consultas preparadas JDBC y credenciales en variables de entorno.
- No devolver ni registrar contraseñas, hashes, credenciales o detalles SQL.
- No crear sesión ni iniciar sesión automáticamente: esta historia solo registra.
- La demostración HTTP actual es local; una publicación real requiere HTTPS.

## Comprobaciones para T11

1. Datos válidos con COMPRADOR y con AGENTE: 201 y una fila por usuario.
2. Campo vacío, null, tipo incorrecto, rol inválido o campo desconocido: 400, sin inserción.
3. Probar límites de nombres, teléfono, correo y contraseña.
4. Repetir correo con mayúsculas o espacios exteriores: 409, sin duplicación.
5. Dos solicitudes simultáneas con el mismo correo: solo una inserción.
6. JSON mal formado: 400; tipo de contenido incorrecto: 415; método incorrecto: 405.
7. Base de datos no disponible: error sin datos internos ni mensaje de éxito.
8. Verificar que la base guarda hash, y que las respuestas no contienen secretos.

## Orden de implementación

T07 crea el modelo y la tabla; T08 conecta JDBC; T09 implementa este contrato;
T10 adapta el formulario y sus mensajes; T11 comprueba el flujo completo.
