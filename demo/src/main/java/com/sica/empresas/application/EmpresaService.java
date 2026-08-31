package com.sica.empresas.application;

import com.sica.empresas.domain.Empresa;
import com.sica.empresas.domain.EmpresaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de Aplicación para Empresas.
 * Contiene la lógica de negocio y coordina las operaciones entre la UI y el Dominio/Infraestructura.
 */
public class EmpresaService {
    
    private final EmpresaRepository repository;

    // Inyección de dependencias a través del constructor
    public EmpresaService(EmpresaRepository repository) {
        this.repository = repository;
    }

    public boolean registrarEmpresa(String nombre, String contactoPrincipal) {
        if (nombre == null || nombre.trim().isEmpty()) {
            System.err.println("El nombre de la empresa no puede estar vacío.");
            return false;
        }
        Empresa nuevaEmpresa = new Empresa(0, nombre, contactoPrincipal);
        return repository.save(nuevaEmpresa);
    }

    public List<Empresa> listarTodas() {
        return repository.findAll();
    }

    public Optional<Empresa> obtenerPorId(int id) {
        return repository.findById(id);
    }

    public boolean actualizarEmpresa(int id, String nombre, String contactoPrincipal) {
        Optional<Empresa> empresaOpt = repository.findById(id);
        if (empresaOpt.isPresent()) {
            Empresa empresa = empresaOpt.get();
            if (nombre != null && !nombre.trim().isEmpty()) {
                empresa.setNombre(nombre);
            }
            if (contactoPrincipal != null) {
                empresa.setContactoPrincipal(contactoPrincipal);
            }
            return repository.update(empresa);
        }
        return false;
    }

    public boolean eliminarEmpresa(int id) {
        return repository.delete(id);
    }
}
