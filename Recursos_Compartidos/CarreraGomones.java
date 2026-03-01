package Recursos_Compartidos;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import Objetos.Atraccion;
import Objetos.Bolso;
import Hilos.Visitante;

public class CarreraGomones implements Atraccion {

    private int GI = 7;
    private int GD = 10;

    private final int gomonesRequeridos = 10;

    private Semaphore mutex;

    private int TREN_CAP = 5;

    private CyclicBarrier trenBarrier;

    private CyclicBarrier largadaBarrier;
    private CyclicBarrier llegadaBarrier;

    private BlockingQueue<Bolso> bolsosEnViaje;
    private BlockingQueue<Bolso> bolsosEnFinal;

    private Exchanger<Visitante> exchangerDoble;

    private AtomicBoolean hayGanador;

    private boolean actividadAbierta;
    private boolean actividadIniciada;

    public CarreraGomones() {

        actividadAbierta = true;
        actividadIniciada = false;

        mutex = new Semaphore(1);

        trenBarrier = new CyclicBarrier(TREN_CAP, () -> {
            System.out.println("TREN LLENO, va a la carrera");
        });

        largadaBarrier = new CyclicBarrier(gomonesRequeridos, () -> {
            System.out.println("COMIENZA LA CARRERA DE GOMONES");
            actividadIniciada = true;
        });

        llegadaBarrier = new CyclicBarrier(gomonesRequeridos, () -> {
            System.out.println("TERMINA LA CARRERA");
            hayGanador.set(false);
            GI = 60;
            GD = 40;
            actividadIniciada = false;

        });

        bolsosEnViaje = new LinkedBlockingQueue<>();
        bolsosEnFinal = new LinkedBlockingQueue<>();

        exchangerDoble = new Exchanger<>();

        hayGanador = new AtomicBoolean(false);
    }

    private void llegarEnTren() {
        try {
            trenBarrier.await(3, TimeUnit.SECONDS);
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

    private boolean encontrarGomonDoble() {

        boolean exito = false;
        Visitante visitante = (Visitante) Thread.currentThread();

        if (GD != 0) {
            GD--;

            try {
                mutex.release();
                exchangerDoble.exchange(visitante, 3, TimeUnit.SECONDS);
                mutex.acquire();
                exito = true;
            } catch (Exception e) {
                System.out.println(e);
            }

        }

        return exito;

    }

    private boolean encontrarGomonIndividual() {

        boolean exito = false;

        if (GI != 0) {
            GI--;
            exito = true;
        }

        return exito;

    }

    public boolean entrar() {

        boolean exito = false;
        boolean encontroGomon = false;
        Visitante visitante = null;
        Bolso bolso = null;

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

                    mutex.acquire();

                    if (GD > 0 || GI > 0) {

                        if (new Random().nextBoolean()) {
                            encontroGomon = encontrarGomonDoble();
                            if (!encontroGomon) {
                                encontroGomon = encontrarGomonIndividual();
                            }
                        } else {
                            encontroGomon = encontrarGomonIndividual();
                            if (!encontroGomon) {
                                encontroGomon = encontrarGomonDoble();
                            }
                        }

                    }

                    mutex.release();

                    if (encontroGomon && !actividadIniciada) {

                        bolso = ponerBolso(visitante);

                        System.out.println("Espera en barrera ...");

                        largadaBarrier.await(30, TimeUnit.SECONDS);
                        exito = true;

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

        try {

            visitante = (Visitante) Thread.currentThread();

            if (hayGanador.compareAndSet(false, true)) {
                for (int i = 0; i < 3; i++) {
                    visitante.agregarFicha("CG");
                }
                System.out.println("HAY GANADOR! " + visitante.obtenerNombre());
            }

            llegadaBarrier.await();

            retirarBolso(visitante);

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
}