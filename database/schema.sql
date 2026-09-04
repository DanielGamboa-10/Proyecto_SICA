-- =====================================================================
-- PROYECTO: SICA (Sistema Integrado de Control de Acceso)
-- COMPLEJO EMPRESARIAL: "ZONA ACME"
-- SCRIPT: schema.sql (Definición de Esquema DDL - MySQL / MariaDB)
-- =====================================================================

DROP DATABASE IF EXISTS sica_db;
CREATE DATABASE sica_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sica_db;

-- =========== 1. TABLAS DE SEGURIDAD Y CONTROL DE ACCESO (RBAC) ===========

CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(50) UNIQUE NOT NULL
) ENGINE=InnoDB COMMENT='Roles de seguridad del sistema';

CREATE TABLE permisos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_permiso VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT
) ENGINE=InnoDB COMMENT='Acciones y permisos granulares del sistema';

CREATE TABLE rol_permisos (
    rol_id INT NOT NULL,
    permiso_id INT NOT NULL,
    PRIMARY KEY (rol_id, permiso_id),
    FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permiso_id) REFERENCES permisos(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Tabla intermedia de asignación de permisos a roles';

CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol_id INT,
    esta_activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='Cuentas de usuario autorizadas para operar SICA';


-- =========== 2. TABLAS DE ESTADOS Y LOOKUPS ===========

CREATE TABLE persona_estados_acceso (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_estado VARCHAR(50) UNIQUE NOT NULL
) ENGINE=InnoDB COMMENT='Estados de acceso físico de personas (Activo, Con Prohibición)';

CREATE TABLE visita_estados (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_estado VARCHAR(50) UNIQUE NOT NULL
) ENGINE=InnoDB COMMENT='Estados del ciclo de vida de una visita';


-- =========== 3. INFRAESTRUCTURA FÍSICA Y ZONAS DEL COMPLEJO ===========

CREATE TABLE zonas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    aforo_maximo INT DEFAULT 50,
    hora_apertura TIME DEFAULT '06:00:00',
    hora_cierre TIME DEFAULT '22:00:00',
    requiere_autorizacion_especial BOOLEAN DEFAULT FALSE,
    esta_activa BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB COMMENT='Zonas y áreas físicas del Complejo Zona Acme';

CREATE TABLE puntos_control (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    zona_id INT NOT NULL,
    tipo_punto ENUM('TORNIQUETE_PEATONAL', 'TALANQUERA_VEHICULAR', 'PUERTA_BIOMETRICA') DEFAULT 'TORNIQUETE_PEATONAL',
    esta_activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (zona_id) REFERENCES zonas(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Dispositivos físicos de control de paso';


-- =========== 4. ENTIDADES DEL NEGOCIO (EMPRESAS Y PERSONAS) ===========

CREATE TABLE empresas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nit VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    contacto_principal VARCHAR(100) NOT NULL,
    email_contacto VARCHAR(100) NOT NULL,
    telefono_contacto VARCHAR(20),
    piso_ubicacion VARCHAR(10),
    esta_activa BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB COMMENT='Empresas inquilinas del complejo Zona Acme';

CREATE TABLE personas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    documento_identidad VARCHAR(20) UNIQUE NOT NULL,
    tipo_documento ENUM('CC', 'CE', 'PASAPORTE', 'TI') DEFAULT 'CC',
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    telefono VARCHAR(20),
    empresa_id INT,
    tipo_persona ENUM('Trabajador', 'Invitado', 'Contratista', 'Proveedor') NOT NULL,
    estado_acceso_id INT NOT NULL,
    url_foto VARCHAR(255),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE SET NULL,
    FOREIGN KEY (estado_acceso_id) REFERENCES persona_estados_acceso(id) ON DELETE RESTRICT
) ENGINE=InnoDB COMMENT='Directorio unificado de personas del complejo';


-- =========== 5. GESTIÓN TRANSACCIONAL DE VISITAS Y ACCESOS ===========

CREATE TABLE visitas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    persona_id INT NOT NULL,
    anfitrion_id INT,
    motivo VARCHAR(255) NOT NULL,
    fecha_prevista_inicio DATETIME NOT NULL,
    fecha_prevista_fin DATETIME NOT NULL,
    fecha_entrada DATETIME,
    fecha_salida DATETIME,
    estado_visita_id INT NOT NULL,
    vehiculo_placa VARCHAR(15),
    visita_aprobada_por INT,
    fecha_aprobacion DATETIME,
    FOREIGN KEY (persona_id) REFERENCES personas(id) ON DELETE CASCADE,
    FOREIGN KEY (anfitrion_id) REFERENCES personas(id) ON DELETE SET NULL,
    FOREIGN KEY (estado_visita_id) REFERENCES visita_estados(id) ON DELETE RESTRICT,
    FOREIGN KEY (visita_aprobada_por) REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='Registro transaccional de solicitudes y estancias de visitas';

CREATE TABLE registros_acceso (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    persona_id INT NOT NULL,
    punto_control_id INT NOT NULL,
    visita_id INT,
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tipo_acceso ENUM('ENTRADA', 'SALIDA') NOT NULL,
    resultado ENUM('PERMITIDO', 'DENEGADO_PERSONA_BLOQUEADA', 'DENEGADO_SIN_VISITA', 'DENEGADO_FUERA_HORARIO', 'DENEGADO_PUNTO_INACTIVO') NOT NULL,
    observacion VARCHAR(255),
    FOREIGN KEY (persona_id) REFERENCES personas(id) ON DELETE CASCADE,
    FOREIGN KEY (punto_control_id) REFERENCES puntos_control(id) ON DELETE CASCADE,
    FOREIGN KEY (visita_id) REFERENCES visitas(id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='Paso físico atómico por torniquetes y barreras';


-- =========== 6. INCIDENTES Y SEGURIDAD ===========

CREATE TABLE incidentes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    visita_id INT,
    persona_id INT,
    reportado_por_id INT,
    nivel_gravedad ENUM('LEVE', 'MODERADO', 'GRAVE', 'CRITICO') DEFAULT 'MODERADO',
    fecha DATETIME NOT NULL,
    descripcion TEXT NOT NULL,
    acciones_tomadas TEXT,
    FOREIGN KEY (visita_id) REFERENCES visitas(id) ON DELETE SET NULL,
    FOREIGN KEY (persona_id) REFERENCES personas(id) ON DELETE CASCADE,
    FOREIGN KEY (reportado_por_id) REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='Reportes de seguridad y novedades operativas';


-- =========== 7. BITÁCORA DE AUDITORÍA INMUTABLE ===========

CREATE TABLE bitacora_auditoria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT,
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accion_realizada VARCHAR(255) NOT NULL,
    tabla_afectada VARCHAR(100),
    registro_id_afectado INT,
    direccion_ip VARCHAR(45) DEFAULT '127.0.0.1',
    detalles TEXT,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='Trazabilidad y registro forense inmutable de operaciones críticas';

-- Índices de alto rendimiento
CREATE INDEX idx_personas_doc ON personas(documento_identidad);
CREATE INDEX idx_visitas_persona ON visitas(persona_id);
CREATE INDEX idx_visitas_estado ON visitas(estado_visita_id);
CREATE INDEX idx_registros_fecha ON registros_acceso(fecha_hora);
CREATE INDEX idx_bitacora_fecha ON bitacora_auditoria(fecha_hora);
