'use strict';
const formularioAcceso = document.querySelector('#formulario-acceso');
const resultadoAcceso = document.querySelector('#resultado');
const datosCuenta = document.querySelector('#datos-cuenta');
let enviandoAcceso = false;
let revisionSesion = 0;
function informarAcceso(texto, fallo = false) {
  resultadoAcceso.textContent = texto;
  resultadoAcceso.className = fallo ? 'fallo' : '';
}
async function solicitarAcceso(ruta, opciones = {}) {
  const respuesta = await fetch(ruta, { credentials: 'same-origin', cache: 'no-store', ...opciones, signal: AbortSignal.timeout(15000) });
  const datos = await respuesta.json();
  return { respuesta, datos };
}
document.querySelector('#mostrar-clave')?.addEventListener('click', (evento) => {
  const campo = document.querySelector('#contrasena');
  const mostrar = campo.type === 'password';
  campo.type = mostrar ? 'text' : 'password';
  evento.currentTarget.textContent = mostrar ? 'Ocultar' : 'Mostrar';
  evento.currentTarget.setAttribute('aria-pressed', String(mostrar));
});
formularioAcceso?.addEventListener('submit', async (evento) => {
  evento.preventDefault();
  if (enviandoAcceso) return;
  if (!['http:', 'https:'].includes(location.protocol)) {
    informarAcceso('Abre InmoConecta desde el servidor local para iniciar sesión.', true); return;
  }
  const correo = formularioAcceso.elements.correo.value.trim().toLowerCase();
  const contrasena = formularioAcceso.elements.contrasena.value;
  enviandoAcceso = true;
  const controles = [...formularioAcceso.elements];
  controles.forEach(control => { control.disabled = true; });
  formularioAcceso.setAttribute('aria-busy', 'true');
  informarAcceso('Comprobando tus datos…');
  try {
    const { respuesta, datos } = await solicitarAcceso('/api/login', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ correo, contrasena }) });
    if (respuesta.ok && datos.codigo === 'SESION_INICIADA') {
      formularioAcceso.reset(); location.assign('cuenta.html'); return;
    }
    informarAcceso(datos.mensaje || 'No se pudo iniciar sesión. Inténtalo más tarde.', true);
    resultadoAcceso.focus();
  } catch {
    informarAcceso('No se pudo confirmar el acceso. Comprueba que el servidor esté disponible y vuelve a intentar.', true);
    resultadoAcceso.focus();
  } finally {
    enviandoAcceso = false;
    controles.forEach(control => { control.disabled = false; });
    formularioAcceso.removeAttribute('aria-busy');
  }
});
async function comprobarCuenta() {
  if (!datosCuenta) return;
  const revision = ++revisionSesion;
  datosCuenta.hidden = true;
  document.querySelector('#saludo').textContent = '';
  document.querySelector('#rol-cuenta').textContent = '';
  try {
    const { respuesta, datos } = await solicitarAcceso('/api/sesion');
    if (revision !== revisionSesion) return;
    if (!respuesta.ok) { informarAcceso(respuesta.status === 401 ? 'Tu sesión no está disponible. Inicia sesión para continuar.' : 'No se pudo comprobar la sesión. Inténtalo más tarde.', true); return; }
    const roles = { COMPRADOR: ['Comprador / arrendatario', 'Puedes explorar el catálogo de demostración. Los favoritos y las consultas estarán disponibles en próximos avances.'], AGENTE: ['Agente / propietario', 'La publicación y gestión de propiedades estarán disponibles en próximos avances.'], ADMIN: ['Administrador', 'La moderación de publicaciones estará disponible en próximos avances.'] };
    const rol = roles[datos.usuario?.rol];
    if (!rol) throw new Error('Rol no reconocido');
    document.querySelector('#saludo').textContent = `Hola, ${datos.usuario.nombre}`;
    document.querySelector('#rol-cuenta').textContent = `Tu rol: ${rol[0]}`;
    document.querySelector('#siguiente-rol').textContent = rol[1];
    datosCuenta.hidden = false;
    informarAcceso('Sesión verificada.');
  } catch { if (revision === revisionSesion) informarAcceso('No se pudo comprobar la sesión. Revisa tu conexión.', true); }
}
if (datosCuenta) {
  window.addEventListener('pageshow', comprobarCuenta);
  window.addEventListener('pagehide', () => { revisionSesion++; datosCuenta.hidden = true; });
  document.addEventListener('visibilitychange', () => { if (!document.hidden) comprobarCuenta(); });
  setInterval(comprobarCuenta, 60000);
}
