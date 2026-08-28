package com.zonaacme.sica.auth.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Enumeración que define los roles de usuario y su matriz de permisos atómicos asociados (RBAC).
 *
 * <p><b>Principios de Diseño y SOLID:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Modela los roles del sistema y encapsula
 *   la asignación predeterminada de permisos granulares.</li>
 *   <li><b>Inmutabilidad:</b> Los conjuntos de permisos retornados son inmutables
 *   ({@link Collections#unmodifiableSet}) para evitar mutaciones externas en tiempo de ejecución.</li>
 * </ul>
 */
public enum Rol {

    ADMINISTRADOR("Administrador del Sistema", Set.of(
            new Permiso("USUARIOS_GESTIONAR", "Gestión de Usuarios"),
            new Permiso("ROLES_ASIGNAR", "Asignación de Roles"),
            new Permiso("ZONAS_GESTIONAR", "Gestión de Zonas y Puntos de Control"),
            new Permiso("VISITAS_CREAR", "Crear Solicitudes de Visita"),
            new Permiso("VISITAS_APROBAR", "Aprobar o Rechazar Visitas"),
            new Permiso("VISITAS_CONSULTAR", "Consultar Historial de Visitas"),
            new Permiso("ACCESO_CHECKIN", "Registrar Ingreso de Personas"),
            new Permiso("ACCESO_CHECKOUT", "Registrar Salida de Personas"),
            new Permiso("ACCESO_MONITOREAR", "Monitorear Accesos en Vivo"),
            new Permiso("AUDITORIA_CONSULTAR", "Consultar Bitácora de Auditoría"),
            new Permiso("REPORTES_GENERAR", "Generar Reportes del Sistema")
    )),

    GUARDIA_SEGURIDAD("Guardia de Seguridad / Control Físico", Set.of(
            new Permiso("VISITAS_CONSULTAR", "Consultar Historial de Visitas"),
            new Permiso("ACCESO_CHECKIN", "Registrar Ingreso de Personas"),
            new Permiso("ACCESO_CHECKOUT", "Registrar Salida de Personas"),
            new Permiso("ACCESO_MONITOREAR", "Monitorear Accesos en Vivo"),
            new Permiso("ALERTAS_GESTIONAR", "Gestionar Alertas de Seguridad")
    )),

    RECEPCIONISTA("Recepcionista de Acceso", Set.of(
            new Permiso("VISITAS_CREAR", "Crear Solicitudes de Visita"),
            new Permiso("VISITAS_CONSULTAR", "Consultar Historial de Visitas"),
            new Permiso("ACCESO_CHECKIN", "Registrar Ingreso de Personas"),
            new Permiso("ACCESO_CHECKOUT", "Registrar Salida de Personas")
    )),

    ANFITRION_EMPLEADO("Empleado Anfitrión", Set.of(
            new Permiso("VISITAS_CREAR", "Crear Solicitudes de Visita"),
            new Permiso("VISITAS_APROBAR", "Aprobar o Rechazar Visitas"),
            new Permiso("VISITAS_CONSULTAR", "Consultar Historial de Visitas")
    )),

    AUDITOR("Auditor de Seguridad y Cumplimiento", Set.of(
            new Permiso("AUDITORIA_CONSULTAR", "Consultar Bitácora de Auditoría"),
            new Permiso("VISITAS_CONSULTAR", "Consultar Historial de Visitas"),
            new Permiso("ACCESO_MONITOREAR", "Monitorear Accesos en Vivo"),
            new Permiso("REPORTES_GENERAR", "Generar Reportes del Sistema")
    ));

    private final String nombreLegible;
    private final Set<Permiso> permisos;

    Rol(String nombreLegible, Set<Permiso> permisos) {
        this.nombreLegible = Objects.requireNonNull(nombreLegible);
        this.permisos = Collections.unmodifiableSet(new HashSet<>(permisos));
    }

    public String getNombreLegible() {
        return nombreLegible;
    }

    public Set<Permiso> getPermisos() {
        return permisos;
    }

    /**
     * Verifica si este rol posee un permiso específico por su código atómico.
     *
     * @param codigoPermiso Código en minúsculas/mayúsculas del permiso a validar.
     * @return {@code true} si el rol contiene el permiso; {@code false} en caso contrario.
     */
    public boolean tienePermiso(String codigoPermiso) {
        if (codigoPermiso == null || codigoPermiso.isBlank()) {
            return false;
        }
        String normalizado = codigoPermiso.trim().toLowerCase();
        return permisos.stream().anyMatch(p -> p.getCodigo().equals(normalizado));
    }

    /**
     * Verifica si este rol posee el permiso dado.
     *
     * @param permiso Instancia del permiso a validar.
     * @return {@code true} si el rol contiene el permiso; {@code false} en caso contrario.
     */
    public boolean tienePermiso(Permiso permiso) {
        if (permiso == null) {
            return false;
        }
        return tienePermiso(permiso.getCodigo());
    }
}
