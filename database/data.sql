USE sica_db;

-- 1. ROLES
INSERT INTO roles (id, nombre_rol) VALUES 
(1, 'Superusuario'),
(2, 'Supervisor de Seguridad'),
(3, 'Guarda de Seguridad'),
(4, 'Funcionario de Empresa');

-- 2. PERMISOS GRANULARES (Alineados al RBAC en Java)
INSERT INTO permisos (id, nombre_permiso, descripcion) VALUES 
(1, 'crear_usuario', 'Crear y administrar cuentas de usuario del sistema SICA'),
(2, 'modificar_usuario', 'Editar permisos, roles y estados de usuarios'),
(3, 'bloquear_persona', 'Cambiar estado de una persona a Con Prohibición de Ingreso'),
(4, 'habilitar_persona', 'Reactivar el permiso de ingreso a una persona'),
(5, 'registrar_ingreso', 'Ejecutar check-in y validar paso en torniquetes'),
(6, 'registrar_salida', 'Ejecutar check-out en puntos de control de salida'),
(7, 'solicitar_visita', 'Pre-registrar solicitudes de visita a instalaciones'),
(8, 'aprobar_visita', 'Autorizar solicitudes de visita e ingresos no anunciados u olvido carnet'),
(9, 'rechazar_visita', 'Denegar ingreso a solicitudes de visita'),
(10, 'registrar_incidente', 'Reportar eventos e infracciones de seguridad física'),
(11, 'consultar_auditoria', 'Acceder a la bitácora inmutable de auditoría forense'),
(12, 'generar_reportes', 'Generar métricas de aforo, analítica y estadísticas con Streams');

-- 3. ASIGNACIÓN ROL-PERMISOS
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES 
-- Superusuario (1 al 12)
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12),
-- Supervisor (gestión operativa, bloqueos, auditoría, incidentes, reportes)
(2, 3), (2, 4), (2, 5), (2, 6), (2, 10), (2, 11), (2, 12),
-- Guarda (check-in, check-out, solicitar pase temporal, reportar incidente)
(3, 5), (3, 6), (3, 7), (3, 10),
-- Funcionario (pre-registro, aprobar y rechazar visitas u olvidos)
(4, 7), (4, 8), (4, 9);

-- 4. USUARIOS (Password hash BCrypt representativo para Java: 'Password123*')
INSERT INTO usuarios (id, nombre, email, password, rol_id, esta_activo) VALUES 
(1, 'Administrador Central SICA', 'admin@zonaacme.com', '$2a$12$e8Y5.5O/6/6B32Wp2qVHQeg0R82p0x.02j9K8W5O31kR9Y4y7X9gK', 1, TRUE),
(2, 'Capitán Fernando Rojas (Supervisor)', 'supervisor.seguridad@zonaacme.com', '$2a$12$e8Y5.5O/6/6B32Wp2qVHQeg0R82p0x.02j9K8W5O31kR9Y4y7X9gK', 2, TRUE),
(3, 'Oficial Carlos Méndez (Guarda Portería)', 'guarda.porteria1@zonaacme.com', '$2a$12$e8Y5.5O/6/6B32Wp2qVHQeg0R82p0x.02j9K8W5O31kR9Y4y7X9gK', 3, TRUE),
(4, 'Oficial Andrea Silva (Guarda Bahía)', 'guarda.bahia@zonaacme.com', '$2a$12$e8Y5.5O/6/6B32Wp2qVHQeg0R82p0x.02j9K8W5O31kR9Y4y7X9gK', 3, TRUE),
(5, 'Dra. Valentina Duque (Directora BioGen)', 'v.duque@biogenacme.com', '$2a$12$e8Y5.5O/6/6B32Wp2qVHQeg0R82p0x.02j9K8W5O31kR9Y4y7X9gK', 4, TRUE),
(6, 'Ing. Mauricio Restrepo (Gerente Quantum)', 'm.restrepo@quantumdynamics.com', '$2a$12$e8Y5.5O/6/6B32Wp2qVHQeg0R82p0x.02j9K8W5O31kR9Y4y7X9gK', 4, TRUE);

-- 5. ESTADOS (Incluye el estado del Flujo 3)
INSERT INTO persona_estados_acceso (id, nombre_estado) VALUES 
(1, 'Activo'),
(2, 'Con Prohibicion de Ingreso');

INSERT INTO visita_estados (id, nombre_estado) VALUES 
(1, 'Pendiente de Aprobacion'),
(2, 'Aprobado'),
(3, 'Dentro'),
(4, 'Fuera'),
(5, 'Rechazado'),
(6, 'Expirado'),
(7, 'Cerrada por Sistema (Salida Olvidada)'),
(8, 'Pendiente de Aprobacion por Olvido');

