package Recursos_Compartidos;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import Objetos.Atraccion;

public class RealidadVirtual implements Atraccion {
    private BlockingQueue<String>[] equipo;
    private int[] capacidades = { 5, 10, 5 };
    private int cantEsperando;
    private Semaphore mutex;
    private Semaphore encargado;
    private Semaphore revisar;
    private AtomicBoolean actividadAbierta;

    public RealidadVirtual() {
        // Dentro de esta BlockingQueue la primera(en posicion 0) es para los visores,
        // la segunda (en posicion 1) es para las manoplas y la tercera (en posicion 2)
        // para las bases
        cantEsperando = 0;
        actividadAbierta = new AtomicBoolean(false);
        mutex = new Semaphore(1);
        equipo = new BlockingQueue[3];
        for (int i = 0; i < equipo.length; i++) {
            equipo[i] = new ArrayBlockingQueue<String>(capacidades[i]);
            this.ponerPartes(i);
        }
        encargado = new Semaphore(0);
        revisar = new Semaphore(0);
    }

    @Override
    public boolean entrar() {
        boolean entro = false;
        try {
            mutex.acquire();

            if (cantEsperando < 4 && estaAbierta()) {

                cantEsperando++;
                mutex.release();

                // Intenta sacar un visor
                String Visor = equipo[0].poll(2, TimeUnit.SECONDS);
                System.out.println("HOLAAAAAAAAAAAAAAAAAAAAAAAAA");
                System.out.println(Visor);
                if (Visor != null) {
                    // Intenta sacar dos manoplas
                    String manopla = equipo[1].poll(2, TimeUnit.SECONDS);
                    String manopla2 = equipo[1].poll(2, TimeUnit.SECONDS);

                    if (manopla != null && manopla2 != null) {
                        // Intenta sacar una base
                        String base = equipo[2].poll(2, TimeUnit.SECONDS);

                        if (base != null) {
                            // Pudo sacar todo y espera ser revisado
                            entro = true;
                            this.esperarRevisar();

                        } else {
                            // No pudo y devuelve lo que saco
                            equipo[0].put("Visor");
                            equipo[1].put("manopla1");
                            equipo[1].put("manopla2");

                        }
                    } else {
                        equipo[0].put("Visor");
                        // pudo haber agarrado alguno de los dos y devuelve uno solo
                        if ((manopla != null && manopla2 == null) || (manopla == null && manopla2 != null)) {
                            equipo[1].put("manopla");
                        }
                    }

                }

                mutex.acquire();
                cantEsperando--;
                mutex.release();

            } else {

                mutex.release();

            }

        } catch (Exception e) {
            // TODO: handle exception
            try {
                mutex.acquire();
                cantEsperando--;
                mutex.release();
            } catch (Exception e2) {
                // TODO: handle exception
            }

        }
        return entro;
    }

    private void esperarRevisar() {
        try {
            encargado.release();
            revisar.acquire();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void salir() {
        try {
            // Pone todo de nuevo
            equipo[0].put("Visor");
            equipo[1].put("manopla");
            equipo[1].put("manopla2");
            equipo[2].put("base");

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public String obtenerTipoFichas() {
        return "RV";
    }

    public void cerrarActividad() {
        try {
            actividadAbierta.set(false);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public boolean estaAbierta() {
        return actividadAbierta.get();
    }

    public void esperarListo() {
        try {
            encargado.acquire();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void liberaEntrada() {
        try {
            revisar.release();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
 // Se cargan las partes en la RV
    public void ponerPartes(int i) {
        try {
            for (int j = 0; j < capacidades[i]; j++) {
                if (i == 0) {

                    equipo[i].put("Visor" + j);
                } else if (i == 1) {
                    equipo[i].put("Manopla" + j);
                } else {
                    equipo[i].put("Base" + j);
                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void abrirActividad() {
        try {
            actividadAbierta.set(true);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
