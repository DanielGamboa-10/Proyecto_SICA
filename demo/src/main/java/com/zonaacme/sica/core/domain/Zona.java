package com.zonaacme.sica.core.domain;

import com.zonaacme.sica.common.exceptions.DomainRuleException;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio que modela una zona física o perímetro de seguridad dentro de las instalaciones de Zona ACME.
 *
 * <p><b>Principios de Diseño y SOLID:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Modela las restricciones de acceso físico, aforo y franjas horarias permitidas.</li>
 *   <li><b>Invariantes de Dominio:</b> Valida que los rangos horarios y capacidades máximas sean lógicas y consistentes.</li>
 * </ul>
 */
public class Zona {

    private final String id;
    private final String codigo;
    private String nombre;
    private String descripcion;
    private int aforoMaximo;
    private LocalTime horaInicioPermitida;
    private LocalTime horaFinPermitida;
    private boolean requiereAprobacionEspecial;
    private boolean activo;

    public Zona(String id, String codigo, String nombre, String descripcion,
                int aforoMaximo, LocalTime horaInicioPermitida, LocalTime horaFinPermitida,
                boolean requiereAprobacionEspecial, boolean activo) {
        this.id = Objects.requireNonNull(id, "ID de zona no puede ser nulo");
        this.codigo = Objects.requireNonNull(codigo, "Código de zona no puede ser nulo").trim().toUpperCase();
        this.nombre = Objects.requireNonNull(nombre, "Nombre de zona no puede ser nulo").trim();
        this.descripcion = descripcion != null ? descripcion.trim() : "";
        if (aforoMaximo <= 0) {
            throw new DomainRuleException("El aforo máximo debe ser mayor a 0.");
        }
        this.aforoMaximo = aforoMaximo;
        this.horaInicioPermitida = horaInicioPermitida != null ? horaInicioPermitida : LocalTime.of(0, 0);
        this.horaFinPermitida = horaFinPermitida != null ? horaFinPermitida : LocalTime.of(23, 59, 59);
        this.requiereAprobacionEspecial = requiereAprobacionEspecial;
        this.activo = activo;
    }

    public static Zona nueva(String codigo, String nombre, String descripcion,
                             int aforoMaximo, LocalTime inicio, LocalTime fin, boolean requiereAprobacionEspecial) {
        return new Zona(
                UUID.randomUUID().toString(),
                codigo,
                nombre,
                descripcion,
                aforoMaximo,
                inicio,
                fin,
                requiereAprobacionEspecial,
                true
        );
    }

    /**
     * Verifica si una hora determinada se encuentra dentro del rango operativo permitido para la zona.
     */
    public boolean esHorarioPermitido(LocalTime hora) {
        if (hora == null) return false;
        if (horaInicioPermitida.isBefore(horaFinPermitida)) {
            return !hora.isBefore(horaInicioPermitida) && !hora.isAfter(horaFinPermitida);
        } else {
            // Rango nocturno cruzando medianoche
            return !hora.isBefore(horaInicioPermitida) || !hora.isAfter(horaFinPermitida);
        }
    }

    public void actualizarHorario(LocalTime inicio, LocalTime fin) {
        this.horaInicioPermitida = Objects.requireNonNull(inicio, "Hora inicio no puede ser nula");
        this.horaFinPermitida = Objects.requireNonNull(fin, "Hora fin no puede ser nula");
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    public String getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getAforoMaximo() {
        return aforoMaximo;
    }

    public LocalTime getHoraInicioPermitida() {
        return horaInicioPermitida;
    }

    public LocalTime getHoraFinPermitida() {
        return horaFinPermitida;
    }

    public boolean isRequiereAprobacionEspecial() {
        return requiereAprobacionEspecial;
    }

    public boolean isActivo() {
        return activo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Zona zona = (Zona) o;
        return Objects.equals(codigo, zona.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return "Zona{" +
                "codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", aforo=" + aforoMaximo +
                ", horario=" + horaInicioPermitida + " - " + horaFinPermitida +
                '}';
    }
}
