-- =====================================================================
-- PROYECTO: SICA (Sistema Integrado de Control de Acceso)
-- COMPLEJO EMPRESARIAL: "ZONA ACME"
-- SCRIPT: data.sql (Datos Semilla Empresariales - DML)
-- =====================================================================

USE sica_db;

-- =========== 1. ROLES DE SEGURIDAD ===========
INSERT INTO roles (id, nombre_rol) VALUES 
(1, 'Superusuario'),
(2, 'Supervisor de Seguridad'),
(3, 'Guarda de Seguridad'),
(4, 'Funcionario de Empresa');

-- =========== 2. PERMISOS GRANULARES ===========
INSERT INTO permisos (id, nombre_permiso, descripcion) VALUES 
(1, 'crear_usuario', 'Crear y administrar cuentas de usuario del sistema SICA'),
(2, 'modificar_usuario', 'Editar permisos, roles y estados de usuarios'),
(3, 'bloquear_persona', 'Cambiar estado de una persona a Con Prohibición de Ingreso'),
(4, 'habilitar_persona', 'Reactivar el permiso de ingreso a una persona'),
(5, 'registrar_ingreso', 'Ejecutar check-in y validar paso en torniquetes'),
(6, 'registrar_salida', 'Ejecutar check-out en puntos de control de salida'),
(7, 'solicitar_visita', 'Pre-registrar solicitudes de visita a instalaciones'),
(8, 'aprobar_visita', 'Autorizar solicitudes de visita e ingresos no anunciados'),
(9, 'rechazar_visita', 'Denegar ingreso a solicitudes de visita'),
(10, 'registrar_incidente', 'Reportar eventos e infracciones de seguridad física'),
(11, 'consultar_auditoria', 'Acceder a la bitácora inmutable de auditoría forense'),
(12, 'generar_reportes', 'Generar métricas de aforo, analítica y estadísticas con Streams');

-- =========== 3. ASIGNACIÓN DE PERMISOS A ROLES (rol_permisos) ===========
-- Superusuario (Todos los permisos: 1 a 12)
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES 
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12);

-- Supervisor de Seguridad (Gestión operativa, auditoría, incidentes, bloqueos, reportes)
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES 
(2, 3), (2, 4), (2, 5), (2, 6), (2, 10), (2, 11), (2, 12);

-- Guarda de Seguridad (Control en torniquetes, check-in, check-out, registrar incidentes)
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES 
(3, 5), (3, 6), (3, 7), (3, 10);

-- Funcionario de Empresa (Pre-registro de invitados, aprobación y rechazo)
INSERT INTO rol_permisos (rol_id, permiso_id) VALUES 
(4, 7), (4, 8), (4, 9);

-- =========== 4. USUARIOS DEL SISTEMA ===========
INSERT INTO usuarios (id, nombre, email, password, rol_id, esta_activo) VALUES 
(1, 'Administrador Central SICA', 'admin@zonaacme.com', '$2a$12$e8Y5.5O/6/6B32Wp2qVHQeg0R82p0x.02j9K8W5O31kR9Y4y7X9gK', 1, TRUE),
(2, 'Capitán Fernando Rojas (Supervisor)', 'supervisor.seguridad@zonaacme.com', '$2a$12$e8Y5.5O/6/6B32Wp2qVHQeg0R82p0x.02j9K8W5O31kR9Y4y7X9gK', 2, TRUE),
(3, 'Oficial Carlos Méndez (Guarda Portería)', 'guarda.porteria1@zonaacme.com', '$2a$12$e8Y5.5O/6/6B32Wp2qVHQeg0R82p0x.02j9K8W5O31kR9Y4y7X9gK', 3, TRUE),
(4, 'Oficial Andrea Silva (Guarda Bahía)', 'guarda.bahia@zonaacme.com', '$2a$12$e8Y5.5O/6/6B32Wp2qVHQeg0R82p0x.02j9K8W5O31kR9Y4y7X9gK', 3, TRUE),
(5, 'Dra. Valentina Duque (Directora BioGen)', 'v.duque@biogenacme.com', '$2a$12$e8Y5.5O/6/6B32Wp2qVHQeg0R82p0x.02j9K8W5O31kR9Y4y7X9gK', 4, TRUE),
(6, 'Ing. Mauricio Restrepo (Gerente Quantum)', 'm.restrepo@quantumdynamics.com', '$2a$12$e8Y5.5O/6/6B32Wp2qVHQeg0R82p0x.02j9K8W5O31kR9Y4y7X9gK', 4, TRUE);

