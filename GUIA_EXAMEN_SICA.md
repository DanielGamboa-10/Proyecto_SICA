# 🎓 GUÍA MAESTRA DE PREPARACIÓN PARA EXAMEN — SICA (SISTEMA DE CONTROL DE ACCESO)

> **Complejo Empresarial Zona Acme**  
> **Versión del Sistema:** SICA v2.0 • Enterprise Architecture  
> **Tecnologías Clave:** Java 17/25, Swing UI (Glassmorphism & Cyberpunk Neon), Concurrencia (Threads, AtomicBoolean, Timers), Java Streams API, Java I/O (NIO/CSV/TXT), MySQL / H2, Arquitectura Hexagonal y Principios SOLID.

---

## ⚡ CÓMO USAR ESTA GUÍA EN EL EXAMEN (TRUCO RÁPIDO)

Todas las 12 funcionalidades clave solicitadas para el examen están **etiquetadas con palabras clave estándar (`KEY_*`)** en el código fuente.

**Para encontrar cualquier funcionalidad en menos de 2 segundos:**
1. Presiona `Ctrl + Shift + F` (o `Cmd + Shift + F` en Mac) en tu IDE (IntelliJ IDEA / Eclipse / VS Code).
2. Escribe la palabra clave correspondiente (ej. `KEY_AFORO_MAXIMO`, `KEY_MODO_EVACUACION`, `KEY_POLLING_GUARDA`).
3. El IDE te llevará exactamente a la clase, método y línea de código donde está implementada.

---

## 📋 TABLA RESUMEN DE FUNCIONALIDADES Y PALABRAS CLAVE

