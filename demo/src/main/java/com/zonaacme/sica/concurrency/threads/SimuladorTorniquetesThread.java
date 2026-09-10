package com.zonaacme.sica.concurrency.threads;

import com.zonaacme.sica.core.adapters.ControlAccesoService;
import com.zonaacme.sica.core.domain.Persona;
import com.zonaacme.sica.core.domain.PuntoControl;
import com.zonaacme.sica.core.domain.RegistroAcceso;
import com.zonaacme.sica.core.ports.out.PersonaRepositoryPort;
import com.zonaacme.sica.core.ports.out.ZonaRepositoryPort;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 🧵 HILO DEFINIDO POR EL USUARIO: Simulador Concurrente de Paso en Torniquetes.
 *
 * <p><b>Propósito Concurrente:</b></p>
 * Simula lecturas aleatorias continuas de credenciales físicas en los puntos de control
 * en tiempo real para evaluar el rendimiento de concurrencia y refresco de interfaz.
 */
public class SimuladorTorniquetesThread extends Thread {

    private final ControlAccesoService controlAccesoService;
    private final PersonaRepositoryPort personaRepo;
    private final ZonaRepositoryPort zonaRepo;
    private final String tokenSesion;
    private final AtomicBoolean ejecutando = new AtomicBoolean(false);
    private final Random random = new Random();
    private Runnable onPasoSimuladoCallback;

    private int pasosSimulados = 0;

    public SimuladorTorniquetesThread(
            ControlAccesoService controlAccesoService,
            PersonaRepositoryPort personaRepo,
            ZonaRepositoryPort zonaRepo,
            String tokenSesion
    ) {
        super("SICA-SimuladorTorniquetes-Thread");
        this.controlAccesoService = controlAccesoService;
        this.personaRepo = personaRepo;
        this.zonaRepo = zonaRepo;
        this.tokenSesion = tokenSesion;
        setDaemon(true);
    }

    public void setOnPasoSimuladoCallback(Runnable callback) {
        this.onPasoSimuladoCallback = callback;
    }

    public void iniciar() {
        if (!ejecutando.get()) {
            ejecutando.set(true);
            if (!isAlive()) {
                start();
            }
        }
    }

    public void pausar() {
        ejecutando.set(false);
    }

    @Override
    public void run() {
        System.out.println("🚀 [Hilo Usuario] " + getName() + " INICIADO.");

        while (!isInterrupted()) {
            if (ejecutando.get()) {
                try {
                    List<Persona> personas = personaRepo.findAll();
                    List<PuntoControl> puntos = zonaRepo.findAllPuntosControl();

                    if (!personas.isEmpty() && !puntos.isEmpty()) {
                        Persona p = personas.get(random.nextInt(personas.size()));
                        PuntoControl pc = puntos.get(random.nextInt(puntos.size()));

                        RegistroAcceso reg;
                        if (random.nextBoolean()) {
                            reg = controlAccesoService.registrarIngreso(p.getId(), pc.getId(), tokenSesion);
                        } else {
                            reg = controlAccesoService.registrarSalida(p.getId(), pc.getId(), tokenSesion);
                        }
                        pasosSimulados++;

                        System.out.println("🤖 [Simulador Hilos] Paso simulado #" + pasosSimulados +
                                " -> " + p.getNombreCompleto() + " en " + pc.getCodigo() + " [" + reg.getResultado() + "]");

                        if (onPasoSimuladoCallback != null) {
                            onPasoSimuladoCallback.run();
                        }
                    }

                    // Esperar entre 3 y 8 segundos entre pasos simulados
                    Thread.sleep(3000 + random.nextInt(5000));
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    System.err.println("⚠️ [Simulador Hilos] Excepción en simulación: " + e.getMessage());
                }
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        System.out.println("🏁 [Hilo Usuario] " + getName() + " DETENIDO.");
    }

    public int getPasosSimulados() {
        return pasosSimulados;
    }

    public boolean isEjecutando() {
        return ejecutando.get();
    }
}
