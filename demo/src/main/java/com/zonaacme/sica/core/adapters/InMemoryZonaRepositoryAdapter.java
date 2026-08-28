package com.zonaacme.sica.core.adapters;

import com.zonaacme.sica.core.domain.PuntoControl;
import com.zonaacme.sica.core.domain.Zona;
import com.zonaacme.sica.core.ports.out.ZonaRepositoryPort;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Adaptador secundario para almacenamiento de Zonas y Puntos de Control en memoria.
 *
 * <p><b>Arquitectura Hexagonal & Principios SOLID:</b></p>
 * <ul>
 *   <li><b>LSP (Liskov Substitution Principle):</b> Cumple el contrato {@link ZonaRepositoryPort} sin ataduras a tecnología.</li>
 *   <li><b>Concurrencia:</b> Soporta accesos seguros multi-hilo con {@link ConcurrentHashMap}.</li>
 * </ul>
 */
public class InMemoryZonaRepositoryAdapter implements ZonaRepositoryPort {

    private final Map<String, Zona> zonasPorId = new ConcurrentHashMap<>();
    private final Map<String, String> idPorCodigoZona = new ConcurrentHashMap<>();

    private final Map<String, PuntoControl> puntosPorId = new ConcurrentHashMap<>();
    private final Map<String, String> idPorCodigoPunto = new ConcurrentHashMap<>();

    public InMemoryZonaRepositoryAdapter() {
        inicializarSemilla();
    }

    private void inicializarSemilla() {
        Zona recepcion = Zona.nueva("ZONA_RECEPCION", "Recepción Principal", "Área de bienvenida y registro", 50, LocalTime.of(6, 0), LocalTime.of(22, 0), false);
        Zona oficinas = Zona.nueva("ZONA_OFICINAS", "Oficinas Administrativas", "Pisos 1 y 2 Edificio Central", 80, LocalTime.of(7, 0), LocalTime.of(19, 0), false);
        Zona datacenter = Zona.nueva("ZONA_DATACENTER", "Centro de Cómputo y Servidores", "Área crítica de alta seguridad", 5, LocalTime.of(8, 0), LocalTime.of(18, 0), true);

        saveZona(recepcion);
        saveZona(oficinas);
        saveZona(datacenter);

        PuntoControl torniquete1 = PuntoControl.nuevo("PC_TORN_01", "Torniquete Peatonal 1", recepcion.getId(), PuntoControl.TipoPunto.TORNIQUETE);
        PuntoControl puertaOficinas = PuntoControl.nuevo("PC_PUERTA_OFI", "Puerta Acceso Oficinas", oficinas.getId(), PuntoControl.TipoPunto.PUERTA_AUTOMATICA);
        PuntoControl barreraVehicular = PuntoControl.nuevo("PC_BARRERA_01", "Barrera Parqueadero Visitantes", recepcion.getId(), PuntoControl.TipoPunto.VEHICULAR);
        PuntoControl puertaDataCenter = PuntoControl.nuevo("PC_PUERTA_DC", "Puerta Blindada Data Center", datacenter.getId(), PuntoControl.TipoPunto.PUERTA_AUTOMATICA);

        savePuntoControl(torniquete1);
        savePuntoControl(puertaOficinas);
        savePuntoControl(barreraVehicular);
        savePuntoControl(puertaDataCenter);
    }

    @Override
    public void saveZona(Zona zona) {
        Objects.requireNonNull(zona, "La zona no puede ser nula");
        zonasPorId.put(zona.getId(), zona);
        idPorCodigoZona.put(zona.getCodigo().toUpperCase(), zona.getId());
    }

    @Override
    public Optional<Zona> findZonaById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(zonasPorId.get(id));
    }

    @Override
    public Optional<Zona> findZonaByCodigo(String codigo) {
        if (codigo == null) return Optional.empty();
        String id = idPorCodigoZona.get(codigo.trim().toUpperCase());
        if (id == null) return Optional.empty();
        return Optional.ofNullable(zonasPorId.get(id));
    }

    @Override
    public List<Zona> findAllZonas() {
        return Collections.unmodifiableList(new ArrayList<>(zonasPorId.values()));
    }

    @Override
    public void savePuntoControl(PuntoControl puntoControl) {
        Objects.requireNonNull(puntoControl, "El punto de control no puede ser nulo");
        puntosPorId.put(puntoControl.getId(), puntoControl);
        idPorCodigoPunto.put(puntoControl.getCodigo().toUpperCase(), puntoControl.getId());
    }

    @Override
    public Optional<PuntoControl> findPuntoControlById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(puntosPorId.get(id));
    }

    @Override
    public Optional<PuntoControl> findPuntoControlByCodigo(String codigo) {
        if (codigo == null) return Optional.empty();
        String id = idPorCodigoPunto.get(codigo.trim().toUpperCase());
        if (id == null) return Optional.empty();
        return Optional.ofNullable(puntosPorId.get(id));
    }

    @Override
    public List<PuntoControl> findAllPuntosControl() {
        return Collections.unmodifiableList(new ArrayList<>(puntosPorId.values()));
    }

    @Override
    public List<PuntoControl> findPuntosControlByZonaId(String zonaId) {
        if (zonaId == null) return Collections.emptyList();
        return puntosPorId.values().stream()
                .filter(p -> p.getZonaId().equals(zonaId))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    public void reset() {
        zonasPorId.clear();
        idPorCodigoZona.clear();
        puntosPorId.clear();
        idPorCodigoPunto.clear();
    }
}