| # | Nivel de Probabilidad | Funcionalidad Clave | Palabra Clave (`KEY_*`) | Archivos Principales |
|---|---|---|---|---|
| **1** | 🔥 **Muy Alta** | **Filtro / Polling en Vivo para Guardas** | `KEY_POLLING_GUARDA` | [VisitasPanel.java](file:///c:/Users/Danie/Proyecto_SICA/demo/src/main/java/com/zonaacme/sica/ui/swing/panels/VisitasPanel.java) |
| **2** | 🔥 **Muy Alta** | **Control de Aforo Máximo Simultáneo** | `KEY_AFORO_MAXIMO` | [ControlAccesoService.java](file:///c:/Users/Danie/Proyecto_SICA/demo/src/main/java/com/zonaacme/sica/core/adapters/ControlAccesoService.java) |
| **3** | 🔥 **Muy Alta** | **Permisos RBAC Granulares** | `KEY_PERMISOS_RBAC` | [Rol.java](file:///c:/Users/Danie/Proyecto_SICA/demo/src/main/java/com/zonaacme/sica/auth/domain/Rol.java), [AuthService.java](file:///c:/Users/Danie/Proyecto_SICA/demo/src/main/java/com/zonaacme/sica/auth/adapters/AuthService.java) |
| **4** | 🔥 **Muy Alta** | **Reportes Agregados con Stream API** | `KEY_STREAM_REPORTES` | [ReportesStreamService.java](file:///c:/Users/Danie/Proyecto_SICA/demo/src/main/java/com/zonaacme/sica/core/services/ReportesStreamService.java), [ReportesPanel.java](file:///c:/Users/Danie/Proyecto_SICA/demo/src/main/java/com/zonaacme/sica/ui/swing/panels/ReportesPanel.java) |
| **5** | 🟢 **Alta** | **Lista Negra y Bloqueo Preventivo** | `KEY_LISTA_NEGRA` | [IncidentesPanel.java](file:///c:/Users/Danie/Proyecto_SICA/demo/src/main/java/com/zonaacme/sica/ui/swing/panels/IncidentesPanel.java) |
| **6** | 🟢 **Alta** | **Exportación de Bitácora / TXT / CSV** | `KEY_EXPORT_CSV` | [ExportadorArchivosService.java](file:///c:/Users/Danie/Proyecto_SICA/demo/src/main/java/com/zonaacme/sica/core/services/ExportadorArchivosService.java), [ReportesPanel.java](file:///c:/Users/Danie/Proyecto_SICA/demo/src/main/java/com/zonaacme/sica/ui/swing/panels/ReportesPanel.java) |
| **7** | 🟢 **Alta** | **Cierre Masivo por Fin de Jornada** | `KEY_CIERRE_JORNADA` | [VisitaService.java](file:///c:/Users/Danie/Proyecto_SICA/demo/src/main/java/com/zonaacme/sica/core/adapters/VisitaService.java), [VisitasPanel.java](file:///c:/Users/Danie/Proyecto_SICA/demo/src/main/java/com/zonaacme/sica/ui/swing/panels/VisitasPanel.java) |
| **8** | 🟢 **Alta** | **Control de Placas Duplicadas** | `KEY_CONTROL_PLACAS` | [VisitaService.java](file:///c:/Users/Danie/Proyecto_SICA/demo/src/main/java/com/zonaacme/sica/core/adapters/VisitaService.java) |
| **9** | 🟡 **Media** | **Bloqueo por Intentos Fallidos (Login)**| `KEY_BLOQUEO_LOGIN` | [AuthService.java](file:///c:/Users/Danie/Proyecto_SICA/demo/src/main/java/com/zonaacme/sica/auth/adapters/AuthService.java), [Usuario.java](file:///c:/Users/Danie/Proyecto_SICA/demo/src/main/java/com/zonaacme/sica/auth/domain/Usuario.java) |
| **10**| 🟡 **Media** | **Buscador en Vivo con TableRowSorter** | `KEY_BUSCADOR_REALTIME`| [ThemeConstants.java](file:///c:/Users/Danie/Proyecto_SICA/demo/src/main/java/com/zonaacme/sica/ui/swing/ThemeConstants.java), Paneles UI |
| **11**| 🟡 **Media** | **Modo Evacuación / Botón de Pánico** | `KEY_MODO_EVACUACION` | [GestorEvacuacionEmergencia.java](file:///c:/Users/Danie/Proyecto_SICA/demo/src/main/java/com/zonaacme/sica/core/services/GestorEvacuacionEmergencia.java), [MainDashboardFrame.java](file:///c:/Users/Danie/Proyecto_SICA/demo/src/main/java/com/zonaacme/sica/ui/swing/MainDashboardFrame.java) |
| **12**| 🟡 **Media** | **Reasignación Dinámica de Anfitrión** | `KEY_REASIGNAR_ANFITRION`| [VisitaService.java](file:///c:/Users/Danie/Proyecto_SICA/demo/src/main/java/com/zonaacme/sica/core/adapters/VisitaService.java), [VisitasPanel.java](file:///c:/Users/Danie/Proyecto_SICA/demo/src/main/java/com/zonaacme/sica/ui/swing/panels/VisitasPanel.java) |

---

## 🔍 DETALLE Y EXPLICACIÓN DE CADA FUNCIONALIDAD

### 1. `KEY_POLLING_GUARDA` — Polling Concurrente en Vivo (UI Guarda)
- **¿Qué es?** Un timer en segundo plano (`javax.swing.Timer`) que consulta periódicamente (cada 3 segundos) el repositorio de visitas para refrescar automáticamente la tabla del guardia sin congelar la UI ni requerir clic manual.
- **Ubicación en código:** `VisitasPanel.java` -> Método `iniciarAutoRefresh()` y badge `"🟢 LIVE POLLING (3s)"`.
- **Cómo defenderlo en el examen:** *"Implementamos un Timer Swing no bloqueante en el EDT (Event Dispatch Thread) que sincroniza el estado de las aprobaciones de los funcionarios en tiempo real."*

---

### 2. `KEY_AFORO_MAXIMO` — Validación de Capacidad Máxima Simultánea
- **¿Qué es?** Regla de negocio en `ControlAccesoService` que valida cuántas visitas están actualmente con estado `EN_CURSO` en la zona solicitada. Si se alcanza o supera `zona.getAforoMaximo()`, el ingreso es denegado con `ResultadoAcceso.DENEGADO_AFORO_MAXIMO`.
- **Ubicación en código:** `ControlAccesoService.java` -> Líneas con la etiqueta `KEY_AFORO_MAXIMO`.
- **Cómo defenderlo en el examen:** *"Antes de permitir el ingreso en torniquete, filtramos por Stream las visitas activas en la zona y validamos contra el aforo máximo de la entidad Zona."*

---

### 3. `KEY_PERMISOS_RBAC` — Permisos Granulares RBAC
- **¿Qué es?** Sistema de permisos desacoplado donde cada `Rol` posee un conjunto inmutable de códigos de permiso (`VISITAS_CREAR`, `VISITAS_APROBAR`, `VISITAS_ANULAR`, `VISITAS_REASIGNAR`, `REPORTES_EXPORTAR`, `PERSONAS_BLOQUEAR`, `SISTEMA_EVACUACION`, etc.).
- **Ubicación en código:** `Rol.java`, `Usuario.java`, y `AuthService.java` (`validarPermiso`).
- **Cómo defenderlo en el examen:** *"No hacemos validaciones hardcodeadas de `if (rol == ADMIN)` en los casos de uso, sino `authUseCase.validarPermiso(token, CODIGO_PERMISO, OPERACION)`."*

---

### 4. `KEY_STREAM_REPORTES` — Reportes con Java Streams API
- **¿Qué es?** Analítica en tiempo real utilizando operadores funcionales de Java:
  - `groupingBy` + `counting()`: Conteo de visitas por estado o accesos por resultado.
  - `TreeMap`: Distribución ordenada cronológicamente de accesos por hora (Horas Pico).
  - `sorted(comparingByValue().reversed())`: Ranking Top 10 visitantes más frecuentes.
- **Ubicación en código:** `ReportesStreamService.java` y `ReportesPanel.java`.
- **Cómo defenderlo en el examen:** *"Aprovechamos el paradigma funcional de Java Streams para procesar colecciones en memoria con complejidad óptima y código declarativo."*

---

### 5. `KEY_LISTA_NEGRA` — Lista Negra y Bloqueo Preventivo
- **¿Qué es?** Cuando se registra un incidente de gravedad `GRAVE` o `CRITICO` en garita, el sistema automáticamente desactiva la entidad `Persona` (`persona.desactivar()`) y persiste el cambio en base de datos. Si la persona intenta ingresar por torniquete, `ControlAccesoService` deniega el acceso de inmediato (`DENEGADO_PERSONA_INACTIVA`).
- **Ubicación en código:** `IncidentesPanel.java` -> `registrarIncidente()` y `desbloquearPersonaSeleccionada()`.
- **Cómo defenderlo en el examen:** *"El módulo de incidentes interactúa directamente con el puerto `PersonaRepositoryPort` para sincronizar las restricciones de seguridad con el motor de torniquetes."*

---

### 6. `KEY_EXPORT_CSV` — Exportación I/O (CSV y TXT de Evacuación)
- **¿Qué es?** Módulo de entrada/salida (`java.io.BufferedWriter` y `java.nio.file.Files`) con codificación UTF-8:
  - Exportación de la tabla actual a `.csv`.
  - Exportación de **Planilla de Evacuación / Personal en Sitio** a `.txt` para brigadistas y bomberos.
  - Exportación de **Bitácora Inmutable de Auditoría** a `.txt`.
- **Ubicación en código:** `ExportadorArchivosService.java` y botones en `ReportesPanel.java`.

---

### 7. `KEY_CIERRE_JORNADA` — Cierre Masivo por Fin de Jornada
- **¿Qué es?** Botón y servicio para finalizar en lote todas las visitas que quedaron abiertas (`EN_CURSO`) al terminar el turno, liberando el aforo de las zonas y actualizando la estampa de check-out.
- **Ubicación en código:** `VisitaService.java` -> `cerrarVisitasFinJornada()` y botón `"🌙 Cierre de Jornada"` en `VisitasPanel.java`.

---

### 8. `KEY_CONTROL_PLACAS` — Validación de Placas Duplicadas
- **¿Qué es?** En `VisitaService.solicitarVisita()`, antes de registrar una visita vehicular, se verifica si la placa ya se encuentra activa dentro del complejo en otra visita `EN_CURSO` o `APROBADA`. Si coincide, se lanza `DomainRuleException`.
- **Ubicación en código:** `VisitaService.java` -> Etiqueta `KEY_CONTROL_PLACAS`.

---

### 9. `KEY_BLOQUEO_LOGIN` — Bloqueo Progresivo tras 3 Intentos Fallidos
- **¿Qué es?** Mecanismo de defensa contra fuerza bruta en `AuthService`. Al llegar a 3 intentos erróneos de contraseña, la cuenta se bloquea por 15 minutos (`usuario.registrarIntentoFallido(3, 15)`) y se emite un `UsuarioBloqueadoEvent` a la bitácora forense.
- **Ubicación en código:** `AuthService.java` y `Usuario.java`.

---

### 10. `KEY_BUSCADOR_REALTIME` — Buscador Reactivo con TableRowSorter
- **¿Qué es?** Componente utilitario que asocia cualquier `JTextField` con un `JTable` usando `DocumentListener` y `RowFilter.regexFilter("(?i)" + texto)`. Filtra instantáneamente por cualquier columna sin recargar la base de datos.
- **Ubicación en código:** `ThemeConstants.instalarBuscadorDinamico()`, integrado en `VisitasPanel`, `IncidentesPanel`, `PersonasZonasPanel` y `ReportesPanel`.

---

### 11. `KEY_MODO_EVACUACION` — Botón de Pánico y Evacuación Global
- **¿Qué es?** Singleton concurrente `GestorEvacuacionEmergencia` basado en `AtomicBoolean`.
  - **Al activarse:** El botón en el header superior cambia a rojo parpadeante (`🚨 EVACUACIÓN ACTIVA`), los torniquetes permiten salida inmediata (`SALIDA`), y se deniegan todos los ingresos (`ENTRADA`) con `DENEGADO_EVACUACION_EMERGENCIA`.
  - **Al desactivarse:** Restablece la operación normal del complejo.
- **Ubicación en código:** `GestorEvacuacionEmergencia.java`, `ControlAccesoService.java`, y `MainDashboardFrame.java`.

---

### 12. `KEY_REASIGNAR_ANFITRION` — Reasignación Dinámica de Anfitrión
- **¿Qué es?** Permite cambiar el anfitrión asignado a una visita cuando el funcionario original no responde o cambia de turno, validando que el nuevo anfitrión sea un empleado activo y registrando el cambio en auditoría.
- **Ubicación en código:** `VisitaService.java` -> `reasignarAnfitrionVisita()` y botón `"🔄 Reasignar Anfitrión"` en `VisitasPanel.java`.

---

## 🛠️ COMANDOS DE COMPILACIÓN Y EJECUCIÓN

### 1. Iniciar Base de Datos MySQL (Docker)
```powershell
docker compose up -d
```
*Configurado para mapear el puerto MySQL `3307:3306` con base de datos `sica_db`.*

### 2. Compilar y Ejecutar Pruebas Unitarias
```powershell
mvn clean test
```

### 3. Ejecutar la Aplicación Gráfica (Swing UI)
```powershell
mvn exec:java -Dexec.mainClass="com.zonaacme.sica.ui.swing.SicaGuiApplication"
```

---

## 👥 USUARIOS Y CREDENCIALES POR DEFECTO PARA DEMOSTRACIÓN

| Usuario | Contraseña | Rol | Permisos Principales |
|---|---|---|---|
| `admin` | `admin123` | **ADMINISTRADOR** | Acceso total a todos los módulos y configuraciones. |
| `auditor` | `audit123` | **AUDITOR** | Reportes, bitácora forense, historial y alertas. |
| `guarda` | `guarda123` | **GUARDIA_SEGURIDAD** | Control de accesos, Check-In/Out, registro de incidentes. |
| `anfitrion`| `host123` | **ANFITRION_EMPLEADO**| Solicitudes y aprobación de visitas. |
| `recepcion`| `recep123` | **RECEPCIONISTA** | Pre-registro de visitas, invitados no anunciados, pases. |

---
**¡Mucho éxito en tu examen! Toda la arquitectura y código están 100% listos y validados.** 🚀
