// Requiere el servidor real iniciado. Crea DOS usuarios ficticios y los conserva
// para comprobar persistencia; no elimina datos ni necesita conocer la clave MySQL.
const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
(async()=>{
  const browser=await chromium.launch({channel:'chrome',headless:true});
  try {
    const page=await browser.newPage({viewport:{width:1280,height:1000}});
    if (process.argv.includes('--persistencia')) {
      const archivo='docs/evidencias/t10/mysql-resultados.json';
      const evidencia=JSON.parse(fs.readFileSync(archivo,'utf8'));
      for(const usuario of evidencia.resultados) {
        const respuesta=await page.request.post('http://127.0.0.1:8080/api/usuarios',{
          data:{nombre:'Prueba',apellido:'Integracion',telefono:'3001234567',correo:usuario.correo,contrasena:'Frase ficticia prueba T10',rol:usuario.rol}
        });
        assert.equal(respuesta.status(),409,usuario.rol+' debe seguir registrado');
      }
      evidencia.persistencia={fecha:new Date().toISOString(),resultado:'Ambos correos conservados: HTTP 409 tras reinicio indicado por el usuario'};
      fs.writeFileSync(archivo,JSON.stringify(evidencia,null,2)+'\n');
      console.log('OK: ambos correos siguen registrados tras el reinicio.');
      return;
    }
    const sello=Date.now();
    const resultados=[];
    for(const rol of ['COMPRADOR','AGENTE']) {
      await page.goto('http://127.0.0.1:8080/');
      await page.getByRole('link',{name:'Registrarme',exact:false}).first().click();
      const correo=`t10-${rol.toLowerCase()}-${sello}@example.com`;
      const llenar=async()=>{
        for(const [id,valor] of Object.entries({nombre:'Prueba',apellido:'Integracion',telefono:'3001234567',correo,contrasena:'Frase ficticia prueba T10'})) await page.locator('#'+id).fill(valor);
        await page.selectOption('#rol',rol);
      };
      await llenar();
      const promesa=page.waitForResponse(r=>r.url().endsWith('/api/usuarios'));
      await page.getByRole('button',{name:'Crear cuenta',exact:true}).click();
      const respuesta=await promesa;
      assert.equal(respuesta.status(),201,`Registro ${rol}`);
      const json=await respuesta.json();
      assert.equal(json.codigo,'USUARIO_CREADO');
      await page.waitForFunction(()=>document.querySelector('#resultado').textContent.includes('creó correctamente'));
      assert.equal(await page.locator('#contrasena').inputValue(),'');
      fs.mkdirSync('docs/evidencias/t10',{recursive:true});
      await page.screenshot({path:`docs/evidencias/t10/mysql-${rol.toLowerCase()}.png`,fullPage:true});
      // Nueva carga del formulario: el estado no proviene de la página anterior.
      await page.reload();
      await llenar();
      const repetida=page.waitForResponse(r=>r.url().endsWith('/api/usuarios'));
      await page.getByRole('button',{name:'Crear cuenta',exact:true}).click();
      assert.equal((await repetida).status(),409);
      await page.waitForFunction(()=>document.querySelector('#correo').getAttribute('aria-invalid')==='true');
      resultados.push({rol,correo,id:json.usuario.id,creacion:201,duplicado:409});
    }
    fs.writeFileSync('docs/evidencias/t10/mysql-resultados.json',JSON.stringify({fecha:new Date().toISOString(),resultados},null,2)+'\n');
    console.log(JSON.stringify(resultados));
  } finally {await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1;});
