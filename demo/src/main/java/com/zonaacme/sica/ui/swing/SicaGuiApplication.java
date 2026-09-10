package com.zonaacme.sica.ui.swing;

import com.zonaacme.sica.audit.adapters.AuditEventListener;
import com.zonaacme.sica.audit.adapters.AuditJdbcRepositoryAdapter;
import com.zonaacme.sica.audit.adapters.AuditService;
import com.zonaacme.sica.audit.ports.out.AuditRepositoryPort;
import com.zonaacme.sica.auth.adapters.AuthService;
import com.zonaacme.sica.auth.adapters.UsuarioJdbcRepositoryAdapter;
import com.zonaacme.sica.auth.ports.out.UsuarioRepositoryPort;
import com.zonaacme.sica.common.events.DomainEvent;
import com.zonaacme.sica.common.events.DomainEventPublisher;
import com.zonaacme.sica.concurrency.GestorHilosSica;
import com.zonaacme.sica.core.adapters.*;
import com.zonaacme.sica.core.ports.out.*;
import com.zonaacme.sica.notifications.adapters.ConsoleNotificationSenderAdapter;
import com.zonaacme.sica.notifications.adapters.InMemoryNotificationRepositoryAdapter;
import com.zonaacme.sica.notifications.adapters.NotificationEventListener;
import com.zonaacme.sica.notifications.adapters.NotificationService;
import com.zonaacme.sica.notifications.ports.out.NotificationRepositoryPort;

import javax.swing.*;

/**
 * Lanzador Principal de la Interfaz Gráfica de Usuario (GUI Swing) para SICA - Zona Acme.
 * Configura persistencia en base de datos MySQL (JDBC), hilos de usuario concurrentes y RBAC.
 */
public class SicaGuiApplication {

    public static void main(String[] args) {
        // Configuración de Look and Feel del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // 1. Inicialización del Bus de Eventos de Dominio
        DomainEventPublisher eventPublisher = DomainEventPublisher.getInstance();
        eventPublisher.reset();

        // 2. Adaptadores de Persistencia JDBC conectados a MySQL (sica_db)
        UsuarioRepositoryPort usuarioRepo = new UsuarioJdbcRepositoryAdapter();
        PersonaJdbcRepositoryAdapter personaRepo = new PersonaJdbcRepositoryAdapter();
        ZonaJdbcRepositoryAdapter zonaRepo = new ZonaJdbcRepositoryAdapter();
        VisitaJdbcRepositoryAdapter visitaRepo = new VisitaJdbcRepositoryAdapter();
        visitaRepo.inicializarSemilla(personaRepo, zonaRepo);

        RegistroAccesoRepositoryPort registroAccesoRepo = new RegistroAccesoJdbcRepositoryAdapter();
        AuditRepositoryPort auditRepo = new AuditJdbcRepositoryAdapter();
        NotificationRepositoryPort notificationRepo = new InMemoryNotificationRepositoryAdapter();
        ConsoleNotificationSenderAdapter notificationSender = new ConsoleNotificationSenderAdapter();
        IncidenteRepositoryPort incidenteRepo = new IncidenteJdbcRepositoryAdapter();

        // 3. Servicios de Aplicación
        AuthService authService = new AuthService(usuarioRepo, eventPublisher);
        VisitaService visitaService = new VisitaService(visitaRepo, personaRepo, zonaRepo, authService, eventPublisher);
        ControlAccesoService controlAccesoService = new ControlAccesoService(
                registroAccesoRepo,
                personaRepo,
                zonaRepo,
                visitaRepo,
                authService,
                eventPublisher
        );
        AuditService auditService = new AuditService(auditRepo);
        NotificationService notificationService = new NotificationService(notificationRepo, notificationSender);

        // 4. Suscripción de Eventos para Auditoría y Notificaciones en tiempo real
        eventPublisher.subscribe(DomainEvent.class, new AuditEventListener(auditService));
        eventPublisher.subscribe(DomainEvent.class, new NotificationEventListener(notificationService));

        // 5. 🧵 INICIALIZACIÓN DE HILOS DEFINIDOS POR EL USUARIO (Multithreading / Concurrencia)
        GestorHilosSica gestorHilos = GestorHilosSica.getInstance();
        gestorHilos.inicializarHilos(visitaRepo, auditService, auditRepo, notificationService);
        gestorHilos.inicializarSimulador(controlAccesoService, personaRepo, zonaRepo, "TOKEN_SIMULADOR_SICA");

        // Hook de apagado limpio para detener hilos de fondo al cerrar JVM
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            gestorHilos.detenerTodos();
        }, "SICA-ShutdownHook-Thread"));

        // 6. Lanzar Ventana de Login en el hilo de eventos de Swing (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(
                    authService,
                    visitaService,
                    controlAccesoService,
                    auditService,
                    notificationService,
                    usuarioRepo,
                    personaRepo,
                    zonaRepo,
                    visitaRepo,
                    registroAccesoRepo,
                    auditRepo,
                    notificationRepo,
                    incidenteRepo
            );
            loginFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            loginFrame.setVisible(true);
        });
    }
}
