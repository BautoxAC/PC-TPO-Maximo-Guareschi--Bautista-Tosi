package Recursos_Compartidos;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import Objetos.Atraccion;

public class RealidadVirtual implements Atraccion {
    private BlockingQueue<String>[] equipo;
    private int[] capacidades = { 5, 10, 5 };
    private int cantEsperando;
    private Semaphore mutex;
    private Semaphore encargado;
    private Semaphore revisar;

    public RealidadVirtual() {
        // Dentro de estas BlockingQueue la primera(en posicion 0) es para los visores,
        // la segunda (en posicion 1) es para las manoplas y la tercera (en posicion 2)
        // para las bases
        cantEsperando = 0;

        mutex = new Semaphore(1);
        equipo = new BlockingQueue[3];
        for (int i = 0; i < equipo.length; i++) {
            equipo[i] = new ArrayBlockingQueue<String>(capacidades[i]);
        }
        encargado = new Semaphore(0);
        revisar = new Semaphore(0);
    }

    @Override
    public boolean entrar() {
        boolean entro = false;
        try {
            mutex.acquire();
            if (cantEsperando < 4) {
                cantEsperando++;
                mutex.release();
                String Visor = equipo[0].poll(20, TimeUnit.SECONDS);
                if (Visor != null) {
                    String manopla = equipo[1].poll(20, TimeUnit.SECONDS);
                    String manopla2 = equipo[1].poll(20, TimeUnit.SECONDS);
                    if (manopla != null && manopla2 != null) {
                        String base = equipo[2].poll(20, TimeUnit.SECONDS);
                        if (base != null) {
                            entro = true;
                            this.esperarRevisar();
                        } else {
                            equipo[0].put("Visor");
                            if ((manopla != null && manopla2 == null) || (manopla == null && manopla2 != null)) {
                                equipo[1].put("manopla");
                            }
                        }
                    } else {
                        equipo[0].put("Visor");
                    }

                }
                mutex.acquire();
                cantEsperando--;
                mutex.release();
            }

        } catch (Exception e) {
            // TODO: handle exception
        }
        return entro;
    }

    private void esperarRevisar() {
        try {
            encargado.release();
            revisar.acquire();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    @Override
    public void salir() {
        try {
            equipo[0].put("Visor");
            equipo[1].put("manopla");
            equipo[1].put("manopla2");
            equipo[2].put("base");

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    @Override
    public String obtenerTipoFichas() {
        return "RV";
    }

    @Override
    public void cerrarActividad() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'cerrarActividad'");
    }

    @Override
    public boolean estaAbierta() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'estaAbierta'");
    }

    public void esperarListo() {
        try {
            encargado.acquire();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void liberaEntrada() {
        try {
            revisar.release();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

}
