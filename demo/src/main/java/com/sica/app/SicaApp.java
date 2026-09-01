package com.sica.app;

import com.sica.auditoria.application.AuditoriaService;
import com.sica.auditoria.domain.BitacoraRepository;
import com.sica.auditoria.infrastructure.BitacoraRepositoryImpl;
import com.sica.empresas.application.EmpresaService;
import com.sica.empresas.domain.EmpresaRepository;
import com.sica.empresas.infrastructure.EmpresaRepositoryImpl;
import com.sica.empresas.ui.EmpresaUI;
import com.sica.incidentes.application.IncidenteService;
import com.sica.incidentes.domain.IncidenteRepository;
import com.sica.incidentes.infrastructure.IncidenteRepositoryImpl;
import com.sica.incidentes.ui.IncidenteUI;
import com.sica.personas.application.PersonaService;
import com.sica.personas.domain.PersonaRepository;
import com.sica.personas.infrastructure.PersonaRepositoryImpl;
import com.sica.personas.ui.PersonaUI;
import com.sica.reportes.application.ReportesService;
import com.sica.reportes.ui.ReportesUI;

import java.util.Scanner;

/**
 * Punto de entrada principal del Sistema SICA.
 *
 * Aquí se aplica la inyección de dependencias manual (wiring).
 * Cada servicio recibe su repositorio por constructor,
 * respetando el principio de inversión de dependencias (D en SOLID).
 */
public class SicaApp {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // ──── INFRAESTRUCTURA ────
        EmpresaRepository empresaRepo       = new EmpresaRepositoryImpl();
        PersonaRepository personaRepo       = new PersonaRepositoryImpl();
        IncidenteRepository incidenteRepo   = new IncidenteRepositoryImpl();
        BitacoraRepository bitacoraRepo     = new BitacoraRepositoryImpl();

        // ──── SERVICIOS ────
        EmpresaService empresaService     = new EmpresaService(empresaRepo);
        PersonaService personaService     = new PersonaService(personaRepo);
        AuditoriaService auditoriaService = new AuditoriaService(bitacoraRepo);
        IncidenteService incidenteService = new IncidenteService(incidenteRepo, personaService);
        ReportesService reportesService   = new ReportesService();

        // ──── VISTAS ────
        EmpresaUI  empresaUI  = new EmpresaUI(empresaService, scanner);
        PersonaUI  personaUI  = new PersonaUI(personaService, scanner);
        IncidenteUI incidenteUI = new IncidenteUI(incidenteService, scanner);
        ReportesUI reportesUI = new ReportesUI(reportesService, personaService, scanner);

        // ──── MENÚ PRINCIPAL ────
        bienvenida();

        int opcion = -1;
        while (opcion != 0) {
            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║        🔐 SISTEMA SICA - MENÚ        ║");
            System.out.println("║   Control de Acceso - Zona Acme      ║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.println("║  1. 🏢 Gestión de Empresas           ║");
            System.out.println("║  2. 👥 Gestión de Personas           ║");
            System.out.println("║  3. 🚨 Gestión de Incidentes         ║");
            System.out.println("║  4. 📊 Reportes y Estadísticas       ║");
            System.out.println("║  0. 🚪 Salir del sistema             ║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.print("  Selecciona una opción: ");

            try {
                opcion = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  ⚠️ Ingresa un número válido.");
                continue;
            }

            switch (opcion) {
                case 1 -> empresaUI.mostrarMenu();
                case 2 -> personaUI.mostrarMenu();
                case 3 -> incidenteUI.mostrarMenu();
                case 4 -> reportesUI.mostrarMenu();
                case 0 -> System.out.println("\n  👋 Cerrando SICA. ¡Hasta pronto!\n");
                default -> System.out.println("  ⚠️ Opción no válida.");
            }
        }

        scanner.close();
    }

    private static void bienvenida() {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║                                                  ║");
        System.out.println("║    🔐 SICA - Sistema Integrado de Control de     ║");
        System.out.println("║           Acceso para Zona Acme                  ║");
        System.out.println("║                                                  ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }
}
