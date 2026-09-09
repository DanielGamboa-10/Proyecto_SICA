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
        Zona lobby = Zona.nueva("ZONA_LOBBY", "Lobby Principal y Recepción", "Área de recepción general y torniquetes peatonales", 200, LocalTime.of(5, 0), LocalTime.of(23, 0), false);
        Zona techHub = Zona.nueva("ZONA_TECH_HUB", "Torre de Innovación & Tech Hub", "Pisos corporativos de oficinas para empresas de tecnología", 350, LocalTime.of(6, 0), LocalTime.of(21, 0), false);
        Zona bioLabs = Zona.nueva("ZONA_BIOLABS", "Laboratorios BioGen & Nanotecnología", "Área biocontenida de investigación farmacéutica", 80, LocalTime.of(7, 0), LocalTime.of(19, 0), true);
        Zona datacenter = Zona.nueva("ZONA_DATACENTER", "Centro de Cómputo Principal (Tier IV)", "Sala de servidores críticos y telecomunicaciones", 15, LocalTime.of(0, 0), LocalTime.of(23, 59), true);
        Zona parking = Zona.nueva("ZONA_PARKING", "Estacionamiento Subterráneo S1/S2", "Bahías vehiculares para funcionarios y visitantes", 150, LocalTime.of(5, 30), LocalTime.of(22, 30), false);

        saveZona(lobby);
        saveZona(techHub);
        saveZona(bioLabs);
        saveZona(datacenter);
        saveZona(parking);

        PuntoControl torniquete1 = PuntoControl.nuevo("PC_TORN_01", "Torniquete Peatonal 1 (Entrada Norte)", lobby.getId(), PuntoControl.TipoPunto.TORNIQUETE);
        PuntoControl torniquete2 = PuntoControl.nuevo("PC_TORN_02", "Torniquete Peatonal 2 (Entrada Sur)", lobby.getId(), PuntoControl.TipoPunto.TORNIQUETE);
        PuntoControl puertaTech = PuntoControl.nuevo("PC_PUERTA_TECH", "Puerta Acceso Torre Tech Hub", techHub.getId(), PuntoControl.TipoPunto.PUERTA_AUTOMATICA);
        PuntoControl esclusaBio = PuntoControl.nuevo("PC_BIO_ESCLUSA", "Esclusa Bioseguridad BioLabs", bioLabs.getId(), PuntoControl.TipoPunto.PUERTA_AUTOMATICA);
        PuntoControl esclusaDc = PuntoControl.nuevo("PC_DC_RESTRINGIDO", "Esclusa de Seguridad Datacenter", datacenter.getId(), PuntoControl.TipoPunto.PUERTA_AUTOMATICA);
        PuntoControl talanquera = PuntoControl.nuevo("PC_TALANQUERA_VEH", "Talanquera Vehicular Acceso S1", parking.getId(), PuntoControl.TipoPunto.VEHICULAR);

        savePuntoControl(torniquete1);
        savePuntoControl(torniquete2);
        savePuntoControl(puertaTech);
        savePuntoControl(esclusaBio);
        savePuntoControl(esclusaDc);
        savePuntoControl(talanquera);
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
