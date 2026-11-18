const { chromium } = require('playwright');
const { spawn } = require('node:child_process');
const { once } = require('node:events');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
(async () => {
  const servidor = spawn(process.env.JAVA_EXE || 'java', ['--add-modules','jdk.httpserver','-cp','backend/out'+path.delimiter+'backend/lib/*','com.inmoconecta.ServidorPruebaAcceso']);
  let browser;
  try {
    const puerto = await Promise.race([once(servidor.stdout,'data').then(([d])=>Number(String(d).trim())), once(servidor,'error').then(([e])=>{throw e;}), once(servidor,'exit').then(()=>{throw Error('Fixture no inició');})]);
    assert.ok(puerto > 0);
    const base = `http://127.0.0.1:${puerto}`;
    const clave = 'Frase ficticia de acceso';
    const post = (body, headers = {}, ruta = '/api/login') => fetch(base+ruta, {method:'POST',headers:{Origin:base,'Content-Type':'application/json',...headers},body:typeof body === 'string' ? body : JSON.stringify(body)});
    const entrada = (correo='comprador@example.com',contrasena=clave)=>({correo,contrasena});
    let r = await post(entrada(),{Origin:'https://ajeno.example'}); assert.equal(r.status,403);
    r = await fetch(base+'/api/login',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(entrada())}); assert.equal(r.status,403);
    assert.equal((await post(entrada(),{Origin:'null'})).status,403);
    assert.equal((await fetch(base+'/api/login')).status,405);
    assert.equal((await post(entrada(),{},'/api/login/extra')).status,404);
    assert.equal((await post(entrada(),{'Content-Type':'text/plain'})).status,415);
    assert.equal((await post('x'.repeat(4097))).status,413);
    for (const cuerpo of ['null','[]','{}','{"correo":"a@b.co","correo":"c@d.co","contrasena":"a"}', JSON.stringify({...entrada(),rol:'ADMIN'}), JSON.stringify({...entrada(),contrasena:42}), JSON.stringify({...entrada(),contrasena:'x'.repeat(129)}), JSON.stringify(entrada())+'{}','{"correo":"a@b.co","contrasena":"a",}']) {
      assert.equal((await post(cuerpo)).status,400,cuerpo);
    }
    for (const rol of ['COMPRADOR','AGENTE','ADMIN']) {
      r = await post(entrada(' '+rol.toLowerCase()+'@EXAMPLE.COM ')); assert.equal(r.status,200);
      const body = await r.json(); assert.equal(body.usuario.rol,rol);
      assert.deepEqual(Object.keys(body.usuario).sort(),['id','nombre','rol']);
      const cookie = r.headers.get('set-cookie');
      assert.match(cookie,/HttpOnly/); assert.match(cookie,/SameSite=Strict/); assert.match(cookie,/Max-Age=1800/);
      assert.equal(r.headers.get('cache-control'),'no-store');
      const valor = cookie.split(';')[0];
      assert.equal((await fetch(base+'/api/sesion',{headers:{Cookie:valor}})).status,200);
      assert.equal((await fetch(base+'/api/sesion',{headers:{Cookie:valor+'; '+valor}})).status,401);
      assert.equal((await fetch(base+'/api/sesion',{headers:{Cookie:'inmoconecta_sesion='+'a'.repeat(43)}})).status,401);
      assert.equal((await fetch(base+'/api/sesion',{headers:{Cookie:valor,Origin:'https://ajeno.example'}})).status,403);
    }
    assert.equal((await fetch(base+'/api/sesion')).status,401);
    r = await post(entrada('fallo@example.com')); assert.equal(r.status,500); assert.equal(r.headers.get('set-cookie'),null); assert.ok(!(await r.text()).includes('secreto'));
    const desconocido = await (await post(entrada('desconocido@example.com'))).text();
    for(let i=0;i<3;i++) { r=await post(entrada('admin@example.com','incorrecta')); assert.equal(r.status,401); assert.equal(await r.text(),desconocido); }
    r=await post(entrada('admin@example.com')); assert.equal(r.status,401); assert.equal(r.headers.get('set-cookie'),null);
    // Navegador real + handlers Java; repositorio exclusivamente simulado.
    browser = await chromium.launch({channel:'chrome',headless:true});
    const page = await browser.newPage(); const errores=[]; page.on('pageerror',e=>errores.push(e.message));
    await page.goto(base+'/cuenta.html');
    await page.waitForFunction(()=>document.querySelector('#resultado').textContent.includes('Inicia sesión'));
    assert.ok(await page.locator('#datos-cuenta').isHidden());
    await page.goto(base); await page.getByRole('link',{name:'Iniciar sesión',exact:true}).click();
    await page.locator('#correo').fill('comprador@example.com'); await page.locator('#contrasena').fill(clave);
    await page.getByRole('button',{name:'Mostrar',exact:true}).click(); assert.equal(await page.locator('#contrasena').getAttribute('type'),'text');
    await page.getByRole('button',{name:'Ocultar',exact:true}).click();
    await page.getByRole('button',{name:'Entrar a mi cuenta'}).click();
    await page.waitForURL('**/cuenta.html'); await page.locator('#datos-cuenta').waitFor();
    assert.match(await page.locator('#rol-cuenta').textContent(),/Comprador/);
    assert.equal(await page.evaluate(()=>localStorage.length),0);
    assert.ok(!(await page.evaluate(()=>document.cookie)).includes('inmoconecta_sesion'));
    fs.mkdirSync('docs/evidencias/hu03',{recursive:true});
    await page.screenshot({path:'docs/evidencias/hu03/cuenta.png',fullPage:true});
    await page.context().clearCookies(); await page.reload();
    await page.waitForFunction(()=>document.querySelector('#resultado').textContent.includes('Inicia sesión'));
    assert.ok(await page.locator('#datos-cuenta').isHidden());
    await page.goto(base+'/login.html');
    for(const width of [360,390,768,1440]) {
      await page.setViewportSize({width,height:900});
      assert.ok(await page.evaluate(()=>document.documentElement.scrollWidth <= innerWidth),`overflow ${width}`);
      await page.screenshot({path:`docs/evidencias/hu03/login-${width}.png`,fullPage:true});
    }
    await page.locator('#correo').fill('agente@example.com'); await page.locator('#contrasena').fill(clave);
    await page.route('**/api/login',route=>route.abort());
    await page.getByRole('button',{name:'Entrar a mi cuenta'}).click();
    await page.waitForFunction(()=>document.querySelector('#resultado').textContent.includes('No se pudo confirmar'));
    assert.ok(await page.getByRole('button',{name:'Entrar a mi cuenta'}).isEnabled());
    await page.unroute('**/api/login');
    let peticiones=0;
    await page.route('**/api/login',async route=>{peticiones++;await new Promise(r=>setTimeout(r,200));await route.continue();});
    await page.getByRole('button',{name:'Entrar a mi cuenta'}).click();
    await page.locator('form').evaluate(f=>f.dispatchEvent(new Event('submit',{cancelable:true})));
    await page.waitForURL('**/cuenta.html'); await page.locator('#datos-cuenta').waitFor();
    assert.equal(peticiones,1); assert.match(await page.locator('#rol-cuenta').textContent(),/Agente/);
    assert.deepEqual(errores,[]);
    console.log('OK: HU03 HTTP y navegador: 3 roles, cookies, JSON estricto, bloqueo, origen, fallo SQL simulado, sesión ausente/alterada, red, envío único y 4 tamaños. MySQL real y concurrencia JDBC pendientes.');
  } finally {if(browser) await browser.close(); servidor.kill();}
})().catch(e=>{console.error(e);process.exitCode=1;});
