package Recursos_Compartidos;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import Objetos.Atraccion;

public class AutitosChocadores implements Atraccion {

    private CountDownLatch contador;

    private Semaphore mutex;
    private Semaphore salida;

    private boolean iniciado;

    private int[] lugaresAutos;
    private int lugaresDisponibles;

    private boolean actividadAbierta;

    public AutitosChocadores() {

        contador = new CountDownLatch(1);

        mutex = new Semaphore(1);
        salida = new Semaphore(0);

        iniciado = false;

        lugaresAutos = new int[10];
        lugaresDisponibles = 10;

        actividadAbierta = false;

    }

    private int buscarAutoDisponible() {

        int lugar = -1;
        int i = 0;

        while (lugar == -1 && i < lugaresAutos.length) {
            if (lugaresAutos[i] < 2) {
                lugar = i;
            }
            i++;
        }

        return lugar;

    }

    public boolean entrar() {

        boolean gano = false;
        int lugar;

        try {

            mutex.acquire();

            if (!iniciado) {

                lugar = buscarAutoDisponible();

                System.out.println(lugar);

                if (lugar != -1) {

                    if (lugaresAutos[lugar] == 1) {
                        lugaresDisponibles--;
                    }
                    lugaresAutos[lugar]++;

                    if (lugaresDisponibles == 0) {
                        System.out.println("EMPIEZA");
                        iniciado = true;
                        contador.countDown();
                        mutex.release();
                    } else {
                        mutex.release();
                        gano = contador.await(60, TimeUnit.SECONDS);
                        mutex.acquire();

                        if (!gano) {
                            if (lugar != -1) {
                                lugaresAutos[lugar]--;
                                if (lugaresAutos[lugar] == 0) {
                                    lugaresDisponibles++;
                                }
                            }
                        }
                        
                    }

                }

                else {
                    System.out.println("NO LUGAR");
                }

            } else {
                mutex.release();
            }

        }
        catch (Exception e) {
            System.out.println(e);
        }

        return gano;

    }

    public void salir() {

        try {

            salida.acquire();

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void cerrarActividad() {
        salida.release();
    }

    public boolean estaAbierta() {
        return actividadAbierta;
    }

    public String obtenerTipoFichas() {
        return "AC";
    }

}
