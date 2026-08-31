-- =========== INSERCIÓN DE DATOS SEMILLA (DML) ===========

-- 1. Insertar roles
INSERT INTO roles (nombre_rol) VALUES 
('Superusuario'), 
('Supervisor de Seguridad'), 
('Guarda de Seguridad'), 
('Funcionario de Empresa');

-- 2. Insertar permisos
INSERT INTO permisos (nombre_permiso, descripcion) VALUES 
('crear_usuario', 'Permite crear nuevos usuarios en el sistema'),
('registrar_visita', 'Permite registrar la entrada y salida de personas'),
('generar_reporte', 'Permite generar reportes y estadísticas'),
('bloquear_persona', 'Permite cambiar el estado de acceso de una persona a prohibido'),
('aprobar_visita', 'Permite a un funcionario aprobar una visita no anunciada'),
('registrar_incidente', 'Permite registrar incidentes de seguridad');

-- 3. Asociar permisos a roles (rol_permisos)
-- Superusuario (Todos los permisos)
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6);
-- Supervisor de Seguridad (Reportes, incidentes, bloqueos)
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES (2, 3), (2, 4), (2, 6);
-- Guarda de Seguridad (Registrar visitas, incidentes)
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES (3, 2), (3, 6);
-- Funcionario de Empresa (Aprobar visitas)
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES (4, 5);

-- 4. Insertar Estados de Acceso de Persona
INSERT INTO persona_estados_acceso (nombre_estado) VALUES 
('Activo'), 
('Con Prohibicion de Ingreso');

-- 5. Insertar Estados de Visita
INSERT INTO visita_estados (nombre_estado) VALUES 
('Dentro'), 
('Fuera'), 
('Pendiente de Aprobacion'), 
('Aprobado'), 
('Rechazado'), 
('Cerrada por Sistema (Salida Olvidada)');

-- 6. Insertar Empresas de prueba
INSERT INTO empresas (nombre, contacto_principal) VALUES 
('Acme Corp', 'Juan Perez'),
('Globex Corporation', 'Hank Scorpio'),
('Stark Industries', 'Tony Stark');

-- 7. Insertar Personas de prueba
-- Trabajadores
INSERT INTO personas (nombre, documento_identidad, empresa_id, tipo_persona, estado_acceso_id) VALUES 
('Carlos Trabajador', '12345678', 1, 'Trabajador', 1),
('Ana Empleada', '87654321', 2, 'Trabajador', 1);
-- Invitados
INSERT INTO personas (nombre, documento_identidad, empresa_id, tipo_persona, estado_acceso_id) VALUES 
('Luis Invitado', '11223344', 1, 'Invitado', 1),
('Maria Visitante', '44332211', 3, 'Invitado', 1);

-- 8. Insertar Usuarios semilla (Las contraseñas aquí están en texto plano por simplicidad del demo, en real usar Hash)
INSERT INTO usuarios (nombre, email, password, rol_id) VALUES 
('Admin Principal', 'admin@sica.com', 'admin123', 1),
('Super Visor', 'supervisor@sica.com', 'super123', 2),
('Guarda Nocturno', 'guarda@sica.com', 'guarda123', 3),
('Funcionario Acme', 'funcionario@acme.com', 'func123', 4);
