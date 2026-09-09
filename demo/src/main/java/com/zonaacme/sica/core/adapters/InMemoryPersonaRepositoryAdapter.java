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
        boolean cargadoDesdeBD = cargarDesdeMySQL();
        if (!cargadoDesdeBD || personasPorId.isEmpty()) {
            inicializarSemilla();
        }
    }

    private void inicializarSemilla() {
        Persona p1 = Persona.nuevo("CC", "10102020", "Roberto", "Gómez (Ciberseguridad)", "rgomez@acmedefense.com", "3001234567", "Acme CyberDefense Labs", TipoPersona.EMPLEADO);
        Persona p2 = Persona.nuevo("CC", "10203040", "Valentina", "Duque (Científica Senior)", "vduque@biogenacme.com", "3109876543", "BioGen Innovations S.A.", TipoPersona.EMPLEADO);
        Persona p3 = Persona.nuevo("CC", "10304050", "Mauricio", "Restrepo (Arquitecto Cloud)", "mrestrepo@quantumdynamics.com", "3156781234", "Quantum Dynamics & Robotics", TipoPersona.EMPLEADO);
        Persona p4 = Persona.nuevo("CC", "10405060", "Sofía", "Carvajal (Líder DevOps)", "scarvajal@apexcloud.io", "3204567890", "Apex Cloud Systems Corp", TipoPersona.EMPLEADO);
        Persona p5 = Persona.nuevo("CC", "80809090", "Mario Alberto", "Visitante (Auditor ISO 27001)", "mario.auditor@certivalid.com", "3118901234", "Acme CyberDefense Labs", TipoPersona.VISITANTE);
        Persona p6 = Persona.nuevo("CC", "70708080", "Elena", "Torres (Redes Cisco)", "elena.redes@telecomexpert.com", "3187654321", "Apex Cloud Systems Corp", TipoPersona.CONTRATISTA);
        Persona p7 = Persona.nuevo("CC", "60607070", "Julian", "Arango (Consultor Genética)", "jarango@biotechconsulting.org", "3012349876", "BioGen Innovations S.A.", TipoPersona.VISITANTE);
        Persona p8 = Persona.nuevo("CC", "99998888", "Persona Restringida", "(Sancionado)", "restringido@correo.com", "3000000000", "Acme CyberDefense Labs", TipoPersona.VISITANTE);
        p8.desactivar();

        save(p1);
        save(p2);
        save(p3);
        save(p4);
        save(p5);
        save(p6);
        save(p7);
        save(p8);
    }

    private boolean cargarDesdeMySQL() {
        try {
            java.sql.Connection conn = com.sica.shared.infrastructure.config.DatabaseConnection.getInstance().getConnection();
            if (conn != null && !conn.isClosed()) {
                String sql = "SELECT p.id, p.nombre, p.documento_identidad, p.tipo_documento, p.email, p.telefono, " +
                             "p.tipo_persona, p.estado_acceso_id, e.nombre as empresa_nombre " +
                             "FROM personas p LEFT JOIN empresas e ON p.empresa_id = e.id";
                int cargadas = 0;
                try (java.sql.Statement stmt = conn.createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        String doc = rs.getString("documento_identidad");
                        String tipoDoc = rs.getString("tipo_documento");
                        String nombreCompleto = rs.getString("nombre");
                        String email = rs.getString("email");
                        String telefono = rs.getString("telefono");
                        String empresa = rs.getString("empresa_nombre");
                        String tipoStr = rs.getString("tipo_persona");
                        int estadoAcceso = rs.getInt("estado_acceso_id");

                        String nombres = nombreCompleto;
                        String apellidos = "";
                        if (nombreCompleto != null && nombreCompleto.contains(" ")) {
                            int idx = nombreCompleto.indexOf(" ");
                            nombres = nombreCompleto.substring(0, idx);
                            apellidos = nombreCompleto.substring(idx + 1);
                        }

                        TipoPersona tipo = TipoPersona.VISITANTE;
                        if (tipoStr != null) {
                            if (tipoStr.equalsIgnoreCase("Trabajador") || tipoStr.equalsIgnoreCase("Empleado")) {
                                tipo = TipoPersona.EMPLEADO;
                            } else if (tipoStr.equalsIgnoreCase("Contratista")) {
                                tipo = TipoPersona.CONTRATISTA;
                            }
                        }

                        Persona p = Persona.nuevo(
                                tipoDoc != null ? tipoDoc : "CC",
                                doc != null ? doc : "00000000",
                                nombres != null ? nombres : "Sin Nombre",
                                !apellidos.isEmpty() ? apellidos : "Registrado",
                                email != null ? email : (doc + "@zonaacme.com"),
                                telefono != null ? telefono : "3001234567",
                                empresa != null ? empresa : "Acme CyberDefense Labs",
                                tipo
                        );
                        if (estadoAcceso == 2) {
                            p.desactivar();
                        }
                        save(p);
                        cargadas++;
                    }
                }
                if (cargadas > 0) {
                    System.out.println("📊 [SICA] " + cargadas + " personas cargadas exitosamente desde MySQL.");
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ [SICA] Error al leer personas de MySQL (usando respaldo en memoria): " + e.getMessage());
        }
        return false;
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