-- 6. INFRAESTRUCTURA
INSERT INTO zonas (id, codigo, nombre, descripcion, aforo_maximo, hora_apertura, hora_cierre, requiere_autorizacion_especial, esta_activa) VALUES 
(1, 'ZONA_LOBBY', 'Lobby Principal y Recepción', 'Área de recepción general y torniquetes peatonales', 200, '05:00:00', '23:00:00', FALSE, TRUE),
(2, 'ZONA_TECH_HUB', 'Torre de Innovación & Tech Hub', 'Pisos corporativos de oficinas para empresas de tecnología', 350, '06:00:00', '21:00:00', FALSE, TRUE),
(3, 'ZONA_BIOLABS', 'Laboratorios BioGen & Nanotecnología', 'Área biocontenida de investigación farmacéutica', 80, '07:00:00', '19:00:00', TRUE, TRUE),
(4, 'ZONA_DATACENTER', 'Centro de Cómputo Principal (Tier IV)', 'Sala de servidores críticos y telecomunicaciones', 15, '00:00:00', '23:59:59', TRUE, TRUE),
(5, 'ZONA_PARKING', 'Estacionamiento Subterráneo S1/S2', 'Bahías vehiculares para funcionarios y visitantes', 150, '05:30:00', '22:30:00', FALSE, TRUE);

INSERT INTO puntos_control (id, codigo, nombre, zona_id, tipo_punto, esta_activo) VALUES 
(1, 'PC_TORN_01', 'Torniquete Peatonal 1 (Entrada Norte)', 1, 'TORNIQUETE_PEATONAL', TRUE),
(2, 'PC_TORN_02', 'Torniquete Peatonal 2 (Entrada Sur)', 1, 'TORNIQUETE_PEATONAL', TRUE),
(3, 'PC_ELEV_TECH', 'Control Biométrico Ascensores Tech Hub', 2, 'PUERTA_BIOMETRICA', TRUE),
(4, 'PC_PUERTA_BIOLAB', 'Puerta Blindada Acceso Laboratorios', 3, 'PUERTA_BIOMETRICA', TRUE),
(5, 'PC_DC_RESTRINGIDO', 'Esclusa de Seguridad Datacenter', 4, 'PUERTA_BIOMETRICA', TRUE),
(6, 'PC_TALANQUERA_VEH', 'Talanquera Vehicular Acceso S1', 5, 'TALANQUERA_VEHICULAR', TRUE);

-- 7. EMPRESAS
INSERT INTO empresas (id, nit, nombre, contacto_principal, email_contacto, telefono_contacto, piso_ubicacion, esta_activa) VALUES 
(1, '900.123.456-1', 'Acme CyberDefense Labs', 'Ing. Roberto Gómez', 'rgomez@acmedefense.com', '601-555-0101', 'Piso 5', TRUE),
(2, '900.789.012-3', 'BioGen Innovations S.A.', 'Dra. Valentina Duque', 'vduque@biogenacme.com', '601-555-0202', 'Piso 3', TRUE),
(3, '901.345.678-5', 'Quantum Dynamics & Robotics', 'Ing. Mauricio Restrepo', 'mrestrepo@quantumdynamics.com', '601-555-0303', 'Piso 4', TRUE),
(4, '901.901.234-7', 'Apex Cloud Systems Corp', 'Lic. Sofía Carvajal', 'scarvajal@apexcloud.io', '601-555-0404', 'Piso 2', TRUE);

