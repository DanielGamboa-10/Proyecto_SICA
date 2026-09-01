package com.sica.empresas.ui;

import com.sica.empresas.application.EmpresaService;
import com.sica.empresas.domain.Empresa;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Vista de consola para la gestión de Empresas.
 */
public class EmpresaUI {

    private final EmpresaService empresaService;
    private final Scanner scanner;

    public EmpresaUI(EmpresaService empresaService, Scanner scanner) {
        this.empresaService = empresaService;
        this.scanner = scanner;
    }

    public void mostrarMenu() {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println("\n╔══════════════════════════════╗");
            System.out.println("║   🏢 GESTIÓN DE EMPRESAS     ║");
            System.out.println("╠══════════════════════════════╣");
            System.out.println("║  1. Registrar empresa        ║");
            System.out.println("║  2. Listar todas             ║");
            System.out.println("║  3. Actualizar empresa       ║");
            System.out.println("║  4. Eliminar empresa         ║");
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
                case 1 -> registrarEmpresa();
                case 2 -> listarEmpresas();
                case 3 -> actualizarEmpresa();
                case 4 -> eliminarEmpresa();
                case 0 -> System.out.println("  ↩️ Regresando al menú principal...");
                default -> System.out.println("  ⚠️ Opción no válida.");
            }
        }
    }

    private void registrarEmpresa() {
        System.out.println("\n--- Registrar Nueva Empresa ---");
        System.out.print("  Nombre: ");
        String nombre = scanner.nextLine().trim();

        System.out.print("  Contacto principal: ");
        String contacto = scanner.nextLine().trim();

        boolean resultado = empresaService.registrarEmpresa(nombre, contacto);
        if (resultado) {
            System.out.println("  ✅ Empresa registrada exitosamente.");
        } else {
            System.out.println("  ❌ No se pudo registrar la empresa.");
        }
    }

    private void listarEmpresas() {
        System.out.println("\n--- Lista de Empresas ---");
        List<Empresa> empresas = empresaService.listarTodas();
        if (empresas.isEmpty()) {
            System.out.println("  No hay empresas registradas.");
        } else {
            empresas.forEach(e -> System.out.printf("  [%d] %-30s | Contacto: %s%n",
                    e.getId(), e.getNombre(), e.getContactoPrincipal()));
        }
    }

    private void actualizarEmpresa() {
        System.out.println("\n--- Actualizar Empresa ---");
        System.out.print("  ID de la empresa a actualizar: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());

            Optional<Empresa> empresaOpt = empresaService.obtenerPorId(id);
            if (empresaOpt.isEmpty()) {
                System.out.println("  ❌ No se encontró ninguna empresa con ese ID.");
                return;
            }

            Empresa empresa = empresaOpt.get();
            System.out.printf("  Empresa encontrada: %s%n", empresa.getNombre());

            System.out.print("  Nuevo nombre (Enter para mantener): ");
            String nuevoNombre = scanner.nextLine().trim();
            if (nuevoNombre.isEmpty()) nuevoNombre = empresa.getNombre();

            System.out.print("  Nuevo contacto (Enter para mantener): ");
            String nuevoContacto = scanner.nextLine().trim();
            if (nuevoContacto.isEmpty()) nuevoContacto = empresa.getContactoPrincipal();

            boolean resultado = empresaService.actualizarEmpresa(id, nuevoNombre, nuevoContacto);
            if (resultado) {
                System.out.println("  ✅ Empresa actualizada correctamente.");
            } else {
                System.out.println("  ❌ No se pudo actualizar.");
            }
        } catch (NumberFormatException e) {
            System.out.println("  ⚠️ ID no válido.");
        }
    }

    private void eliminarEmpresa() {
        System.out.println("\n--- Eliminar Empresa ---");
        System.out.print("  ID de la empresa a eliminar: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("  ¿Está seguro? (s/n): ");
            String confirmacion = scanner.nextLine().trim();

            if (confirmacion.equalsIgnoreCase("s")) {
                boolean resultado = empresaService.eliminarEmpresa(id);
                if (resultado) {
                    System.out.println("  ✅ Empresa eliminada.");
                } else {
                    System.out.println("  ❌ No se pudo eliminar (puede tener datos asociados).");
                }
            } else {
                System.out.println("  ⚠️ Operación cancelada.");
            }
        } catch (NumberFormatException e) {
            System.out.println("  ⚠️ ID no válido.");
        }
    }
}