-- =========== 5. ESTADOS DE ACCESO Y ESTADOS DE VISITA ===========
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
(7, 'Cerrada por Sistema (Salida Olvidada)');

-- =========== 6. ZONAS Y PUNTOS DE CONTROL FÍSICOS ===========
INSERT INTO zonas (id, codigo, nombre, descripcion, aforo_maximo, hora_apertura, hora_cierre, requiere_autorizacion_especial, esta_activa) VALUES 
(1, 'ZONA_LOBBY', 'Lobby Principal y Recepción', 'Área de recepción general, control de acceso peatonal y salas de espera', 200, '05:00:00', '23:00:00', FALSE, TRUE),
(2, 'ZONA_TECH_HUB', 'Torre de Innovación & Tech Hub', 'Pisos corporativos de oficinas para empresas de tecnología y software', 350, '06:00:00', '21:00:00', FALSE, TRUE),
(3, 'ZONA_BIOLABS', 'Laboratorios BioGen & Nanotecnología', 'Área biocontenida de investigación farmacéutica y biotecnología', 80, '07:00:00', '19:00:00', TRUE, TRUE),
(4, 'ZONA_DATACENTER', 'Centro de Cómputo Principal (Tier IV)', 'Sala de servidores críticos, telecomunicaciones y almacenamiento en la nube', 15, '00:00:00', '23:59:59', TRUE, TRUE),
(5, 'ZONA_PARKING', 'Estacionamiento Subterráneo S1/S2', 'Bahías vehiculares para funcionarios, visitantes autorizados y logística', 150, '05:30:00', '22:30:00', FALSE, TRUE);

INSERT INTO puntos_control (id, codigo, nombre, zona_id, tipo_punto, esta_activo) VALUES 
(1, 'PC_TORN_01', 'Torniquete Peatonal 1 (Entrada Norte)', 1, 'TORNIQUETE_PEATONAL', TRUE),
(2, 'PC_TORN_02', 'Torniquete Peatonal 2 (Entrada Sur)', 1, 'TORNIQUETE_PEATONAL', TRUE),
(3, 'PC_ELEV_TECH', 'Control Biométrico Ascensores Tech Hub', 2, 'PUERTA_BIOMETRICA', TRUE),
(4, 'PC_PUERTA_BIOLAB', 'Puerta Blindada Acceso Laboratorios', 3, 'PUERTA_BIOMETRICA', TRUE),
(5, 'PC_DC_RESTRINGIDO', 'Esclusa de Seguridad Datacenter', 4, 'PUERTA_BIOMETRICA', TRUE),
(6, 'PC_TALANQUERA_VEH', 'Talanquera Vehicular Acceso S1', 5, 'TALANQUERA_VEHICULAR', TRUE);

-- =========== 7. EMPRESAS INQUILINAS DE ZONA ACME ===========
INSERT INTO empresas (id, nit, nombre, contacto_principal, email_contacto, telefono_contacto, piso_ubicacion, esta_activa) VALUES 
(1, '900.123.456-1', 'Acme CyberDefense Labs', 'Ing. Roberto Gómez', 'rgomez@acmedefense.com', '601-555-0101', 'Piso 5', TRUE),
(2, '900.789.012-3', 'BioGen Innovations S.A.', 'Dra. Valentina Duque', 'vduque@biogenacme.com', '601-555-0202', 'Piso 3', TRUE),
(3, '901.345.678-5', 'Quantum Dynamics & Robotics', 'Ing. Mauricio Restrepo', 'mrestrepo@quantumdynamics.com', '601-555-0303', 'Piso 4', TRUE),
(4, '901.901.234-7', 'Apex Cloud Systems Corp', 'Lic. Sofía Carvajal', 'scarvajal@apexcloud.io', '601-555-0404', 'Piso 2', TRUE),
(5, '902.567.890-9', 'Servicios Externos de Limpieza & Logística', 'Sr. Germán Herrera', 'contacto@servilogistica.com', '601-555-0505', 'Piso 1', TRUE);

