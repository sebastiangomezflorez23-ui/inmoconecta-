"use strict";

const formulario = document.querySelector("#formulario-registro");
const resultado = document.querySelector("#resultado");
const campos = Array.from(formulario.querySelectorAll("input, select"));
const contrasena = document.querySelector("#contrasena");
const mostrarClave = document.querySelector("#mostrar-clave");

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

formulario.addEventListener("submit", (evento) => {
  // Evita recargar la página o enviar contraseñas mediante la URL.
  evento.preventDefault();
  const camposInvalidos = campos.filter((campo) => mostrarError(campo));
  if (camposInvalidos.length) {
    resultado.className = "fallo";
    resultado.textContent = "Revisa los campos señalados antes de continuar.";
    camposInvalidos[0].focus();
    return;
  }

  // Esta demostración no envía ni almacena datos, contraseñas o cuentas.
  resultado.className = "exito";
  resultado.textContent = "Los datos cumplen las validaciones del formulario. No se ha creado una cuenta: falta conectar el servidor Java y MySQL.";
  resultado.focus();
});
