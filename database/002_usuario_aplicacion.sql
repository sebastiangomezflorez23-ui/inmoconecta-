-- Ejecutar una sola vez como administrador en el MySQL LOCAL de Sebastián.
-- NO contiene contraseñas prefijadas. MySQL genera una y la muestra como resultado.
-- Guardar ese resultado de forma privada; no incluirlo en capturas ni commits.
-- Si el usuario ya existe, detenerse y revisar; no borrar ni reemplazar cuentas.
CREATE USER 'inmoconecta_app'@'localhost' IDENTIFIED BY RANDOM PASSWORD;
GRANT SELECT, INSERT ON inmoconecta.usuarios TO 'inmoconecta_app'@'localhost';
SHOW GRANTS FOR 'inmoconecta_app'@'localhost';