-- =========== 8. DIRECTORIO DE PERSONAS ===========
INSERT INTO personas (id, documento_identidad, tipo_documento, nombre, email, telefono, empresa_id, tipo_persona, estado_acceso_id, url_foto) VALUES 
-- Trabajadores de planta
(1, '10102020', 'CC', 'Roberto Carlos Gómez (Líder Ciberseguridad)', 'rgomez@acmedefense.com', '3001234567', 1, 'Trabajador', 1, 'https://images.unsplash.com/photo-1534528741775-53994a69daeb'),
(2, '10203040', 'CC', 'Dra. Valentina Duque (Científica Senior)', 'vduque@biogenacme.com', '3109876543', 2, 'Trabajador', 1, 'https://images.unsplash.com/photo-1580489944761-15a19d654956'),
(3, '10304050', 'CC', 'Ing. Mauricio Restrepo (Arquitecto Cloud)', 'mrestrepo@quantumdynamics.com', '3156781234', 3, 'Trabajador', 1, 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d'),
(4, '10405060', 'CC', 'Lic. Sofía Carvajal (Líder DevOps)', 'scarvajal@apexcloud.io', '3204567890', 4, 'Trabajador', 1, 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2'),

-- Invitados, Contratistas y Proveedores
(5, '80809090', 'CC', 'Mario Alberto Visitante (Auditor ISO 27001)', 'mario.auditor@certivalid.com', '3118901234', 1, 'Invitado', 1, 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e'),
(6, '70708080', 'CC', 'Elena Torres (Especialista Redes Cisco)', 'elena.redes@telecomexpert.com', '3187654321', 4, 'Contratista', 1, 'https://images.unsplash.com/photo-1544005313-94ddf0286df2'),
(7, '60607070', 'CC', 'Dr. Julian Arango (Consultor Genética)', 'jarango@biotechconsulting.org', '3012349876', 2, 'Invitado', 1, 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d'),
(8, '99998888', 'CC', 'Persona Restringida (Ex-empleado sancionado)', 'restringido@correo.com', '3000000000', 1, 'Invitado', 2, 'https://images.unsplash.com/photo-1492562080023-ab3db95bfbce');

-- =========== 9. REGISTRO DE VISITAS (DEMOSTRACIÓN DE LOS 4 FLUJOS) ===========
INSERT INTO visitas (id, persona_id, anfitrion_id, motivo, fecha_prevista_inicio, fecha_prevista_fin, fecha_entrada, fecha_salida, estado_visita_id, vehiculo_placa, visita_aprobada_por, fecha_aprobacion) VALUES 
-- Flujo 1: Invitado Pre-registrado Aprobado
(1, 5, 1, 'Auditoría Anual de Seguridad de la Información ISO 27001', '2026-09-03 08:00:00', '2026-09-03 18:00:00', '2026-09-03 08:15:22', NULL, 3, 'ABC-123', 1, '2026-09-02 17:00:00'),

-- Flujo 2: Invitado No Anunciado (Pendiente de Aprobación en Tiempo Real)
(2, 7, 2, 'Reunión de urgencia para validación de resultados de laboratorio', '2026-09-03 10:00:00', '2026-09-03 12:00:00', NULL, NULL, 1, 'XYZ-789', NULL, NULL),

-- Flujo 3: Regularización de Salida Olvidada
(3, 6, 4, 'Mantenimiento preventivo de enlaces de fibra óptica', '2026-09-02 14:00:00', '2026-09-02 18:00:00', '2026-09-02 14:10:00', NULL, 7, 'KLR-456', 4, '2026-09-02 13:50:00'),

-- Visita Finalizada con Salida Normal
(4, 6, 4, 'Mantenimiento de Servidores Core (Nueva Sesión tras regularización)', '2026-09-03 09:00:00', '2026-09-03 13:00:00', '2026-09-03 09:05:00', '2026-09-03 12:55:00', 4, 'KLR-456', 4, '2026-09-03 08:50:00');

-- =========== 10. REGISTROS ATÓMICOS DE PASO POR TORNIQUETES ===========
INSERT INTO registros_acceso (id, persona_id, punto_control_id, visita_id, fecha_hora, tipo_acceso, resultado, observacion) VALUES 
(1, 1, 1, NULL, '2026-09-03 07:45:10', 'ENTRADA', 'PERMITIDO', 'Ingreso laboral de funcionario registrado'),
(2, 5, 1, 1, '2026-09-03 08:15:22', 'ENTRADA', 'PERMITIDO', 'Check-In de visita pre-registrada con aprobación'),
(3, 6, 2, 4, '2026-09-03 09:05:00', 'ENTRADA', 'PERMITIDO', 'Ingreso de contratista autorizado'),
(4, 8, 1, NULL, '2026-09-03 09:20:15', 'ENTRADA', 'DENEGADO_PERSONA_BLOQUEADA', 'Intento de acceso denegado: Persona con sanción activa'),
(5, 6, 2, 4, '2026-09-03 12:55:00', 'SALIDA', 'PERMITIDO', 'Check-Out normal registrado en portería');

-- =========== 11. INCIDENTES DE SEGURIDAD FÍSICA ===========
INSERT INTO incidentes (id, visita_id, persona_id, reportado_por_id, nivel_gravedad, fecha, descripcion, acciones_tomadas) VALUES 
(1, NULL, 8, 2, 'GRAVE', '2026-09-03 09:22:00', 'Intento de forzar torniquete peatonal norte por parte de ex-empleado con prohibición de ingreso', 'Activación de protocolo de seguridad perimetral y custodia por guardas en lobby'),
(2, 1, 5, 3, 'LEVE', '2026-09-03 11:30:00', 'Visitante extravió temporalmente su credencial física en el área común del piso 5', 'Emisión de carnet provisional y entrega formal al anfitrión');

-- =========== 12. BITÁCORA INMUTABLE DE AUDITORÍA FORENSE ===========
INSERT INTO bitacora_auditoria (id, usuario_id, fecha_hora, accion_realizada, tabla_afectada, registro_id_afectado, direccion_ip, detalles) VALUES 
(1, 1, '2026-09-03 07:00:00', 'LOGIN_EXITOSO', 'usuarios', 1, '192.168.1.10', 'Inicio de sesión administrativo central'),
(2, 3, '2026-09-03 07:30:00', 'LOGIN_EXITOSO', 'usuarios', 3, '192.168.1.25', 'Apertura de turno en torniquetes norte'),
(3, 3, '2026-09-03 08:15:22', 'ACCESO_CHECKIN', 'visitas', 1, '192.168.1.25', 'Check-in procesado para visitante CC 80809090 (Mario Visitante)'),
(4, 3, '2026-09-03 09:20:15', 'ALERTA_ACCESO_DENEGADO', 'personas', 8, '192.168.1.25', 'Alerta de intrusión: Detección de persona bloqueada CC 99998888'),
(5, 2, '2026-09-03 09:25:00', 'REGISTRO_INCIDENTE', 'incidentes', 1, '192.168.1.15', 'Registro forense de incidente GRAVE en torniquetes norte'),
(6, 4, '2026-09-03 09:30:00', 'CAMBIO_ESTADO_VISITA', 'visitas', 3, '192.168.1.30', 'Regularización automática: Visita cerrada por sistema (salida olvidada)');
