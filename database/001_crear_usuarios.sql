-- Ejecutar en una base de desarrollo nueva de MySQL.
-- No elimina ni reemplaza tablas existentes.
CREATE DATABASE IF NOT EXISTS inmoconecta
    CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

USE inmoconecta;

CREATE TABLE usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(80) NOT NULL,
    apellido VARCHAR(80) NOT NULL,
    telefono VARCHAR(16) NOT NULL,
    correo VARCHAR(254) NOT NULL,
    contrasena_hash VARCHAR(512) NOT NULL,
    rol ENUM('COMPRADOR', 'AGENTE') NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_usuarios_correo UNIQUE (correo)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

-- Java debe guardar correo recortado y en minúsculas antes del INSERT.
-- telefono contiene solo dígitos y el prefijo + opcional.
-- contrasena_hash contendrá el formato codificado del algoritmo elegido en T09,
-- incluyendo salt y parámetros; nunca la contraseña original.
