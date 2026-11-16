"use strict";

const formulario = document.querySelector("#formulario-registro");
const resultado = document.querySelector("#resultado");
const campos = Array.from(formulario.querySelectorAll("input, select"));
const contrasena = document.querySelector("#contrasena");
const mostrarClave = document.querySelector("#mostrar-clave");
const botonEnviar = formulario.querySelector('[type="submit"]');
const modoArchivo = location.protocol === "file:";
let enviando = false;
document.querySelector("#modo-registro").textContent = modoArchivo
  ? "Modo demostración: se validan los campos, pero no se crean cuentas. Para registrar, abre http://127.0.0.1:8080 con el servidor iniciado."
  : "Crea tu cuenta como comprador o agente. Tus datos se enviarán al servidor local.";
botonEnviar.textContent = modoArchivo ? "Validar registro" : "Crear cuenta";

// Devuelve un mensaje si el dato falla; una cadena vacía significa que es válido.
function validarCampo(campo) {
  const valor = campo.value.trim();
  if (!valor) return "Completa este campo.";

  switch (campo.name) {
    case "nombre":
    case "apellido":
      if (valor.length > 80) return "Escribe como máximo 80 caracteres.";
      break;
    case "telefono": {
      const digitos = valor.replace(/\D/g, "");
      if (!/^\+?[\d\s()-]+$/.test(valor) || digitos.length < 7 || digitos.length > 15) {
        return "Escribe un teléfono de 7 a 15 dígitos, con prefijo + opcional.";
      }
      break;
    }
    case "correo":
      if (valor.length > 254 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(valor)) {
        return "Escribe un correo válido, por ejemplo: tu@correo.com.";
      }
      break;
    case "contrasena":
      // No recortamos ni transformamos la contraseña elegida por la persona.
      if (campo.value.length < 12 || campo.value.length > 128) {
        return "La contraseña debe tener entre 12 y 128 caracteres.";
      }
      break;
    case "rol":
      if (!["COMPRADOR", "AGENTE"].includes(valor)) return "Selecciona uno de los roles disponibles.";
      break;
  }
  return "";
}

function mostrarError(campo) {
  const mensaje = validarCampo(campo);
  document.querySelector(`#error-${campo.id}`).textContent = mensaje;
  campo.setAttribute("aria-invalid", String(Boolean(mensaje)));
  return mensaje;
}

campos.forEach((campo) => {
  campo.addEventListener("blur", () => mostrarError(campo));
  campo.addEventListener("input", () => {
    resultado.textContent = "";
    resultado.className = "";
    if (campo.hasAttribute("aria-invalid")) mostrarError(campo);
  });
});

mostrarClave.addEventListener("click", () => {
  const visible = contrasena.type === "password";
  contrasena.type = visible ? "text" : "password";
  mostrarClave.textContent = visible ? "Ocultar" : "Mostrar";
  mostrarClave.setAttribute("aria-pressed", String(visible));
});

formulario.addEventListener("submit", async (evento) => {
  // Evita recargar la página o enviar contraseñas mediante la URL.
  evento.preventDefault();
  if (enviando) return;
  const camposInvalidos = campos.filter((campo) => mostrarError(campo));
  if (camposInvalidos.length) {
    resultado.className = "fallo";
    resultado.textContent = "Revisa los campos señalados antes de continuar.";
    camposInvalidos[0].focus();
    return;
  }

  if (modoArchivo) {
  resultado.className = "exito";
  resultado.textContent = "Los datos cumplen las validaciones del formulario. No se ha creado una cuenta: abre el sitio desde el servidor local para registrarte.";
  resultado.focus();
  return;
  }

  const datos = Object.fromEntries(campos.map(campo => [campo.name, campo.value]));
  enviando = true;
  botonEnviar.disabled = true;
  campos.forEach(campo => { campo.disabled = true; });
  formulario.setAttribute("aria-busy", "true");
  botonEnviar.textContent = "Creando cuenta…";
  resultado.className = "";
  resultado.textContent = "Enviando tus datos…";
  const controlador = new AbortController();
  const limite = setTimeout(() => controlador.abort(), 15000);
  let enfocar = resultado;
  try {
    const respuesta = await fetch("/api/usuarios", {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify(datos), signal: controlador.signal
    });
    const cuerpo = await respuesta.json();
    if (respuesta.status === 201 && cuerpo.codigo === "USUARIO_CREADO") {
      resultado.className = "exito";
      resultado.textContent = "Tu cuenta se creó correctamente. Ya estás registrado en InmoConecta. El inicio de sesión estará disponible en una próxima entrega.";
      formulario.reset();
      contrasena.type = "password";
      mostrarClave.textContent = "Mostrar";
      mostrarClave.setAttribute("aria-pressed", "false");
      campos.forEach(campo => campo.removeAttribute("aria-invalid"));
    } else {
      resultado.className = "fallo";
      if (respuesta.status === 409) {
        resultado.textContent = "Este correo ya está registrado. Utiliza otro correo.";
        document.querySelector("#error-correo").textContent = "Este correo ya está registrado.";
        document.querySelector("#correo").setAttribute("aria-invalid", "true");
        enfocar = document.querySelector("#correo");
      } else if (respuesta.status === 400) {
        resultado.textContent = "El servidor rechazó algunos datos. Revisa los campos indicados.";
        for (const campo of campos) {
          const mensaje = cuerpo.errores?.[campo.name];
          if (typeof mensaje === "string") {
            document.querySelector(`#error-${campo.id}`).textContent = mensaje;
            campo.setAttribute("aria-invalid", "true");
            if (enfocar === resultado) enfocar = campo;
          }
        }
      } else {
        resultado.textContent = "No pudimos completar el registro. Intenta nuevamente en unos momentos.";
      }
    }
  } catch {
    resultado.className = "fallo";
    resultado.textContent = "No pudimos confirmar el registro. Comprueba que el servidor esté activo. Si vuelves a intentarlo y el correo ya existe, el primer envío pudo completarse.";
  } finally {
    clearTimeout(limite);
    datos.contrasena = "";
    enviando = false;
    campos.forEach(campo => { campo.disabled = false; });
    botonEnviar.disabled = false;
    botonEnviar.textContent = "Crear cuenta";
    formulario.removeAttribute("aria-busy");
    enfocar.focus();
  }
});
