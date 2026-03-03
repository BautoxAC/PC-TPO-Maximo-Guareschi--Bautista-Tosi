package Recursos_Compartidos;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import Objetos.*;
import Hilos.Visitante;

public class CarreraGomones implements Atraccion {

    private final int GI = 7;
    private final int GD = 10;
    private final int GOMONES_PARA_LARGAR = 10;

    private Semaphore mutex;
    private Semaphore mutexPareja;

    private Semaphore gomonesIndividuales;
    private Semaphore gomonesDobles;
    private Semaphore gomonesListos;

    private Semaphore semaforoEntrada;

    private int participantesCarrera;
    private int participantes;
    private int llegados;

    private CyclicBarrier trenBarrier;

    private BlockingQueue<Bolso> bolsosEnViaje;
    private BlockingQueue<Bolso> bolsosEnFinal;

    private Exchanger<Visitante> exchangerDoble;

    private AtomicBoolean hayGanador;

    private boolean actividadAbierta;
    private boolean actividadIniciada;

    private ConcurrentHashMap<Visitante, Gomon> visitanteAGomon;

    public CarreraGomones() {

        gomonesIndividuales = new Semaphore(GI);
        gomonesDobles = new Semaphore(GD);
        gomonesListos = new Semaphore(0);

        semaforoEntrada = new Semaphore(0);

        participantesCarrera = 0;
        participantes = 0;
        llegados = 0;

        actividadAbierta = true;
        actividadIniciada = false;

        mutex = new Semaphore(1);
        mutexPareja = new Semaphore(1);

        trenBarrier = new CyclicBarrier(15, () -> {
            System.out.println("TREN LLENO, va a la carrera");
        });

        bolsosEnViaje = new LinkedBlockingQueue<>();
        bolsosEnFinal = new LinkedBlockingQueue<>();

        exchangerDoble = new Exchanger<>();

        hayGanador = new AtomicBoolean(false);

        visitanteAGomon = new ConcurrentHashMap<>();

    }

