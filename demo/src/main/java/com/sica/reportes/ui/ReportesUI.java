package com.sica.reportes.ui;

import com.sica.incidentes.domain.Incidente;
import com.sica.personas.application.PersonaService;
import com.sica.personas.domain.Persona;
import com.sica.reportes.application.ReportesService;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Vista de consola para Reportes y Estadísticas.
 */
public class ReportesUI {

    private final ReportesService reportesService;
    private final PersonaService personaService;
    private final Scanner scanner;

    public ReportesUI(ReportesService reportesService, PersonaService personaService, Scanner scanner) {
        this.reportesService = reportesService;
        this.personaService = personaService;
        this.scanner = scanner;
    }

    public void mostrarMenu() {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println("\n╔══════════════════════════════════╗");
            System.out.println("║   📊 REPORTES Y ESTADÍSTICAS     ║");
            System.out.println("╠══════════════════════════════════╣");
            System.out.println("║  1. Personas activas             ║");
            System.out.println("║  2. Personas bloqueadas          ║");
            System.out.println("║  3. Personas por empresa         ║");
            System.out.println("║  0. Volver al menú principal     ║");
            System.out.println("╚══════════════════════════════════╝");
            System.out.print("  Opción: ");

            try {
                opcion = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  ⚠️ Opción no válida.");
                continue;
            }

            switch (opcion) {
                case 1 -> reportePersonasActivas();
                case 2 -> reportePersonasBloqueadas();
                case 3 -> reportePersonasPorEmpresa();
                case 0 -> System.out.println("  ↩️ Regresando al menú principal...");
                default -> System.out.println("  ⚠️ Opción no válida.");
            }
        }
    }

    private void reportePersonasActivas() {
        List<Persona> todas = personaService.listarTodas();
        List<Persona> activas = reportesService.filtrarPersonasPorEstado(todas, 1);
        System.out.println("\n--- ✅ Personas con Acceso Activo ---");
        if (activas.isEmpty()) {
            System.out.println("  No hay personas activas.");
        } else {
            activas.forEach(p -> System.out.printf("  [%d] %-25s | %s | Doc: %s%n",
                    p.getId(), p.getNombre(), p.getTipoPersona(), p.getDocumentoIdentidad()));
            System.out.printf("%n  Total: %d persona(s)%n", activas.size());
        }
    }

    private void reportePersonasBloqueadas() {
        List<Persona> todas = personaService.listarTodas();
        List<Persona> bloqueadas = reportesService.filtrarPersonasPorEstado(todas, 2);
        System.out.println("\n--- 🚫 Personas con Prohibición de Ingreso ---");
        if (bloqueadas.isEmpty()) {
            System.out.println("  No hay personas bloqueadas.");
        } else {
            bloqueadas.forEach(p -> System.out.printf("  [%d] %-25s | Doc: %s%n",
                    p.getId(), p.getNombre(), p.getDocumentoIdentidad()));
            System.out.printf("%n  Total: %d persona(s) bloqueada(s)%n", bloqueadas.size());
        }
    }

    private void reportePersonasPorEmpresa() {
        System.out.println("\n--- 🏢 Personas por Empresa ---");
        System.out.print("  ID de la empresa: ");
        try {
            int empresaId = Integer.parseInt(scanner.nextLine().trim());
            List<Persona> todas = personaService.listarTodas();
            System.out.printf("  Personas en empresa #%d:%n", empresaId);
            reportesService.imprimirPersonasPorEmpresa(todas, empresaId);
        } catch (NumberFormatException e) {
            System.out.println("  ⚠️ ID no válido.");
        }
    }
}
