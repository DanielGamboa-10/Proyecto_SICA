package com.zonaacme.sica.app;

import com.zonaacme.sica.audit.adapters.AuditEventListener;
import com.zonaacme.sica.audit.adapters.AuditService;
import com.zonaacme.sica.audit.adapters.InMemoryAuditRepositoryAdapter;
import com.zonaacme.sica.audit.domain.BitacoraAuditoria;
import com.zonaacme.sica.auth.adapters.AuthService;
import com.zonaacme.sica.auth.adapters.InMemoryUsuarioRepositoryAdapter;
import com.zonaacme.sica.auth.domain.SesionUsuario;
import com.zonaacme.sica.common.events.DomainEvent;
import com.zonaacme.sica.common.events.DomainEventPublisher;
import com.zonaacme.sica.core.adapters.*;
import com.zonaacme.sica.core.domain.*;
import com.zonaacme.sica.notifications.adapters.ConsoleNotificationSenderAdapter;
import com.zonaacme.sica.notifications.adapters.InMemoryNotificationRepositoryAdapter;
import com.zonaacme.sica.notifications.adapters.NotificationEventListener;
import com.zonaacme.sica.notifications.adapters.NotificationService;
import com.zonaacme.sica.notifications.domain.Notificacion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Punto de entrada principal y orquestador del Sistema de Control de Acceso (SICA) - Zona ACME.
 *
 * <p><b>Arquitectura Hexagonal & Principios SOLID:</b></p>
 * <ul>
 *   <li><b>Inversión de Dependencias (DIP) & Contenedor de Inyección:</b> Inicializa y cablea
 *   manualmente todos los adaptadores de persistencia y puertos secundarios hacia los servicios de aplicación.</li>
 *   <li><b>Patrón Observer Desacoplado:</b> Registra los oyentes de auditoría y notificaciones
 *   en el {@link DomainEventPublisher} para asegurar reacción en tiempo real ante eventos de negocio.</li>
 *   <li><b>Single Responsibility Principle (SRP):</b> Orquesta el ciclo de vida y la interfaz de usuario por consola.</li>
 * </ul>
 */
public class SicaApplication {

    private final DomainEventPublisher eventPublisher;
    private final AuthService authService;
    private final VisitaService visitaService;
    private final ControlAccesoService controlAccesoService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    // Repositorios
    private final InMemoryUsuarioRepositoryAdapter usuarioRepo;
    private final InMemoryPersonaRepositoryAdapter personaRepo;
    private final InMemoryZonaRepositoryAdapter zonaRepo;
    private final InMemoryVisitaRepositoryAdapter visitaRepo;
    private final InMemoryRegistroAccesoRepositoryAdapter registroAccesoRepo;
    private final InMemoryAuditRepositoryAdapter auditRepo;
    private final InMemoryNotificationRepositoryAdapter notificationRepo;
    private final ConsoleNotificationSenderAdapter notificationSender;

    public SicaApplication() {
        // 1. Inicialización del Bus de Eventos de Dominio
        this.eventPublisher = DomainEventPublisher.getInstance();
        this.eventPublisher.reset();

        // 2. Inicialización de Adaptadores de Persistencia Secundarios (In-Memory)
        this.usuarioRepo = new InMemoryUsuarioRepositoryAdapter();
        this.personaRepo = new InMemoryPersonaRepositoryAdapter();
        this.zonaRepo = new InMemoryZonaRepositoryAdapter();
        this.visitaRepo = new InMemoryVisitaRepositoryAdapter();
        this.registroAccesoRepo = new InMemoryRegistroAccesoRepositoryAdapter();
        this.auditRepo = new InMemoryAuditRepositoryAdapter();
        this.notificationRepo = new InMemoryNotificationRepositoryAdapter();
        this.notificationSender = new ConsoleNotificationSenderAdapter();

        // 3. Inicialización de Servicios de Aplicación (Casos de Uso)
        this.authService = new AuthService(usuarioRepo, eventPublisher);
        this.visitaService = new VisitaService(visitaRepo, personaRepo, zonaRepo, authService, eventPublisher);
        this.controlAccesoService = new ControlAccesoService(
                registroAccesoRepo,
                personaRepo,
                zonaRepo,
                visitaRepo,
                authService,
                eventPublisher
        );
        this.auditService = new AuditService(auditRepo);
        this.notificationService = new NotificationService(notificationRepo, notificationSender);

        // 4. Registro de Listeners de Eventos Desacoplados (Patrón Observer)
        AuditEventListener auditListener = new AuditEventListener(auditService);
        NotificationEventListener notificationListener = new NotificationEventListener(notificationService);

        this.eventPublisher.subscribe(DomainEvent.class, auditListener);
        this.eventPublisher.subscribe(DomainEvent.class, notificationListener);
    }

