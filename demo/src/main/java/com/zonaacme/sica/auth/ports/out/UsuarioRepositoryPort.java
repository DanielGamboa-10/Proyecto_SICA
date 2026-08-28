package com.zonaacme.sica.auth.ports.out;

import com.zonaacme.sica.auth.domain.Usuario;
import java.util.List;
import java.util.Optional;

/**
 * Puerto Secundario / de Salida (Driven Port) para la persistencia y consulta de entidades de Usuario.
 *
 * <p><b>Arquitectura Hexagonal & Principios SOLID:</b></p>
 * <ul>
 *   <li><b>DIP (Dependency Inversion Principle):</b> El dominio depende de esta abstracción de repositorio,
 *   desacoplándose de detalles de implementación (In-Memory, JPA/Hibernate, JDBC, etc.).</li>
 *   <li><b>LSP (Liskov Substitution Principle):</b> Cualquier adaptador que implemente esta interfaz es
 *   intercambiable sin alterar la lógica de negocio.</li>
 * </ul>
 */
public interface UsuarioRepositoryPort {

    /**
     * Guarda o actualiza un usuario en el repositorio.
     *
     * @param usuario Instancia del usuario a persistir.
     */
    void save(Usuario usuario);

    /**
     * Busca un usuario por su identificador único.
     *
     * @param id Identificador UUID del usuario.
     * @return {@link Optional} con el usuario encontrado o vacío si no existe.
     */
    Optional<Usuario> findById(String id);

    /**
     * Busca un usuario por su nombre de usuario único.
     *
     * @param username Nombre de usuario.
     * @return {@link Optional} con el usuario o vacío.
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Busca un usuario por su correo electrónico único.
     *
     * @param email Correo electrónico.
     * @return {@link Optional} con el usuario o vacío.
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Retorna todos los usuarios registrados en el sistema.
     *
     * @return Lista inmutable de usuarios.
     */
    List<Usuario> findAll();

    /**
     * Comprueba si ya existe un usuario registrado con el username provisto.
     */
    boolean existsByUsername(String username);

    /**
     * Comprueba si ya existe un usuario registrado con el email provisto.
     */
    boolean existsByEmail(String email);
}
