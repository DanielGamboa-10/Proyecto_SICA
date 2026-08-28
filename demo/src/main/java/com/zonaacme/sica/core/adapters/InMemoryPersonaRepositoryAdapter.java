package com.zonaacme.sica.core.adapters;

import com.zonaacme.sica.core.domain.Persona;
import com.zonaacme.sica.core.domain.TipoPersona;
import com.zonaacme.sica.core.ports.out.PersonaRepositoryPort;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador secundario para almacenamiento de personas en memoria con soporte concurrente.
 *
 * <p><b>Arquitectura Hexagonal & Principios SOLID:</b></p>
 * <ul>
 *   <li><b>LSP (Liskov Substitution Principle):</b> Implementa {@link PersonaRepositoryPort} de forma totalmente intercambiable.</li>
 *   <li><b>Concurrencia:</b> Utiliza {@link ConcurrentHashMap} para garantizar seguridad en hilos concurrentes.</li>
 * </ul>
 */
public class InMemoryPersonaRepositoryAdapter implements PersonaRepositoryPort {

    private final Map<String, Persona> personasPorId = new ConcurrentHashMap<>();
    private final Map<String, String> idPorDocumento = new ConcurrentHashMap<>();

    public InMemoryPersonaRepositoryAdapter() {
        inicializarSemilla();
    }

    private void inicializarSemilla() {
        Persona empleado = Persona.nuevo("CC", "10102020", "Roberto", "Empleado", "anfitrion1@zonaacme.com", "3001234567", "Zona ACME S.A.", TipoPersona.EMPLEADO);
        Persona visitante = Persona.nuevo("CC", "80809090", "Mario", "Visitante", "mario.visitante@gmail.com", "3109876543", "Servicios Externos SAS", TipoPersona.VISITANTE);
        Persona contratista = Persona.nuevo("CC", "70708080", "Elena", "Contratista", "elena@redesacme.com", "3154567890", "Redes & Telecom ACME", TipoPersona.CONTRATISTA);

        save(empleado);
        save(visitante);
        save(contratista);
    }

    private String generarClaveDocumento(String tipo, String numero) {
        return (tipo != null ? tipo.trim().toUpperCase() : "") + "_" + (numero != null ? numero.trim() : "");
    }

    @Override
    public void save(Persona persona) {
        Objects.requireNonNull(persona, "La persona no puede ser nula");
        personasPorId.put(persona.getId(), persona);
        idPorDocumento.put(generarClaveDocumento(persona.getTipoDocumento(), persona.getNumeroDocumento()), persona.getId());
    }

    @Override
    public Optional<Persona> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(personasPorId.get(id));
    }

    @Override
    public Optional<Persona> findByDocumento(String tipoDocumento, String numeroDocumento) {
        String id = idPorDocumento.get(generarClaveDocumento(tipoDocumento, numeroDocumento));
        if (id == null) return Optional.empty();
        return Optional.ofNullable(personasPorId.get(id));
    }

    @Override
    public List<Persona> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(personasPorId.values()));
    }

    @Override
    public boolean existsByDocumento(String tipoDocumento, String numeroDocumento) {
        return idPorDocumento.containsKey(generarClaveDocumento(tipoDocumento, numeroDocumento));
    }

    public void reset() {
        personasPorId.clear();
        idPorDocumento.clear();
    }
}
