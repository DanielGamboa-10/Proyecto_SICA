package com.zonaacme.sica.notifications.adapters;

import com.zonaacme.sica.notifications.domain.Notificacion;
import com.zonaacme.sica.notifications.ports.in.NotificationUseCase;
import com.zonaacme.sica.notifications.ports.out.NotificationRepositoryPort;
import com.zonaacme.sica.notifications.ports.out.NotificationSenderPort;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Servicio de aplicación que implementa los casos de uso de gestión y despacho de notificaciones y alertas.
 *
 * <p><b>Arquitectura Hexagonal & Principios SOLID:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Orquesta el flujo de despacho, persistencia y consulta
 *   de notificaciones sin acoplarse a detalles técnicos.</li>
 *   <li><b>DIP (Dependency Inversion Principle):</b> Depende de las abstracciones {@link NotificationRepositoryPort}
 *   y {@link NotificationSenderPort} inyectadas por constructor.</li>
 * </ul>
 */
public class NotificationService implements NotificationUseCase {

    private final NotificationRepositoryPort notificationRepository;
    private final NotificationSenderPort notificationSender;

    public NotificationService(NotificationRepositoryPort notificationRepository,
                               NotificationSenderPort notificationSender) {
        this.notificationRepository = Objects.requireNonNull(notificationRepository, "El repositorio de notificaciones no puede ser nulo");
        this.notificationSender = Objects.requireNonNull(notificationSender, "El emisor de notificaciones no puede ser nulo");
    }

    @Override
    public void enviarNotificacion(Notificacion notificacion) {
        if (notificacion == null) {
            return;
        }

        // 1. Persistir el registro inmutable de la notificación
        notificationRepository.save(notificacion);

        // 2. Transmitir el mensaje a través del canal tecnológico asignado
        notificationSender.send(notificacion);
    }

    @Override
    public List<Notificacion> consultarPorDestinatario(String destinatarioId) {
        if (destinatarioId == null || destinatarioId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(notificationRepository.findByDestinatarioId(destinatarioId));
    }

    @Override
    public List<Notificacion> consultarAlertasSeguridad() {
        return Collections.unmodifiableList(notificationRepository.findAlertasSeguridad());
    }

    @Override
    public void marcarComoLeida(String notificacionId) {
        if (notificacionId == null || notificacionId.trim().isEmpty()) {
            return;
        }

        notificationRepository.findById(notificacionId).ifPresent(notif -> {
            notif.marcarComoLeida();
            notificationRepository.save(notif);
        });
    }

    @Override
    public Optional<Notificacion> consultarPorId(String notificacionId) {
        if (notificacionId == null || notificacionId.trim().isEmpty()) {
            return Optional.empty();
        }
        return notificationRepository.findById(notificacionId);
    }
}
