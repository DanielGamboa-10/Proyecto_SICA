package com.sica.personas.application;

import com.sica.personas.domain.Persona;
import com.sica.personas.domain.PersonaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de Aplicación para Personas.
 */
public class PersonaService {

    private final PersonaRepository repository;

    public PersonaService(PersonaRepository repository) {
        this.repository = repository;
    }

    public boolean registrarPersona(String nombre, String documento, int empresaId, 
                                    String tipoPersona, String urlFoto) {
        // Validaciones básicas
        if (nombre == null || documento == null || tipoPersona == null) {
            System.err.println("Datos incompletos para registrar persona.");
            return false;
        }

        // Verificar si ya existe el documento
        if (repository.findByDocumento(documento).isPresent()) {
            System.err.println("Ya existe una persona con el documento: " + documento);
            return false;
        }

        // Estado de acceso por defecto 1 (Activo)
        Persona nuevaPersona = new Persona(0, nombre, documento, empresaId, tipoPersona, 1, urlFoto);
        return repository.save(nuevaPersona);
    }

    public Optional<Persona> buscarPorDocumento(String documento) {
        return repository.findByDocumento(documento);
    }

    public List<Persona> listarTodas() {
        return repository.findAll();
    }

    public boolean actualizarDatos(int idPersona, String nuevoNombre, int nuevaEmpresaId, 
                                   String nuevoTipo, String nuevaUrlFoto) {
        Optional<Persona> personaOpt = repository.findById(idPersona);
        if (personaOpt.isPresent()) {
            Persona persona = personaOpt.get();
            if (nuevoNombre != null && !nuevoNombre.isEmpty()) persona.setNombre(nuevoNombre);
            if (nuevaEmpresaId > 0) persona.setEmpresaId(nuevaEmpresaId);
            if (nuevoTipo != null && !nuevoTipo.isEmpty()) persona.setTipoPersona(nuevoTipo);
            if (nuevaUrlFoto != null) persona.setUrlFoto(nuevaUrlFoto);
            
            return repository.update(persona);
        }
        return false;
    }

    public boolean bloquearAcceso(int idPersona) {
        // Estado 2 = Con Prohibición de Ingreso
        return repository.updateEstadoAcceso(idPersona, 2);
    }

    public boolean habilitarAcceso(int idPersona) {
        // Estado 1 = Activo
        return repository.updateEstadoAcceso(idPersona, 1);
    }
}
