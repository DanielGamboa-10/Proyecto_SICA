package com.sica.incidentes.ui;

import com.sica.incidentes.application.IncidenteService;
import com.sica.incidentes.domain.Incidente;

import java.util.List;
import java.util.Scanner;

/**
 * Vista de consola para la gestión de Incidentes.
 */
public class IncidenteUI {

    private final IncidenteService incidenteService;
    private final Scanner scanner;

    public IncidenteUI(IncidenteService incidenteService, Scanner scanner) {
        this.incidenteService = incidenteService;
        this.scanner = scanner;
    }

    public void mostrarMenu() {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println("\n╔══════════════════════════════╗");
            System.out.println("║   🚨 GESTIÓN DE INCIDENTES   ║");
            System.out.println("╠══════════════════════════════╣");
            System.out.println("║  1. Registrar incidente      ║");
            System.out.println("║  2. Listar todos             ║");
            System.out.println("║  3. Buscar por visita        ║");
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
                case 1 -> registrarIncidente();
                case 2 -> listarIncidentes();
                case 3 -> buscarPorVisita();
                case 0 -> System.out.println("  ↩️ Regresando al menú principal...");
                default -> System.out.println("  ⚠️ Opción no válida.");
            }
        }
    }

    private void registrarIncidente() {
        System.out.println("\n--- Registrar Incidente de Seguridad ---");
        try {
            System.out.print("  ID de la visita relacionada: ");
            int visitaId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("  ID del usuario que reporta: ");
            int reportadoPorId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("  Descripción del incidente: ");
            String descripcion = scanner.nextLine().trim();

            System.out.print("  ID de persona a bloquear (0 si no aplica): ");
            int personaIdABloquear = Integer.parseInt(scanner.nextLine().trim());

            boolean resultado = incidenteService.registrarIncidenteYBloquearPersona(
                    visitaId, reportadoPorId, descripcion, personaIdABloquear);

            if (resultado) {
                System.out.println("  ✅ Incidente registrado.");
                if (personaIdABloquear > 0) {
                    System.out.println("  🚫 Acceso de la persona bloqueado automáticamente.");
                }
            } else {
                System.out.println("  ❌ No se pudo registrar el incidente.");
            }
        } catch (NumberFormatException e) {
            System.out.println("  ⚠️ Valor no válido ingresado.");
        }
    }

    private void listarIncidentes() {
        System.out.println("\n--- Lista de Todos los Incidentes ---");
        List<Incidente> incidentes = incidenteService.listarTodos();
        if (incidentes.isEmpty()) {
            System.out.println("  No hay incidentes registrados.");
        } else {
            System.out.printf("  %-5s %-10s %-10s %-20s %-30s%n", "ID", "VisitaID", "ReportadoBy", "Fecha", "Descripción");
            System.out.println("  " + "-".repeat(80));
            incidentes.forEach(i -> System.out.printf("  %-5d %-10d %-10d %-20s %-30s%n",
                    i.getId(), i.getVisitaId(), i.getReportadoPorId(),
                    i.getFecha().toString().replace("T", " "),
                    i.getDescripcion().length() > 28 ? i.getDescripcion().substring(0, 28) + "..." : i.getDescripcion()));
        }
    }

    private void buscarPorVisita() {
        System.out.println("\n--- Buscar Incidentes por Visita ---");
        System.out.print("  ID de la visita: ");
        try {
            int visitaId = Integer.parseInt(scanner.nextLine().trim());
            List<Incidente> incidentes = incidenteService.listarPorVisita(visitaId);
            if (incidentes.isEmpty()) {
                System.out.println("  No hay incidentes para esa visita.");
            } else {
                incidentes.forEach(i -> System.out.printf("  [%d] %s - %s%n",
                        i.getId(), i.getFecha(), i.getDescripcion()));
            }
        } catch (NumberFormatException e) {
            System.out.println("  ⚠️ ID no válido.");
        }
    }
}
