package com.sica.empresas.domain;

import java.util.List;
import java.util.Optional;

/**
 * Puerto (Port) / Interfaz del Repositorio para la entidad Empresa.
 * Define las operaciones CRUD que deben ser implementadas por la infraestructura.
 */
public interface EmpresaRepository {
    
    boolean save(Empresa empresa);
    
    Optional<Empresa> findById(int id);
    
    List<Empresa> findAll();
    
    boolean update(Empresa empresa);
    
    boolean delete(int id);
}