    private void llegarEnTren() {
        try {
            trenBarrier.await(6, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("Tren no se lleno o hubo un problema, va en bici");
            llegarEnBici();
        }
    }

    private void llegarEnBici() {
        System.out.println("Llega en bicicleta al inicio");
    }

    private Bolso ponerBolso(Visitante visitante) {
        Bolso bolso = new Bolso(new Random().nextInt(1000), visitante);

        try {
            bolsosEnViaje.put(bolso);
            System.out.println("Bolso " + bolso.obtenerId() + " enviado del visitante " + visitante.obtenerNombre());
        } catch (InterruptedException e) {
            System.out.println(e);
        }

        return bolso;
    }

    public void transportarBolso() {
        Bolso bolso;
        try {
            bolso = bolsosEnViaje.take();
            bolsosEnFinal.put(bolso);
            System.out.println("Bolso " + bolso.obtenerId() + " llego al final");
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }

    private void retirarBolso(Visitante visitante) {

        boolean encontrado = false;

        while (!encontrado) {
            try {
                Bolso bolso = bolsosEnFinal.take();

                if (bolso.obtenerDuenio() == visitante) {
                    System.out.println(
                            "Visitante " + visitante.obtenerNombre() + " retira su bolso " + bolso.obtenerId());
                    encontrado = true;
                } else {
                    bolsosEnFinal.put(bolso);
                }

            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }

    public void habilitarCarrera() {

        try {

            gomonesListos.acquire(GOMONES_PARA_LARGAR);

            mutex.acquire();
            actividadIniciada = true;
            participantesCarrera = participantes;
            mutex.release();

            System.out.println("EMPIEZA LA CARRERA");

            semaforoEntrada.release(participantesCarrera);

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public boolean entrar() {

        boolean exito = false;
        Visitante visitante = null;
        Bolso bolso = null;
        boolean esConductor = false;
        boolean puedeCorrer;
        Gomon gomon;
        Visitante pareja;
        int comparacion;
        Visitante conductor;
        Visitante pasajero;

        try {
            mutex.acquire();

        } catch (Exception e) {
            System.out.println(e);

        }

        if (actividadAbierta && !actividadIniciada) {

            mutex.release();

            try {
                if (new Random().nextBoolean()) {
                    llegarEnTren();
                } else {
                    llegarEnBici();
                }

                visitante = (Visitante) Thread.currentThread();

                
                System.out.println(gomonesDobles.availablePermits());
                System.out.println(gomonesIndividuales.availablePermits());
                System.out.println(gomonesListos.availablePermits());

                if (gomonesDobles.tryAcquire()) {

                    try {
                        
                        pareja = exchangerDoble.exchange(visitante);

                        comparacion = visitante.obtenerNombre().compareTo(pareja.obtenerNombre());

                        if (comparacion > 0) {
                            conductor = visitante;
                            pasajero = pareja;
                        } else {
                            conductor = pareja;
                            pasajero = visitante;
                        }

                        mutex.acquire();

                        try {
                            if (!visitanteAGomon.containsKey(conductor)) {
                                gomon = new Gomon(conductor, pasajero);
                                visitanteAGomon.put(conductor, gomon);
                                visitanteAGomon.put(pasajero, gomon);
                            }
                        } finally {
                            mutex.release();
                        }

                        esConductor = visitante == conductor;

                    } catch (Exception r) {
                        System.out.println(r);
                        gomonesDobles.release();
                    }

                } else if (gomonesIndividuales.tryAcquire()) {
                    gomon = new Gomon(visitante);
                    visitanteAGomon.put(visitante, gomon);
                    esConductor = true;
                } else {
                    System.out.println("No hay gomones disponibles");
                }

                

                if (visitanteAGomon.containsKey(visitante)) {

                    System.out.println("ESTA ");

                    mutex.acquire();

                    if (!actividadIniciada) {
                        participantes++;
                        puedeCorrer = true;
                    } else {
                        puedeCorrer = false;
                    }

                    System.out.println(participantes);

                    mutex.release();

                    if (esConductor) {
                        System.out.println("libera");
                        gomonesListos.release();
                    }

                    if (puedeCorrer) {

                        bolso = ponerBolso(visitante);

                        System.out.println("Espera a iniciar ...");

                        semaforoEntrada.acquire();

                        exito = true;
                    }

                }

            } catch (Exception e) {
                System.out.println("No se completó la carrera, se retira");

                if (visitante != null && bolso != null) {
                    retirarBolso(visitante);
                }
                System.out.println(e);
            }

        } else {
            mutex.release();
        }

        return exito;

    }

    public void salir() {

        Visitante visitante;
        Visitante pareja;
        Gomon gomon;

        try {

            visitante = (Visitante) Thread.currentThread();

            gomon = visitanteAGomon.get(visitante);

            if (visitante != null && gomon != null) {

                if (!gomon.esDoble()) {
                    gomonesIndividuales.release();
                } else if (gomon.esConductor(visitante)) {
                    gomonesDobles.release();
                }

                if (hayGanador.compareAndSet(false, true)) {

                    visitante.agregarFicha("CG");

                    pareja = gomon.obtenerPareja(visitante);

                    if (pareja != null) {
                        pareja.agregarFicha("CG");
                        System.out.println("GANARON " + visitante.obtenerNombre() + " Y " + pareja.obtenerNombre());
                    } else {
                        System.out.println("HAY GANADOR " + visitante.obtenerNombre());
                    }
                }

                retirarBolso(visitante);

                mutex.acquire();
                llegados++;

                if (llegados == participantesCarrera) {

                    System.out.println("TERMINA LA CARRERA");

                    llegados = 0;
                    participantes = 0;
                    participantesCarrera = 0;
                    actividadIniciada = false;
                    hayGanador.set(false);
                    visitanteAGomon.clear();

                }

                mutex.release();

            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void cerrarActividad() {
        try {
            mutex.acquire();
        } catch (Exception e) {
            System.out.println(e);
        }
        actividadAbierta = false;
        mutex.release();
    }

    public boolean estaAbierta() {
        boolean abierta = false;
        try {
            mutex.acquire();
            abierta = actividadAbierta;
            mutex.release();
        } catch (Exception e) {
            System.out.println(e);
        }
        return abierta;
    }

    public String obtenerTipoFichas() {
        return "CG";
    }

    @Override
    public void abrirActividad() {
         try {
            mutex.acquire();
        } catch (Exception e) {
            System.out.println(e);
        }
        actividadAbierta = true;
        mutex.release();
    }
}