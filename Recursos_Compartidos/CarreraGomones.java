package Recursos_Compartidos;

import java.util.ArrayList;
import java.util.List;
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

    private Semaphore bolsosListos;

    private Semaphore mutexBarrera;
    private Semaphore semaforoEntrada;
    private Semaphore mutexLista;

    private int participantesCarrera;
    private int participantes;
    private int llegados;

    private CyclicBarrier trenBarrier; // barrera ciclica para el tren

    private List<Bolso> bolsosInicio;
    private List<Bolso> bolsosEnCamioneta;
    private ConcurrentHashMap<String, Bolso> bolsosFinal;

    private Exchanger<Visitante> exchangerDoble; // exchanger para determinar si hay o no gomon doble

    private AtomicBoolean hayGanador;

    private boolean actividadAbierta;
    private boolean actividadIniciada;

    private ConcurrentHashMap<Visitante, Gomon> visitanteAGomon; // hash concurrente para mapear un visitante a su gomon

    public CarreraGomones() {

        gomonesIndividuales = new Semaphore(GI);
        gomonesDobles = new Semaphore(GD);
        gomonesListos = new Semaphore(0);

        bolsosListos = new Semaphore(0);

        semaforoEntrada = new Semaphore(0);

        participantesCarrera = 0;
        participantes = 0;
        llegados = 0;

        actividadAbierta = false;
        actividadIniciada = false;

        mutex = new Semaphore(1);
        mutexBarrera = new Semaphore(1);
        mutexLista = new Semaphore(1);

        trenBarrier = new CyclicBarrier(15, () -> {
            System.out.println("TREN LLENO, va a la carrera");
        });

        bolsosInicio = new ArrayList<>();
        bolsosEnCamioneta = new ArrayList<>();
        bolsosFinal = new ConcurrentHashMap<>();

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
        Gomon gomonAgarrado;
        Visitante pareja;
        int comparacion;
        Visitante conductor;
        Visitante pasajero;

        try {
            mutex.acquire();

        } catch (Exception e) {
            e.printStackTrace();

        }

        if (actividadAbierta && !actividadIniciada) {

            System.out.println("Permisos de gomonesListos :" + gomonesListos.availablePermits());
                    System.out.println("Permisos de gomonesDobles :" + gomonesDobles.availablePermits());
                    System.out.println("Permisos de gomonesIndividuales :" + gomonesIndividuales.availablePermits());


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

                    } catch (TimeoutException e) {
                        gomonesDobles.release();
                    } catch (Exception r) {
                        System.out.println(r);
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

                    
                    if (puedeCorrer) { // si puede correr pone su bolso y espera

                        if (esConductor) { // solo si maneja un gomon cuenta gomones listos, es decir si es dueño de un
                            // doble o de un individual
                            gomonesListos.release();
                        }

                        bolso = ponerBolso(visitante);

                        System.out.println("Espera a iniciar ...");

                        if (!semaforoEntrada.tryAcquire(15, TimeUnit.SECONDS)) {
                            if (esConductor) {
                                gomonesListos.acquire();
                            }

                            System.out.println("se sale por EL TIEMPO");

                            gomonAgarrado = visitanteAGomon.remove(visitante);

                            if (gomonAgarrado != null) {

                                pareja = gomonAgarrado.obtenerPareja(visitante);

                                if (pareja != null) {
                                    visitanteAGomon.remove(pareja);
                                }

                                if (gomonAgarrado.esDoble()) {
                                    gomonesDobles.release();
                                } else {
                                    gomonesIndividuales.release();
                                }
                            }

                            mutex.acquire();
                            participantes--;
                            mutex.release();
                            retirarBolsoInicio(visitante);
                        }

                        exito = true;
                    }

                }

            } catch (Exception e) {
                System.out.println("error en la carrera");

                if (visitante != null && bolso != null) {
                    retirarBolsoInicio(visitante);
                }
                e.printStackTrace();
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

                // if (!gomon.esDoble()) {
                // gomonesIndividuales.release();
                // } else if (gomon.esConductor(visitante)) {
                // gomonesDobles.release(2);
                // gomon.avisarPasajero();
                // }

                if (!gomon.esDoble()) {
                    gomonesIndividuales.release();
                } else {
                    gomonesDobles.release();
                    if (gomon.esConductor(visitante)) {
                        gomon.avisarPasajero();
                    }
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

                retirarBolsoFin(visitante);

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
            e.printStackTrace();
        }
    }

    public boolean verificarPasajero() {

        boolean espera = true;
        Visitante visitante = (Visitante) Thread.currentThread();
        Gomon gomon = visitanteAGomon.get(visitante);

        if (gomon != null) {

            if (gomon.esDoble() && !gomon.esConductor(visitante)) {
                gomon.esperarConductor();
                espera = false;
            }

        }

        return espera;

    }

    // metodos privados del recurso compartido

    private void llegarEnTren() {
        // metodo que maneja la llegada en tren
        try {
            trenBarrier.await(6, TimeUnit.SECONDS);
        } catch (BrokenBarrierException | TimeoutException r) {
            try {
                mutexBarrera.acquire();
                if (trenBarrier.isBroken()) {
                    trenBarrier.reset();
                }
                System.out.println("Tren no se lleno o hubo un problema, va en bici");
                llegarEnBici();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mutexBarrera.release();

            }

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
        Bolso bolso = new Bolso(visitante.obtenerNombre(), new Random().nextInt(60)); // se crea un bolso al azar por
                                                                                      // cada visitante

        try {
            mutexLista.acquire();
            bolsosInicio.add(bolso); // se pone en la lista
            mutexLista.release();
            System.out.println("Se deja el bolso del visitante " + bolso.obtenerDuenio() + " con "
                    + bolso.obtenerCosas() + " cosas");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return bolso;
    }

    private void retirarBolsoInicio(Visitante visitante) {

        Bolso encontrado = null;
        int i = 0;

        try {
            mutexLista.acquire();

            while (encontrado == null && i < bolsosInicio.size()) { // cicla hasta encontrar el bolso

                Bolso bolso = bolsosInicio.get(i);

                if (bolso.obtenerDuenio().equals(visitante.obtenerNombre())) { // si lo encuentra, frena y se lo
                                                                               // lleva
                    System.out.println(
                            "Visitante " + visitante.obtenerNombre()
                                    + " retira su bolso con la siguiente cantidad de cosas: "
                                    + bolso.obtenerCosas());
                    encontrado = bolso;
                }

                i++;

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mutexLista.release();
        }

        if (encontrado != null) {
            bolsosInicio.remove(encontrado);
            System.out.println("El visitante " + visitante.obtenerNombre() + " retiro su bolso con "
                    + encontrado.obtenerCosas() + " cosas");
        }

    }

    private void retirarBolsoFin(Visitante visitante) {

        Bolso encontrado = null;

        try {

            mutexLista.acquire();

            encontrado = bolsosFinal.remove(visitante.obtenerNombre());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mutexLista.release();
        }

        if (encontrado != null) {

            System.out.println("El visitante " + visitante.obtenerNombre() + " retiro su bolso con "
                    + encontrado.obtenerCosas() + " cosas");

        }

    }

    // metodos que hace el hilo camioneta

    public void esperarBolsos() {

        try {

            bolsosListos.acquire();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void subirBolsos() {

        try {

            mutexLista.acquire();

            for (Bolso bolso : bolsosInicio) {
                bolsosEnCamioneta.add(bolso);
            }

            bolsosInicio.clear();

            mutexLista.release();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void dejarBolsos() {

        try {

            mutexLista.acquire();

            for (Bolso bolso : bolsosEnCamioneta) {
                bolsosFinal.put(bolso.obtenerDuenio(), bolso);
            }

            bolsosEnCamioneta.clear();

            mutexLista.release();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // metodo que hace el hilo encargado carrera gomones

    public void habilitarCarrera() {

        try {

            // espera el encargado a que esten todos los gomones listos

            gomonesListos.acquire(GOMONES_PARA_LARGAR);

            bolsosListos.release();

            mutex.acquire();
            actividadIniciada = true;
            participantesCarrera = participantes;
            mutex.release();

            System.out.println("EMPIEZA LA CARRERA");

            semaforoEntrada.release(participantesCarrera);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // metodos de la interfaz

    public void cerrarActividad() {
        try {
            mutex.acquire();
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
        actividadAbierta = true;
        mutex.release();
    }

    public boolean preparar() {
        return verificarPasajero();
    }

}