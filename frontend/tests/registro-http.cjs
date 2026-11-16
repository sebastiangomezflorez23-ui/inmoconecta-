const { chromium } = require('playwright');
const { spawn } = require('node:child_process');
const { once } = require('node:events');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

(async () => {
  const java = process.env.JAVA_EXE || 'java';
  const servidor = spawn(java, ['--add-modules', 'jdk.httpserver', '-cp', 'backend/out'+path.delimiter+'backend/lib/*', 'com.inmoconecta.ServidorPruebaInterfaz']);
  let browser;
  try {
    const puerto = await Promise.race([
      once(servidor.stdout, 'data').then(([data]) => Number(String(data).trim())),
      once(servidor, 'error').then(([error]) => { throw error; }),
      once(servidor, 'exit').then(() => { throw new Error('No inició el fixture Java'); })
    ]);
    assert.ok(puerto > 0);
    const base = `http://127.0.0.1:${puerto}`;
    browser = await chromium.launch({ channel:'chrome', headless:true });
    const page = await browser.newPage();
    const errores = [];
    page.on('pageerror', e => errores.push(e.message));
    await page.goto(base);
    assert.equal(await page.locator('.property').count(),3);
    for (const ruta of ['/.env','/backend/src/com/inmoconecta/Servidor.java','/../README.md','/%2e%2e/README.md','/tests/inicio.cjs']) {
      assert.equal((await page.request.get(base+ruta)).status(),404,ruta);
    }
    assert.equal((await page.request.post(base+'/registro.html')).status(),405);
    assert.equal((await page.request.head(base+'/registro.html')).status(),200);
    await page.getByRole('link',{name:'Registrarme',exact:false}).first().click();
    const llenar = async (correo,rol='COMPRADOR') => {
      for(const [id,value] of Object.entries({nombre:'Ana',apellido:'Prueba',telefono:'3001234567',correo,contrasena:'Frase ficticia de prueba'})) await page.locator('#'+id).fill(value);
      await page.selectOption('#rol',rol);
    };
    const enviar = async () => {
      const respuesta = page.waitForResponse(r => r.url().endsWith('/api/usuarios'));
      await page.getByRole('button',{name:'Crear cuenta',exact:true}).click();
      const status = (await respuesta).status();
      await page.getByRole('button',{name:'Crear cuenta',exact:true}).waitFor();
      return status;
    };
    await llenar('comprador@example.com');
    assert.equal(await enviar(),201);
    assert.match(await page.locator('#resultado').textContent(),/creó correctamente/);
    assert.equal(await page.locator('#contrasena').inputValue(),'');
    await llenar('comprador@example.com');
    assert.equal(await enviar(),409);
    assert.equal(await page.evaluate(()=>document.activeElement.id),'correo');
    await llenar('agente@example.com','AGENTE');
    assert.equal(await enviar(),201);
    await llenar('fallo@example.com');
    assert.equal(await enviar(),500);
    assert.match(await page.locator('#resultado').textContent(),/No pudimos completar/);
    await llenar('invalido@example.com');
    await page.route('**/api/usuarios',route=>route.continue({postData:JSON.stringify({nombre:'Ana',apellido:'Prueba',telefono:'3001234567',correo:'incorrecto',contrasena:'Frase ficticia de prueba',rol:'COMPRADOR'})}));
    assert.equal(await enviar(),400);
    assert.equal(await page.locator('#correo').getAttribute('aria-invalid'),'true');
    await page.unroute('**/api/usuarios');
    await llenar('red@example.com');
    await page.route('**/api/usuarios',route=>route.abort());
    await page.getByRole('button',{name:'Crear cuenta',exact:true}).click();
    await page.waitForFunction(()=>document.querySelector('#resultado').textContent.includes('No pudimos confirmar'));
    assert.equal(await page.getByRole('button',{name:'Crear cuenta',exact:true}).isEnabled(),true);
    await page.unroute('**/api/usuarios');
    let peticiones=0;
    await llenar('unico@example.com');
    await page.route('**/api/usuarios',async route=>{peticiones++;await new Promise(r=>setTimeout(r,300));await route.continue();});
    await page.getByRole('button',{name:'Crear cuenta',exact:true}).click();
    await page.locator('form').evaluate(f=>f.dispatchEvent(new Event('submit',{cancelable:true})));
    await page.waitForFunction(()=>document.querySelector('#resultado').textContent.includes('creó correctamente'));
    assert.equal(peticiones,1);
    assert.deepEqual(errores,[]);
    fs.mkdirSync('docs/evidencias/t10',{recursive:true});
    await page.screenshot({path:'docs/evidencias/t10/registro-http-prueba.png',fullPage:true});
    console.log('OK: HTTP Java + navegador: 201 ambos roles, 409, 400, 500, fallo de red, envío único y archivos privados inaccesibles. Repositorio en memoria; MySQL no probado en esta suite.');
  } finally { if(browser) await browser.close(); servidor.kill(); }
})().catch(e=>{console.error(e);process.exitCode=1;});