    public static void main(String[] args) {
        SicaApplication app = new SicaApplication();
        app.ejecutarDemostracionCompleta();
    }

    /**
     * Ejecuta una demostración integral automatizada de todos los flujos de negocio del sistema.
     */
    public void ejecutarDemostracionCompleta() {
        System.out.println("======================================================================");
        System.out.println(" 🏢 SISTEMA INTEGRAL DE CONTROL DE ACCESO (SICA) - ZONA ACME");
        System.out.println("======================================================================\n");

        // 1. Autenticación de Usuarios con RBAC
        System.out.println("--- [1. AUTENTICACIÓN Y SEGURIDAD RBAC] ---");
        SesionUsuario sesionAdmin = authService.autenticar("admin", "Admin123*");
        System.out.println("✅ Sesión iniciada: Administrador -> " + sesionAdmin.getUsername() + " (Token: " + sesionAdmin.getToken() + ")");

        SesionUsuario sesionGuardia = authService.autenticar("guardia1", "Guardia123*");
        System.out.println("✅ Sesión iniciada: Guardia -> " + sesionGuardia.getUsername() + " (Token: " + sesionGuardia.getToken() + ")\n");

        // 2. Consulta de Personas y Zonas Semilla
        System.out.println("--- [2. MAESTROS DE PERSONAS Y ZONAS] ---");
        Persona empleado = personaRepo.findByDocumento("CC", "10102020").orElseThrow();
        Persona visitante = personaRepo.findByDocumento("CC", "80809090").orElseThrow();
        Zona recepcion = zonaRepo.findZonaByCodigo("ZONA_RECEPCION").orElseThrow();
        PuntoControl torniquete = zonaRepo.findPuntoControlByCodigo("PC_TORN_01").orElseThrow();
        PuntoControl puertaServidores = zonaRepo.findPuntoControlByCodigo("PC_PUERTA_DC").orElseThrow();

        System.out.println("👤 Empleado: " + empleado.getNombreCompleto() + " (" + empleado.getTipoPersona() + ")");
        System.out.println("👤 Visitante: " + visitante.getNombreCompleto() + " (" + visitante.getTipoPersona() + ")");
        System.out.println("📍 Zona: " + recepcion.getNombre() + " | Punto de Control: " + torniquete.getNombre() + "\n");

        // 3. Flujo de Visita: Solicitud y Aprobación
        System.out.println("--- [3. GESTIÓN DE VISITAS CON NOTIFICACIONES AUTOMÁTICAS] ---");
        LocalDateTime inicio = LocalDateTime.now().minusMinutes(10);
        LocalDateTime fin = LocalDateTime.now().plusHours(2);

        SolicitudVisita visita = visitaService.solicitarVisita(
                visitante.getId(),
                empleado.getId(),
                "Reunión de Consultoría de TI",
                inicio,
                fin,
                Set.of(recepcion.getId()),
                "Vehículo placa XYZ-123",
                sesionAdmin.getToken()
        );
        System.out.println("📝 Visita radicada con éxito. ID: " + visita.getId() + " [Estado: " + visita.getEstado() + "]");

        visitaService.aprobarVisita(visita.getId(), "Aprobado por el anfitrión para ingreso", sesionAdmin.getToken());
        System.out.println("✅ Visita aprobada formalmente. Estado: " + visita.getEstado() + "\n");

        // 4. Control de Acceso Físico en Torniquetes
        System.out.println("--- [4. CONTROL DE ACCESO FÍSICO Y EVENTOS DE AUDITORÍA/ALERTA] ---");

        // A. Intento no autorizado en Servidores (Denegado -> Dispara Alerta de Seguridad)
        System.out.println("🚨 Prueba A: Intento de ingreso no autorizado en Sala de Servidores...");
        RegistroAcceso accesoDenegado = controlAccesoService.registrarIngreso(
                visitante.getId(),
                puertaServidores.getId(),
                sesionGuardia.getToken()
        );
        System.out.println("❌ Resultado: " + accesoDenegado.getResultado() + " | Permitido: " + accesoDenegado.esExitoso() + "\n");

        // B. Check-In de Visitante en Torniquete Autorizado (Permitido)
        System.out.println("🚪 Prueba B: Check-In de Visitante en Torniquete Principal...");
        RegistroAcceso checkIn = controlAccesoService.registrarIngreso(
                visitante.getId(),
                torniquete.getId(),
                sesionGuardia.getToken()
        );
        System.out.println("✅ Resultado Check-In: " + checkIn.getResultado() + " | Permitido: " + checkIn.esExitoso());

        // C. Check-Out de Visitante
        System.out.println("🚪 Prueba C: Check-Out de Visitante en Torniquete Principal...");
        RegistroAcceso checkOut = controlAccesoService.registrarSalida(
                visitante.getId(),
                torniquete.getId(),
                sesionGuardia.getToken()
        );
        System.out.println("✅ Resultado Check-Out: " + checkOut.getResultado() + " | Completado: " + checkOut.esExitoso() + "\n");

        // 5. Consulta de Auditoría Inmutable Forense
        System.out.println("--- [5. BITÁCORA INMUTABLE DE AUDITORÍA FORENSE] ---");
        List<BitacoraAuditoria> bitacora = auditService.consultarHistorialCompleto();
        System.out.println("📊 Total eventos auditados: " + bitacora.size());
        for (BitacoraAuditoria b : bitacora) {
            System.out.println("  📜 [" + b.getFechaHora() + "] " + b.getAccion() + " | Entidad: " + b.getEntidadAfectada() + " | Usuario: " + b.getUsuarioId());
        }
        System.out.println();

        // 6. Consulta de Alertas y Notificaciones Despachadas
        System.out.println("--- [6. CENTRO DE NOTIFICACIONES Y ALERTAS DE SEGURIDAD] ---");
        List<Notificacion> alertas = notificationService.consultarAlertasSeguridad();
        System.out.println("🚨 Total de Alertas Críticas Recibidas: " + alertas.size());
        for (Notificacion a : alertas) {
            System.out.println("  ⚠️ " + a.getTipo() + " -> " + a.getAsunto() + " (" + a.getCuerpo() + ")");
        }
        System.out.println("\n======================================================================");
        System.out.println(" 🎯 DEMOSTRACIÓN INTEGRAL SICA FINALIZADA SATISFACTORIAMENTE");
        System.out.println("======================================================================\n");
    }

    // Getters de servicios para testing e integración
    public AuthService getAuthService() { return authService; }
    public VisitaService getVisitaService() { return visitaService; }
    public ControlAccesoService getControlAccesoService() { return controlAccesoService; }
    public AuditService getAuditService() { return auditService; }
    public NotificationService getNotificationService() { return notificationService; }
    public InMemoryPersonaRepositoryAdapter getPersonaRepo() { return personaRepo; }
    public InMemoryZonaRepositoryAdapter getZonaRepo() { return zonaRepo; }
    public InMemoryVisitaRepositoryAdapter getVisitaRepo() { return visitaRepo; }
    public ConsoleNotificationSenderAdapter getNotificationSender() { return notificationSender; }
}
