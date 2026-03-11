package Recursos_Compartidos;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import Objetos.*;
import Hilos.Visitante;

public class CarreraGomones implements Atraccion {

    private int GI = 7;
    private int GD = 10;
    private int GOMONES_PARA_LARGAR = 10;

    private Semaphore mutex;

    private Semaphore gomonesIndividuales;
    private Semaphore gomonesDobles;
    private Semaphore gomonesListos;

    private Semaphore semaforoEntrada;

    private int participantesCarrera;
    private int participantes;
    private int llegados;

    private CyclicBarrier trenBarrier; // barrera ciclica para el tren

    private BlockingQueue<Bolso> bolsosEnViaje; // colas bloqueantes para los bolsos
    private BlockingQueue<Bolso> bolsosEnFinal;

    private Exchanger<Visitante> exchangerDoble; // exchanger para determinar si hay o no gomon doble

    private AtomicBoolean hayGanador;

    private boolean actividadAbierta;
    private boolean actividadIniciada;

    private ConcurrentHashMap<Visitante, Gomon> visitanteAGomon; // hash concurrente para mapear un visitante a su gomon

    public CarreraGomones() {

        gomonesIndividuales = new Semaphore(GI);
        gomonesDobles = new Semaphore(GD);
        gomonesListos = new Semaphore(0);

        semaforoEntrada = new Semaphore(0);

        participantesCarrera = 0;
        participantes = 0;
        llegados = 0;

        actividadAbierta = false;
        actividadIniciada = false;

        mutex = new Semaphore(1);

        trenBarrier = new CyclicBarrier(15, () -> {
            System.out.println("TREN LLENO, va a la carrera");
        });

        bolsosEnViaje = new LinkedBlockingQueue<>();
        bolsosEnFinal = new LinkedBlockingQueue<>();

        exchangerDoble = new Exchanger<>();

        hayGanador = new AtomicBoolean(false);

        visitanteAGomon = new ConcurrentHashMap<>();

    }

    // metodos que hace el visitante

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

                if (gomonesDobles.tryAcquire()) {

                    try {

                        pareja = exchangerDoble.exchange(visitante, 3, TimeUnit.SECONDS); // si no encuentra pareja en 3
                                                                                          // segundos, se devuelve el
                                                                                          // permiso de gomonesDobles

                        comparacion = visitante.obtenerNombre().compareTo(pareja.obtenerNombre());

                        // se determina quien va a ser el dueño del gomon

                        if (comparacion > 0) {
                            conductor = visitante;
                            pasajero = pareja;
                        } else {
                            conductor = pareja;
                            pasajero = visitante;
                        }

                        mutex.acquire();

                        try {
                            if (!visitanteAGomon.containsKey(conductor)) { // si no esta en el hash, lo crea
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

                    mutex.acquire();

                    if (!actividadIniciada) { // si la actividad no esta iniciada incrementa los participantes y puede
                                              // correr
                        participantes++;
                        puedeCorrer = true;
                    } else {
                        puedeCorrer = false;
                    }

                    mutex.release();

                    if (esConductor) { // solo si maneja un gomon cuenta gomones listos, es decir si es dueño de un
                                       // doble o de un individual
                        gomonesListos.release();
                    }

                    if (puedeCorrer) { // si puede correr pone su bolso y espera

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

                // libera en base a si no es doble o bien es doble y es conductor

                if (!gomon.esDoble()) {
                    gomonesIndividuales.release();
                } else if (gomon.esConductor(visitante)) {
                    gomonesDobles.release();
                }

                if (hayGanador.compareAndSet(false, true)) {

                    // como siempre se dan fichas en nuestro programa, a el/los ganadores se le dan
                    // varias fichas

                    for (int i = 0; i < 3; i++) {
                        visitante.agregarFicha("CG");
                    }

                    pareja = gomon.obtenerPareja(visitante);

                    if (pareja != null) {
                        for (int i = 0; i < 3; i++) {
                            pareja.agregarFicha("CG");
                        }
                        System.out.println("GANARON " + visitante.obtenerNombre() + " Y " + pareja.obtenerNombre());
                    } else {
                        System.out.println("HAY GANADOR " + visitante.obtenerNombre());
                    }
                }

                retirarBolso(visitante);

                mutex.acquire();
                llegados++;

                if (llegados == participantesCarrera) { // si es el ultimo que llega, termina la carrera y se
                                                        // restablecen los valores

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

    // metodos privados del recurso compartido

    private void llegarEnTren() {
        // metodo que maneja la llegada en tren
        try {
            trenBarrier.await(6, TimeUnit.SECONDS);
        } catch (BrokenBarrierException | TimeoutException r) {
            trenBarrier.reset();
            System.out.println("Tren no se lleno o hubo un problema, va en bici");
            llegarEnBici();
        } catch (Exception e) {
            System.out.println("Tren no se lleno o hubo un problema, va en bici");
            llegarEnBici(); // si no se llena, se van en bici
        }
    }

    private void llegarEnBici() {
        // llega en bici
        System.out.println("Llega en bicicleta al inicio");
    }

    private Bolso ponerBolso(Visitante visitante) {
        Bolso bolso = new Bolso(new Random().nextInt(1000), visitante); // se crea un bolso al azar por cada visitante

        try {
            bolsosEnViaje.put(bolso); // se pone en la cola
            System.out.println("Bolso " + bolso.obtenerId() + " enviado del visitante " + visitante.obtenerNombre());
        } catch (InterruptedException e) {
            System.out.println(e);
        }

        return bolso;
    }

    private void retirarBolso(Visitante visitante) {

        boolean encontrado = false;

        while (!encontrado) { // cicla hasta encontrar el bolso
            try {
                Bolso bolso = bolsosEnFinal.take();

                if (bolso.obtenerDuenio() == visitante) { // si lo encuentra, frena y se lo lleva
                    System.out.println(
                            "Visitante " + visitante.obtenerNombre() + " retira su bolso " + bolso.obtenerId());
                    encontrado = true;
                } else {
                    bolsosEnFinal.put(bolso); // si no lo encuentra lo devuelve
                }

            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }

    // metodo que hace el hilo camioneta

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

    // metodo que hace el hilo encargado carrera gomones

    public void habilitarCarrera() {

        try {

            // espera el encargado a que esten todos los gomones listos

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

    // metodos de la interfaz

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