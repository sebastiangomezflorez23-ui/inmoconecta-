"use strict";

// Catálogo ficticio local: no consulta servicios ni almacena datos personales.
const propiedades = [
  { nombre: "Apartamento entre montañas", municipio: "Medellín", barrio: "El Poblado", operacion: "venta", precio: 485000000, habitaciones: 3, banos: 2, area: 92, imagen: "apartamento.svg" },
  { nombre: "Un nuevo comienzo en Laureles", municipio: "Medellín", barrio: "Laureles", operacion: "arriendo", precio: 2400000, habitaciones: 2, banos: 2, area: 68, imagen: "interior.svg" },
  { nombre: "Casa con espacio para crecer", municipio: "Envigado", barrio: "El Dorado", operacion: "venta", precio: 620000000, habitaciones: 3, banos: 3, area: 145, imagen: "casa.svg" }
];
const catalogo = document.querySelector("#lista-propiedades");
const operacion = document.querySelector("#operacion");
const municipio = document.querySelector("#municipio");
const contador = document.querySelector("#contador");
const vacio = document.querySelector("#sin-resultados");
const pesos = new Intl.NumberFormat("es-CO", { style: "currency", currency: "COP", maximumFractionDigits: 0 });

function mostrarPropiedades() {
  const visibles = propiedades.filter(p => (!operacion.value || p.operacion === operacion.value) && (!municipio.value || p.municipio === municipio.value));
  // Solo se interpola el catálogo fijo definido arriba, nunca datos introducidos por visitantes.
  catalogo.innerHTML = visibles.map(p => `<article class="property">
    <div class="property-visual"><img src="assets/${p.imagen}" alt="Ilustración de ${p.nombre.toLowerCase()}" width="600" height="360"><span class="badge">${p.operacion === "venta" ? "En venta" : "En arriendo"}</span></div>
    <div class="property-body"><p class="location">${p.barrio} · ${p.municipio}</p><h3>${p.nombre}</h3>
    <div class="specs"><span>${p.habitaciones} habitaciones</span><span>${p.banos} baños</span><span>${p.area} m²</span></div>
    <p class="price">${pesos.format(p.precio)} <small>${p.operacion === "arriendo" ? "/ mes" : "COP"}</small></p><p class="sample">Propiedad ficticia · Demostración</p></div></article>`).join("");
  contador.textContent = `${visibles.length} ${visibles.length === 1 ? "propiedad de ejemplo" : "propiedades de ejemplo"}`;
  vacio.hidden = visibles.length !== 0;
}
operacion.addEventListener("change", mostrarPropiedades);
municipio.addEventListener("change", mostrarPropiedades);
document.querySelector("#restablecer").addEventListener("click", () => {
  operacion.value = "";
  municipio.value = "";
  mostrarPropiedades();
});
mostrarPropiedades();
