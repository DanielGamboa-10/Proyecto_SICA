package com.zonaacme.sica.notifications.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio que representa un mensaje de notificación o alerta en el sistema SICA.
 *
 * <p><b>Principios de Diseño y SOLID:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Encapsula la información, canal y estado de lectura
 *   de un aviso o alerta.</li>
 *   <li><b>Inmutabilidad de contenido:</b> Los datos del mensaje son finales para garantizar fidelidad probatoria.</li>
 * </ul>
 */
public class Notificacion {

    private final String id;
    private final String destinatarioId;
    private final TipoNotificacion tipo;
    private final String asunto;
    private final String cuerpo;
    private final CanalNotificacion canal;
    private final LocalDateTime fechaHora;
    private boolean leida;

    public Notificacion(String id, String destinatarioId, TipoNotificacion tipo,
                        String asunto, String cuerpo, CanalNotificacion canal,
                        LocalDateTime fechaHora, boolean leida) {
        this.id = Objects.requireNonNull(id, "ID de notificación no puede ser nulo");
        this.destinatarioId = Objects.requireNonNull(destinatarioId, "Destinatario no puede ser nulo");
        this.tipo = Objects.requireNonNull(tipo, "Tipo de notificación no puede ser nulo");
        this.asunto = Objects.requireNonNull(asunto, "Asunto no puede ser nulo").trim();
        this.cuerpo = Objects.requireNonNull(cuerpo, "Cuerpo no puede ser nulo").trim();
        this.canal = Objects.requireNonNull(canal, "Canal no puede ser nulo");
        this.fechaHora = fechaHora != null ? fechaHora : LocalDateTime.now();
        this.leida = leida;
    }

    public static Notificacion crear(String destinatarioId, TipoNotificacion tipo,
                                     String asunto, String cuerpo, CanalNotificacion canal) {
        return new Notificacion(
                UUID.randomUUID().toString(),
                destinatarioId,
                tipo,
                asunto,
                cuerpo,
                canal,
                LocalDateTime.now(),
                false
        );
    }

    public static Notificacion alertaSeguridad(String asunto, String detalle) {
        return crear(
                "CENTRAL_SEGURIDAD",
                TipoNotificacion.ALERTA_SEGURIDAD_ACCESO_DENEGADO,
                asunto,
                detalle,
                CanalNotificacion.ALERTA_MONITOR_GUARDIA
        );
    }

    public void marcarComoLeida() {
        this.leida = true;
    }

    public String getId() {
        return id;
    }

    public String getDestinatarioId() {
        return destinatarioId;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public String getAsunto() {
        return asunto;
    }

    public String getCuerpo() {
        return cuerpo;
    }

    public CanalNotificacion getCanal() {
        return canal;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public boolean isLeida() {
        return leida;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notificacion that = (Notificacion) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Notificacion{" +
                "id='" + id + '\'' +
                ", tipo=" + tipo +
                ", destinatario='" + destinatarioId + '\'' +
                ", canal=" + canal +
                ", leida=" + leida +
                '}';
    }
}
