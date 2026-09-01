package com.sica.incidentes.application;

import com.sica.incidentes.domain.Incidente;
import com.sica.incidentes.domain.IncidenteRepository;
import com.sica.personas.application.PersonaService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de Aplicación para Incidentes.
 */
public class IncidenteService {

    private final IncidenteRepository incidenteRepository;
    private final PersonaService personaService; // Dependencia para cambiar estados de acceso

    public IncidenteService(IncidenteRepository incidenteRepository, PersonaService personaService) {
        this.incidenteRepository = incidenteRepository;
        this.personaService = personaService;
    }

    /**
     * Registra un incidente y opcionalmente bloquea el acceso de la persona involucrada.
     */
    public boolean registrarIncidenteYBloquearPersona(int visitaId, int reportadoPorId, 
                                                      String descripcion, int personaIdABloquear) {
        
        if (descripcion == null || descripcion.trim().isEmpty()) {
            System.err.println("La descripción del incidente no puede estar vacía.");
            return false;
        }

        Incidente nuevoIncidente = new Incidente(0, visitaId, reportadoPorId, LocalDateTime.now(), descripcion);
        boolean guardado = incidenteRepository.save(nuevoIncidente);

        if (guardado && personaIdABloquear > 0) {
            // Regla de Negocio: Marcar restricción de acceso sobre la persona
            boolean bloqueado = personaService.bloquearAcceso(personaIdABloquear);
            if (!bloqueado) {
                System.err.println("Advertencia: El incidente se guardó, pero no se pudo bloquear a la persona.");
            }
        }
        
        return guardado;
    }

    public List<Incidente> listarTodos() {
        return incidenteRepository.findAll();
    }

    public List<Incidente> listarPorVisita(int visitaId) {
        return incidenteRepository.findByVisitaId(visitaId);
    }
    
    public Optional<Incidente> obtenerPorId(int id) {
        return incidenteRepository.findById(id);
    }
}
