package com.zonaacme.sica.auth.adapters;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Utilitario criptográfico para la generación de sales aleatorias y hashing seguro de contraseñas.
 *
 * <p><b>Principios de Seguridad y SOLID:</b></p>
 * <ul>
 *   <li><b>SRP (Single Responsibility Principle):</b> Su única responsabilidad es el hashing y verificación segura
 *   de credenciales sin almacenar contraseñas en texto plano.</li>
 *   <li><b>Resistencia a Timing Attacks:</b> Utiliza {@link MessageDigest#isEqual(byte[], byte[])} para comparar hashes en tiempo constante.</li>
 *   <li><b>Salting Criptográfico:</b> Emplea {@link SecureRandom} para garantizar aleatoriedad no determinística en cada salt.</li>
 * </ul>
 */
public final class PasswordHasher {

    private static final String ALGORITMO = "SHA-256";
    private static final int SALT_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
        // Constructor privado para evitar instanciación
    }

    /**
     * Genera un salt criptográfico pseudoaleatorio en formato hexadecimal.
     *
     * @return Cadena hexadecimal del salt.
     */
    public static String generarSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return HexFormat.of().formatHex(salt);
    }

    /**
     * Genera el hash criptográfico SHA-256 combinando la contraseña en texto plano y el salt provisto.
     *
     * @param password Contraseña en texto plano.
     * @param salt Salt en formato hexadecimal.
     * @return Cadena hexadecimal del hash generado.
     */
    public static String hashPassword(String password, String salt) {
        Objects.requireNonNull(password, "La contraseña no puede ser nula");
        Objects.requireNonNull(salt, "El salt no puede ser nulo");

        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITMO);
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo de hashing no soportado: " + ALGORITMO, e);
        }
    }

    /**
     * Compara en tiempo constante la contraseña en texto plano contra el hash y salt esperados.
     *
     * @param passwordEnTexto Contraseña en texto plano a verificar.
     * @param salt Salt original del usuario.
     * @param hashEsperado Hash almacenado en la base de datos o repositorio.
     * @return {@code true} si coinciden; {@code false} si son distintos o si los argumentos son nulos.
     */
    public static boolean verificar(String passwordEnTexto, String salt, String hashEsperado) {
        if (passwordEnTexto == null || salt == null || hashEsperado == null) {
            return false;
        }

        String hashCalculado = hashPassword(passwordEnTexto, salt);
        byte[] calculadoBytes = hashCalculado.getBytes(StandardCharsets.UTF_8);
        byte[] esperadoBytes = hashEsperado.getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(calculadoBytes, esperadoBytes);
    }
}
