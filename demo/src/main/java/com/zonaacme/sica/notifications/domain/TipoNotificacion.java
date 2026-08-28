package com.zonaacme.sica.notifications.domain;

/**
 * Clasificación y nivel de severidad de los mensajes del sistema de notificaciones y alertas.
 *
 * <p><b>Principio SOLID aplicado:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Tipifica las razones de comunicación hacia usuarios y guardias.</li>
 * </ul>
 */
public enum TipoNotificacion {
    VISITA_SOLICITADA("Nueva Solicitud de Visita Pendiente", "INFO"),
    VISITA_APROBADA("Solicitud de Visita Aprobada", "SUCCESS"),
    VISITA_LLEGADA_ANFITRION("Su Visita ha Llegado a Recepción", "INFO"),
    ALERTA_SEGURIDAD_ACCESO_DENEGADO("Alerta: Intento de Acceso No Autorizado", "WARNING"),
    ALERTA_USUARIO_BLOQUEADO("Alerta: Cuenta de Usuario Bloqueada", "CRITICAL");

    private final String tituloPredeterminado;
    private final String nivel;

    TipoNotificacion(String tituloPredeterminado, String nivel) {
        this.tituloPredeterminado = tituloPredeterminado;
        this.nivel = nivel;
    }

    public String getTituloPredeterminado() {
        return tituloPredeterminado;
    }

    public String getNivel() {
        return nivel;
    }

    public boolean esCritico() {
        return "CRITICAL".equals(nivel) || "WARNING".equals(nivel);
    }
}
