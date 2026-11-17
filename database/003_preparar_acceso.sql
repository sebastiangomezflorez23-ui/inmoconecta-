-- PREPARACIÓN HU-03: revisar antes de aplicar. Aún no ejecutada por el asistente.
-- Ejecutar una vez, como administrador, sobre el esquema creado por 001.
-- Hacer respaldo de desarrollo antes de migrar. El DDL de MySQL hace commit
-- implícito: no presentar un ROLLBACK como recuperación de este archivo.
-- Si una sentencia falla, detenerse e inspeccionar el esquema antes de repetir.
-- No elimina usuarios, no cambia sus hashes y no crea cuentas ADMIN.
USE inmoconecta;

ALTER TABLE usuarios
    MODIFY COLUMN rol ENUM('COMPRADOR', 'AGENTE', 'ADMIN') NOT NULL,
    ADD COLUMN intentos_fallidos TINYINT UNSIGNED NOT NULL DEFAULT 0,
    ADD COLUMN bloqueado_hasta DATETIME(6) NULL;

CREATE TABLE sesiones (
    token_hash CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    usuario_id BIGINT NOT NULL,
    creada_en DATETIME(6) NOT NULL,
    vence_en DATETIME(6) NOT NULL,
    PRIMARY KEY (token_hash),
    INDEX idx_sesiones_usuario (usuario_id),
    INDEX idx_sesiones_vencimiento (vence_en),
    CONSTRAINT fk_sesiones_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT chk_sesiones_vencimiento CHECK (vence_en > creada_en)
) ENGINE=InnoDB;

-- El servidor podrá actualizar bloqueo, pero no modificar el rol de una cuenta.
GRANT UPDATE (intentos_fallidos, bloqueado_hasta)
    ON inmoconecta.usuarios TO 'inmoconecta_app'@'localhost';
GRANT SELECT, INSERT, DELETE ON inmoconecta.sesiones
    TO 'inmoconecta_app'@'localhost';

-- Verificaciones de estructura, sin mostrar contraseñas ni tokens.
SHOW COLUMNS FROM usuarios;
SHOW CREATE TABLE sesiones;
SHOW GRANTS FOR 'inmoconecta_app'@'localhost';
