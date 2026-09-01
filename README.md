# 🔐 SICA — Sistema Integrado de Control de Acceso para Zona Acme

> **Proyecto universitario** | Java Puro | JDBC | Arquitectura Hexagonal + Vertical Slicing | RBAC | Java Streams

---

## 📋 Tabla de Contenidos

1. [Descripción del Proyecto](#descripción-del-proyecto)
2. [Modelo de la Base de Datos](#modelo-de-la-base-de-datos)
3. [Decisiones de Diseño](#decisiones-de-diseño)
4. [Instrucciones de Instalación y Ejecución](#instrucciones-de-instalación-y-ejecución)
5. [Guía de Uso](#guía-de-uso)
6. [Credenciales de Prueba](#credenciales-de-prueba)
7. [Autores](#autores)

---

## 📌 Descripción del Proyecto

### El Problema

El Complejo Empresarial **"Zona Acme"** operaba con un sistema de control de acceso obsoleto: libros de registro en papel y comunicación por radio. Esto generaba:

- ❌ **Vulnerabilidad de Seguridad**: sin registro fiable de quién está dentro del complejo.
- ❌ **Experiencia Ineficiente**: largas filas en horas pico e invitados no anunciados generando cuellos de botella.
- ❌ **Falta de Trazabilidad**: investigar incidentes implicaba revisar páginas de caligrafía ilegible.
- ❌ **Gestión Reactiva**: sin forma de marcar restricciones de acceso de manera inmediata.

### La Solución

**SICA** es una aplicación de escritorio en **Java puro** que moderniza y digitaliza completamente el control de acceso del complejo. Sus funciones clave son:

- ✅ Control de acceso basado en roles (**RBAC**) con permisos granulares leídos desde la base de datos.
- ✅ Registro de visitas con flujos para invitados pre-registrados, no anunciados y regularización de salidas olvidadas.
- ✅ Módulo de **incidentes de seguridad** con bloqueo inmediato de acceso.
- ✅ **Bitácora de auditoría** inmutable que registra cada acción crítica del sistema.
- ✅ Módulo de **reportes y estadísticas** con Java Streams API.

---

## 🗄️ Modelo de la Base de Datos

### Diagrama Entidad-Relación (E-R)

```
┌──────────┐     ┌──────────────────┐     ┌──────────┐
│  roles   │────<│   rol_permisos   │>────│ permisos │
└──────────┘     └──────────────────┘     └──────────┘
     │
     │ tiene
     ▼
┌──────────┐
│ usuarios │
└──────────┘
     │ aprueba visita
     ▼
┌──────────┐       ┌─────────────────────────┐
│  visitas │>──────│ persona_estados_acceso  │
└──────────┘       └─────────────────────────┘
     │                          ▲
     │ pertenece a               │ tiene
     ▼                          │
┌──────────┐               ┌──────────┐
│ personas │───────────────│ empresas │
└──────────┘               └──────────┘
     │
     │ visita (durante)
     ▼
┌─────────────┐      ┌────────────────────────┐
│ incidentes  │      │   bitacora_auditoria   │
└─────────────┘      └────────────────────────┘
```

### Tablas Principales

| Tabla | Descripción |
|---|---|
| `roles` | Define los roles del sistema (Superusuario, Supervisor, Guarda, Funcionario) |
| `permisos` | Permisos atómicos como `crear_usuario`, `registrar_visita`, etc. |
| `rol_permisos` | Tabla intermedia RBAC que asocia roles con permisos |
| `usuarios` | Usuarios del sistema con su rol asignado |
| `empresas` | Empresas dentro del complejo Zona Acme |
| `personas` | Trabajadores e invitados con estado de acceso y URL de foto |
| `visitas` | Registro atómico de cada entrada y salida |
| `incidentes` | Incidentes de seguridad vinculados a visitas |
| `bitacora_auditoria` | Registro inmutable de todas las acciones críticas |

---

## 🏛️ Decisiones de Diseño

### Arquitectura: Hexagonal + Vertical Slicing

El proyecto sigue una **Arquitectura Hexagonal** (Ports & Adapters) organizada por **Vertical Slicing**: cada módulo de negocio (empresas, personas, incidentes, etc.) es un "slice" independiente con sus propias capas internas:

```
com.sica/
├── app/           → Punto de entrada y wiring de dependencias
├── shared/        → Infraestructura compartida (DatabaseConnection)
├── empresas/
│   ├── domain/    → Entidad + interfaz Repository (Puerto)
│   ├── application/ → Servicio con lógica de negocio
│   ├── infrastructure/ → Implementación JDBC (Adaptador)
│   └── ui/        → Vista de consola
├── personas/      → (misma estructura)
├── incidentes/    → (misma estructura)
├── auditoria/     → (misma estructura)
└── reportes/      → (misma estructura)
```

---

### ⚙️ Principios SOLID Aplicados

| Principio | Dónde se Aplica |
|---|---|
| **S** (Single Responsibility) | Cada clase tiene una sola responsabilidad: `EmpresaService` solo maneja lógica de negocio, `EmpresaRepositoryImpl` solo accede a datos, `EmpresaUI` solo muestra menús. |
| **O** (Open/Closed) | Los repositorios están abiertos a extensión (se puede agregar un `EmpresaRepositoryFileImpl`) sin modificar los servicios. |
| **L** (Liskov Substitution) | `EmpresaRepositoryImpl` puede sustituir a `EmpresaRepository` sin que `EmpresaService` note la diferencia. |
| **I** (Interface Segregation) | Cada repositorio tiene su propia interfaz específica (`EmpresaRepository`, `PersonaRepository`, etc.) en vez de una interfaz genérica gigante. |
| **D** (Dependency Inversion) | `SicaApp` construye e inyecta las dependencias por constructor. Los servicios dependen de abstracciones (interfaces), no de implementaciones concretas. |

---

### 🧩 Patrones de Diseño Aplicados

#### 1. 🔵 Singleton — `DatabaseConnection`
```java
// Garantiza una sola conexión a la base de datos en toda la aplicación
public static DatabaseConnection getInstance() {
    if (instance == null) {
        synchronized (DatabaseConnection.class) {
            if (instance == null) {
                instance = new DatabaseConnection();
            }
        }
    }
    return instance;
}
```
**Por qué**: Abrir múltiples conexiones a la BD es costoso y puede causar errores de concurrencia. Un Singleton asegura que solo exista una conexión compartida.

---

#### 2. 🟢 Repository — `EmpresaRepositoryImpl`, `PersonaRepositoryImpl`, etc.
```java
// La interfaz define el contrato (Port)
public interface EmpresaRepository {
    boolean save(Empresa empresa);
    Optional<Empresa> findById(int id);
    List<Empresa> findAll();
    // ...
}

// La implementación JDBC es el Adaptador
public class EmpresaRepositoryImpl implements EmpresaRepository { ... }
```
**Por qué**: Desacopla la lógica de negocio (`EmpresaService`) del mecanismo de persistencia (JDBC/MySQL). Si mañana se cambia a un archivo XML o una API REST, solo se crea una nueva implementación.

---

### ☕ Java Streams y Lambdas — `ReportesService`

```java
// Filtrar personas activas usando Streams y lambda
public List<Persona> filtrarPersonasPorEstado(List<Persona> personas, int estadoId) {
    return personas.stream()
            .filter(persona -> persona.getEstadoAccesoId() == estadoId)
            .collect(Collectors.toList());
}

// Contar incidentes de un usuario
public long contarIncidentesReportadosPorUsuario(List<Incidente> incidentes, int usuarioId) {
    return incidentes.stream()
            .filter(i -> i.getReportadoPorId() == usuarioId)
            .count();
}

// Obtener incidentes en un rango de fechas
public List<Incidente> filtrarIncidentesPorRangoFechas(List<Incidente> incidentes, 
                                                        LocalDateTime inicio, LocalDateTime fin) {
    return incidentes.stream()
            .filter(i -> !i.getFecha().isBefore(inicio) && !i.getFecha().isAfter(fin))
            .collect(Collectors.toList());
}
```

---

## 🚀 Instrucciones de Instalación y Ejecución

### Prerrequisitos

| Herramienta | Versión recomendada |
|---|---|
| Java JDK | 17 o superior |
| MySQL | 8.0 o superior |
| Maven | 3.8 o superior |
| IDE recomendado | IntelliJ IDEA / VS Code |

### Paso 1: Clonar el Repositorio

```bash
git clone https://github.com/DanielGamboa-10/Proyecto_SICA.git
cd Proyecto_SICA
git checkout Devkevin
```

### Paso 2: Configurar la Base de Datos

1. Abrir **MySQL Workbench** o cualquier cliente SQL.
2. Crear la base de datos:
```sql
CREATE DATABASE sica_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sica_db;
```
3. Ejecutar el script de esquema:
```bash
# Desde MySQL Workbench: File → Open SQL Script → seleccionar database/schema.sql
# O desde terminal:
mysql -u root -p sica_db < database/schema.sql
```
4. Ejecutar el script de datos semilla:
```bash
mysql -u root -p sica_db < database/data.sql
```

### Paso 3: Configurar la Conexión

Editar el archivo [`DatabaseConnection.java`](demo/src/main/java/com/sica/shared/infrastructure/config/DatabaseConnection.java):

```java
private final String URL      = "jdbc:mysql://localhost:3306/sica_db";
private final String USER     = "root";        // Tu usuario de MySQL
private final String PASSWORD = "tu_password"; // Tu contraseña de MySQL
```

### Paso 4: Compilar y Ejecutar

```bash
cd demo
mvn clean compile
mvn exec:java -Dexec.mainClass="com.sica.app.SicaApp"
```

---

## 📖 Guía de Uso

Al ejecutar la aplicación se muestra el **Menú Principal**:

```
╔══════════════════════════════════════╗
║        🔐 SISTEMA SICA - MENÚ        ║
║   Control de Acceso - Zona Acme      ║
╠══════════════════════════════════════╣
║  1. 🏢 Gestión de Empresas           ║
║  2. 👥 Gestión de Personas           ║
║  3. 🚨 Gestión de Incidentes         ║
║  4. 📊 Reportes y Estadísticas       ║
║  0. 🚪 Salir del sistema             ║
╚══════════════════════════════════════╝
```

| Módulo | Funciones disponibles |
|---|---|
| **Empresas** | Registrar, listar, actualizar, eliminar |
| **Personas** | Registrar, buscar por documento, actualizar datos, bloquear/habilitar acceso |
| **Incidentes** | Registrar incidente (con bloqueo automático), listar todos, buscar por visita |
| **Reportes** | Personas activas, personas bloqueadas, personas por empresa |

---

## 🔑 Credenciales de Prueba

Estos usuarios son insertados automáticamente por el script `data.sql`:

| Rol | Email | Contraseña | Permisos |
|---|---|---|---|
| **Superusuario** | `admin@sica.com` | `admin123` | Todos los permisos |
| **Supervisor de Seguridad** | `supervisor@sica.com` | `super123` | Generar reportes, registrar incidentes, bloquear personas |
| **Guarda de Seguridad** | `guarda@sica.com` | `guarda123` | Registrar visitas, registrar incidentes |
| **Funcionario de Empresa** | `funcionario@acme.com` | `func123` | Aprobar visitas |

### Datos de Prueba Incluidos

**Empresas:**
- Acme Corp (Contacto: Juan Perez)
- Globex Corporation (Contacto: Hank Scorpio)
- Stark Industries (Contacto: Tony Stark)

**Personas:**
- Carlos Trabajador — Acme Corp — Documento: `12345678`
- Ana Empleada — Globex Corporation — Documento: `87654321`
- Luis Invitado — Acme Corp — Documento: `11223344`
- Maria Visitante — Stark Industries — Documento: `44332211`

---

## 👥 Autores

| Integrante | Rama | Módulos |
|---|---|---|
| **Kevin** | `Devkevin` | BD, Empresas, Personas, Incidentes, Auditoría, Reportes, UI, README |
| **Daniel Gamboa** | `DevGamboa` | Módulo de Usuarios, Control de Acceso, Login RBAC, Concurrencia |

---

> 📅 Proyecto desarrollado como entrega académica | 2026
