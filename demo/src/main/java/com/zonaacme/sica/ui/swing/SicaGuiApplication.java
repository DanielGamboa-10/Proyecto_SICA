package com.zonaacme.sica.ui.swing;

import com.zonaacme.sica.audit.adapters.AuditEventListener;
import com.zonaacme.sica.audit.adapters.AuditService;
import com.zonaacme.sica.audit.adapters.InMemoryAuditRepositoryAdapter;
import com.zonaacme.sica.auth.adapters.AuthService;
import com.zonaacme.sica.auth.adapters.InMemoryUsuarioRepositoryAdapter;
import com.zonaacme.sica.common.events.DomainEvent;
import com.zonaacme.sica.common.events.DomainEventPublisher;
import com.zonaacme.sica.core.adapters.*;
import com.zonaacme.sica.notifications.adapters.ConsoleNotificationSenderAdapter;
import com.zonaacme.sica.notifications.adapters.InMemoryNotificationRepositoryAdapter;
import com.zonaacme.sica.notifications.adapters.NotificationEventListener;
import com.zonaacme.sica.notifications.adapters.NotificationService;

import javax.swing.*;

/**
 * Lanzador de la Interfaz Gráfica de Usuario (GUI Swing) para SICA - Zona Acme.
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

        // 2. Adaptadores de Persistencia Secundarios en Memoria (con datos semilla)
        InMemoryUsuarioRepositoryAdapter usuarioRepo = new InMemoryUsuarioRepositoryAdapter();
        InMemoryPersonaRepositoryAdapter personaRepo = new InMemoryPersonaRepositoryAdapter();
        InMemoryZonaRepositoryAdapter zonaRepo = new InMemoryZonaRepositoryAdapter();
        InMemoryVisitaRepositoryAdapter visitaRepo = new InMemoryVisitaRepositoryAdapter();
        InMemoryRegistroAccesoRepositoryAdapter registroAccesoRepo = new InMemoryRegistroAccesoRepositoryAdapter();
        InMemoryAuditRepositoryAdapter auditRepo = new InMemoryAuditRepositoryAdapter();
        InMemoryNotificationRepositoryAdapter notificationRepo = new InMemoryNotificationRepositoryAdapter();
        ConsoleNotificationSenderAdapter notificationSender = new ConsoleNotificationSenderAdapter();

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

        // 5. Lanzar Ventana de Login en el hilo de eventos de Swing (EDT)
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
                    notificationRepo
            );
            loginFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            loginFrame.setVisible(true);
        });
    }
}