-- 8. PERSONAS
INSERT INTO personas (id, documento_identidad, tipo_documento, nombre, email, telefono, empresa_id, tipo_persona, estado_acceso_id, url_foto) VALUES 
-- Trabajadores
(1, '10102020', 'CC', 'Roberto Gómez (Ciberseguridad)', 'rgomez@acmedefense.com', '3001234567', 1, 'Trabajador', 1, 'https://images.unsplash.com/photo-1534528741775-53994a69daeb'),
(2, '10203040', 'CC', 'Valentina Duque (Científica Senior)', 'vduque@biogenacme.com', '3109876543', 2, 'Trabajador', 1, 'https://images.unsplash.com/photo-1580489944761-15a19d654956'),
(3, '10304050', 'CC', 'Mauricio Restrepo (Arquitecto Cloud)', 'mrestrepo@quantumdynamics.com', '3156781234', 3, 'Trabajador', 1, 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d'),
(4, '10405060', 'CC', 'Sofía Carvajal (Líder DevOps)', 'scarvajal@apexcloud.io', '3204567890', 4, 'Trabajador', 1, 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2'),

-- Invitados y Contratistas
(5, '80809090', 'CC', 'Mario Alberto Visitante (Auditor ISO 27001)', 'mario.auditor@certivalid.com', '3118901234', 1, 'Invitado', 1, 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e'),
(6, '70708080', 'CC', 'Elena Torres (Redes Cisco)', 'elena.redes@telecomexpert.com', '3187654321', 4, 'Contratista', 1, 'https://images.unsplash.com/photo-1544005313-94ddf0286df2'),
(7, '60607070', 'CC', 'Julian Arango (Consultor Genética)', 'jarango@biotechconsulting.org', '3012349876', 2, 'Invitado', 1, 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d'),
(8, '99998888', 'CC', 'Persona Restringida (Sancionado)', 'restringido@correo.com', '3000000000', 1, 'Invitado', 2, 'https://images.unsplash.com/photo-1492562080023-ab3db95bfbce');

-- 9. VISITAS (Cubre exactamente los 4 flujos de la rúbrica)
INSERT INTO visitas (id, persona_id, anfitrion_id, motivo, fecha_prevista_inicio, fecha_prevista_fin, fecha_entrada, fecha_salida, estado_visita_id, vehiculo_placa, visita_aprobada_por, fecha_aprobacion) VALUES 
-- Flujo 1: Invitado Pre-registrado Aprobado
(1, 5, 1, 'Auditoría Anual ISO 27001', '2026-09-03 08:00:00', '2026-09-03 18:00:00', '2026-09-03 08:15:22', NULL, 3, 'ABC-123', 5, '2026-09-02 17:00:00'),

-- Flujo 2: Invitado No Anunciado (Pendiente de Aprobación en Tiempo Real)
(2, 7, 2, 'Reunión urgente de laboratorio', '2026-09-03 10:00:00', '2026-09-03 12:00:00', NULL, NULL, 1, 'XYZ-789', NULL, NULL),

-- Flujo 3: Trabajador con Carnet Olvidado (Pase Temporal)
(3, 4, 3, 'Olvido de carnet físico en recepción', '2026-09-03 08:30:00', '2026-09-03 18:00:00', NULL, NULL, 8, NULL, NULL, NULL),

-- Flujo 4: Regularización de Salida Olvidada (Cerrada automáticamente)
(4, 6, 4, 'Mantenimiento preventivo enlaces de fibra', '2026-09-02 14:00:00', '2026-09-02 18:00:00', '2026-09-02 14:10:00', NULL, 7, 'KLR-456', 6, '2026-09-02 13:50:00');

-- 10. REGISTROS ATÓMICOS DE PASO
INSERT INTO registros_acceso (id, persona_id, punto_control_id, visita_id, fecha_hora, tipo_acceso, resultado, observacion) VALUES 
(1, 1, 1, NULL, '2026-09-03 07:45:10', 'ENTRADA', 'PERMITIDO', 'Ingreso laboral de funcionario registrado'),
(2, 5, 1, 1, '2026-09-03 08:15:22', 'ENTRADA', 'PERMITIDO', 'Check-In de visita pre-registrada'),
(3, 8, 1, NULL, '2026-09-03 09:20:15', 'ENTRADA', 'DENEGADO_PERSONA_BLOQUEADA', 'Intento de acceso denegado: sanción activa');

-- 11. INCIDENTES
INSERT INTO incidentes (id, visita_id, persona_id, reportado_por_id, nivel_gravedad, fecha, descripcion, acciones_tomadas) VALUES 
(1, NULL, 8, 2, 'GRAVE', '2026-09-03 09:22:00', 'Intento de forzar torniquete norte por persona con prohibición activa', 'Custodia por personal de seguridad en lobby');

-- 12. BITÁCORA DE AUDITORÍA
INSERT INTO bitacora_auditoria (id, usuario_id, fecha_hora, accion_realizada, tabla_afectada, registro_id_afectado, direccion_ip, detalles) VALUES 
(1, 1, '2026-09-03 07:00:00', 'LOGIN_EXITOSO', 'usuarios', 1, '192.168.1.10', 'Inicio de sesión Superusuario'),
(2, 3, '2026-09-03 08:15:22', 'CHECKIN_VISITA', 'visitas', 1, '192.168.1.25', 'Check-in autorizado para Mario Alberto Visitante'),
(3, 3, '2026-09-03 09:20:15', 'ALERTA_ACCESO_DENEGADO', 'personas', 8, '192.168.1.25', 'Detección de persona bloqueada CC 99998888');