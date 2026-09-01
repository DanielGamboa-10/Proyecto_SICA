package com.sica.personas.ui;

import com.sica.personas.application.PersonaService;
import com.sica.personas.domain.Persona;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Vista de consola para la gestión de Personas.
 */
public class PersonaUI {

    private final PersonaService personaService;
    private final Scanner scanner;

    public PersonaUI(PersonaService personaService, Scanner scanner) {
        this.personaService = personaService;
        this.scanner = scanner;
    }

    public void mostrarMenu() {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println("\n╔══════════════════════════════╗");
            System.out.println("║   👥 GESTIÓN DE PERSONAS     ║");
            System.out.println("╠══════════════════════════════╣");
            System.out.println("║  1. Registrar persona        ║");
            System.out.println("║  2. Buscar por documento     ║");
            System.out.println("║  3. Listar todas             ║");
            System.out.println("║  4. Actualizar datos         ║");
            System.out.println("║  5. Bloquear acceso          ║");
            System.out.println("║  6. Habilitar acceso         ║");
            System.out.println("║  0. Volver al menú principal ║");
            System.out.println("╚══════════════════════════════╝");
            System.out.print("  Opción: ");

            try {
                opcion = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  ⚠️ Opción no válida.");
                continue;
            }

            switch (opcion) {
                case 1 -> registrarPersona();
                case 2 -> buscarPorDocumento();
                case 3 -> listarPersonas();
                case 4 -> actualizarPersona();
                case 5 -> bloquearAcceso();
                case 6 -> habilitarAcceso();
                case 0 -> System.out.println("  ↩️ Regresando al menú principal...");
                default -> System.out.println("  ⚠️ Opción no válida.");
            }
        }
    }

    private void registrarPersona() {
        System.out.println("\n--- Registrar Nueva Persona ---");
        System.out.print("  Nombre completo: ");
        String nombre = scanner.nextLine().trim();

        System.out.print("  Documento de identidad: ");
        String documento = scanner.nextLine().trim();

        System.out.print("  ID de empresa (0 si no aplica): ");
        int empresaId = 0;
        try { empresaId = Integer.parseInt(scanner.nextLine().trim()); } catch (NumberFormatException ignored) {}

        System.out.print("  Tipo [Trabajador/Invitado]: ");
        String tipo = scanner.nextLine().trim();

        System.out.print("  URL de foto (Enter para omitir): ");
        String urlFoto = scanner.nextLine().trim();

        boolean resultado = personaService.registrarPersona(nombre, documento, empresaId, tipo, urlFoto.isEmpty() ? null : urlFoto);
        if (resultado) {
            System.out.println("  ✅ Persona registrada exitosamente.");
        } else {
            System.out.println("  ❌ No se pudo registrar (¿Documento ya existe?).");
        }
    }

    private void buscarPorDocumento() {
        System.out.println("\n--- Buscar Persona por Documento ---");
        System.out.print("  Documento de identidad: ");
        String documento = scanner.nextLine().trim();

        Optional<Persona> personaOpt = personaService.buscarPorDocumento(documento);
        if (personaOpt.isPresent()) {
            Persona p = personaOpt.get();
            System.out.printf("%n  ✅ Encontrado:%n");
            System.out.printf("  ID:        %d%n", p.getId());
            System.out.printf("  Nombre:    %s%n", p.getNombre());
            System.out.printf("  Documento: %s%n", p.getDocumentoIdentidad());
            System.out.printf("  Tipo:      %s%n", p.getTipoPersona());
            System.out.printf("  Estado:    %s%n", p.getEstadoAccesoId() == 1 ? "✅ Activo" : "🚫 Bloqueado");
            if (p.getUrlFoto() != null && !p.getUrlFoto().isEmpty()) {
                System.out.printf("  Foto:      %s%n", p.getUrlFoto());
            }
        } else {
            System.out.println("  ❌ No se encontró ninguna persona con ese documento.");
        }
    }

    private void listarPersonas() {
        System.out.println("\n--- Lista de Personas ---");
        List<Persona> personas = personaService.listarTodas();
        if (personas.isEmpty()) {
            System.out.println("  No hay personas registradas.");
        } else {
            System.out.printf("  %-5s %-25s %-15s %-12s %-10s%n", "ID", "Nombre", "Documento", "Tipo", "Estado");
            System.out.println("  " + "-".repeat(72));
            personas.forEach(p -> System.out.printf("  %-5d %-25s %-15s %-12s %-10s%n",
                    p.getId(), p.getNombre(), p.getDocumentoIdentidad(),
                    p.getTipoPersona(),
                    p.getEstadoAccesoId() == 1 ? "✅ Activo" : "🚫 Bloqueado"));
        }
    }

    private void actualizarPersona() {
        System.out.println("\n--- Actualizar Datos de Persona ---");
        System.out.print("  ID de la persona a actualizar: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("  Nuevo nombre (Enter para mantener): ");
            String nombre = scanner.nextLine().trim();
            System.out.print("  Nuevo ID empresa (0 para mantener): ");
            int empresaId = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("  Nuevo tipo (Enter para mantener): ");
            String tipo = scanner.nextLine().trim();
            System.out.print("  Nueva URL foto (Enter para mantener): ");
            String urlFoto = scanner.nextLine().trim();

            boolean resultado = personaService.actualizarDatos(id,
                    nombre.isEmpty() ? null : nombre,
                    empresaId,
                    tipo.isEmpty() ? null : tipo,
                    urlFoto.isEmpty() ? null : urlFoto);

            System.out.println(resultado ? "  ✅ Datos actualizados." : "  ❌ No se pudo actualizar.");
        } catch (NumberFormatException e) {
            System.out.println("  ⚠️ ID no válido.");
        }
    }

    private void bloquearAcceso() {
        System.out.println("\n--- Bloquear Acceso ---");
        System.out.print("  ID de la persona a bloquear: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            boolean resultado = personaService.bloquearAcceso(id);
            System.out.println(resultado ? "  🚫 Acceso bloqueado." : "  ❌ No se pudo bloquear.");
        } catch (NumberFormatException e) {
            System.out.println("  ⚠️ ID no válido.");
        }
    }

    private void habilitarAcceso() {
        System.out.println("\n--- Habilitar Acceso ---");
        System.out.print("  ID de la persona a habilitar: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            boolean resultado = personaService.habilitarAcceso(id);
            System.out.println(resultado ? "  ✅ Acceso habilitado." : "  ❌ No se pudo habilitar.");
        } catch (NumberFormatException e) {
            System.out.println("  ⚠️ ID no válido.");
        }
    }
}
