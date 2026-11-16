// Ejecutar con Node y Playwright instalado: node frontend/tests/inicio.cjs
const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const path = require('node:path');
const fs = require('node:fs');
const { pathToFileURL } = require('node:url');

(async () => {
  const browser = await chromium.launch({ channel: 'chrome', headless: true });
  try {
    const page = await browser.newPage({ viewport: { width: 1440, height: 1000 } });
    const errores = [];
    page.on('pageerror', e => errores.push(e.message));
    const externos = [];
    page.on('request', r => { if (/^https?:/.test(r.url())) externos.push(r.url()); });
    const url = pathToFileURL(path.resolve(__dirname, '../index.html')).href;
    const salida = path.resolve(__dirname, '../../docs/evidencias/hu02');
    fs.mkdirSync(salida, { recursive: true });
    await page.goto(url);
    assert.equal(await page.locator('.property').count(), 3);
    assert.ok(await page.locator('.pill').isVisible());
    assert.ok(await page.locator('img').evaluateAll(imgs => imgs.every(i => i.complete && i.naturalWidth > 0 && i.alt)));
    await page.screenshot({ path: path.join(salida, 'inicio-escritorio.png'), fullPage: true });
    await page.selectOption('#operacion', 'venta');
    assert.equal(await page.locator('.property').count(), 2);
    await page.selectOption('#operacion', 'arriendo');
    assert.equal(await page.locator('.property').count(), 1);
    await page.selectOption('#municipio', 'Envigado');
    assert.equal(await page.locator('.property').count(), 0);
    assert.ok(await page.locator('#sin-resultados').isVisible());
    await page.locator('#propiedades').scrollIntoViewIfNeeded();
    await page.screenshot({ path: path.join(salida, 'filtros-sin-resultados.png') });
    await page.click('#restablecer');
    assert.equal(await page.locator('.property').count(), 3);
    assert.equal(await page.locator('#municipio').inputValue(), '');
    assert.equal(await page.locator('#operacion').inputValue(), '');
    assert.equal(await page.locator('#sin-resultados').isVisible(), false);
    await page.selectOption('#municipio', 'Envigado');
    assert.equal(await page.locator('.property').count(), 1);
    await page.click('#restablecer');
    for (const width of [320, 390, 768, 1024, 1440]) {
      await page.setViewportSize({ width, height: 900 });
      assert.ok(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth), `Desbordamiento a ${width}px`);
    }
    await page.setViewportSize({ width: 390, height: 844 });
    await page.evaluate(() => scrollTo(0,0));
    await page.screenshot({ path: path.join(salida, 'inicio-movil.png'), fullPage: true });
    await page.reload();
    await page.keyboard.press('Tab');
    assert.equal(await page.evaluate(() => document.activeElement.textContent), 'Saltar al contenido');
    await page.keyboard.press('Tab');
    assert.ok(await page.evaluate(() => document.activeElement.classList.contains('brand')));
    await page.locator('#operacion').focus();
    assert.ok(await page.locator('#operacion').evaluate(e => getComputedStyle(e).outlineStyle !== 'none'));
    await page.keyboard.press('ArrowDown');
    await page.keyboard.press('Enter');
    assert.equal(await page.locator('.property').count(), 2);
    await page.getByRole('link', { name: 'Registrarme', exact: false }).first().click();
    assert.ok(page.url().endsWith('/registro.html'));
    assert.ok(await page.locator('#formulario-registro').isVisible());
    await page.getByRole('button', { name: 'Validar registro' }).click();
    assert.equal(await page.evaluate(() => document.activeElement.id), 'nombre');
    for (const [id, valor] of Object.entries({nombre:'Ana',apellido:'Demo',telefono:'3001234567',correo:'demo@example.com',contrasena:'Frase ficticia de prueba'})) await page.locator('#'+id).fill(valor);
    await page.selectOption('#rol','COMPRADOR');
    await page.getByRole('button', { name: 'Validar registro' }).click();
    assert.match(await page.locator('#resultado').textContent(), /No se ha creado una cuenta/);
    assert.ok(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth));
    await page.screenshot({ path: path.join(salida, 'registro-demo.png'), fullPage: true });
    await page.getByRole('link', { name: 'Volver al inicio', exact: false }).click();
    assert.ok(page.url().endsWith('/index.html'));
    assert.deepEqual(errores, []);
    assert.deepEqual(externos, []);
    console.log('OK: catálogo, filtros combinados, vacío, restablecer, imágenes, 5 anchos, teclado, navegación y validación demo. Sin errores JS ni solicitudes externas.');
  } finally { await browser.close(); }
})().catch(e => { console.error(e); process.exitCode = 1; });
