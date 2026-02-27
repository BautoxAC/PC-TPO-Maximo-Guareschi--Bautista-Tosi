package Hilos;

import Recursos_Compartidos.AutitosChocadores;

public class EncargadoAutitos implements Runnable {

    private AutitosChocadores autitosChocadores;

    public EncargadoAutitos(AutitosChocadores autitosChocadores) {

        this.autitosChocadores = autitosChocadores;

    }

    public void run() {

        try {

            while (true) {

                System.out.println("Espera a que se llenen los autitos chocadores .......");

                autitosChocadores.esperarLlenarse();

                System.out.println("Comienzan los autitos chocadores .....");

                Thread.sleep(13000);

                autitosChocadores.salir();

            }

        } catch (Exception e) {
            System.out.println(e);
        }

    }

}
