package Hilos;

import Recursos_Compartidos.AutitosChocadores;

public class EncargadoAutitos implements Runnable {

    private AutitosChocadores autitosChocadores;

    public EncargadoAutitos(AutitosChocadores autitosChocadores) {

        this.autitosChocadores = autitosChocadores;

    }

    // encargado de iniciar los autitos chocadores

    public void run() {

        try {

            while (true) {

                System.out.println("Espera a que se llenen los autitos chocadores .......");

                autitosChocadores.esperarLlenarse(); // espera a que se llenen los autitos chocadores

                System.out.println("Comienzan los autitos chocadores .....");

                Thread.sleep(7000);

                autitosChocadores.salir();

            }

        } catch (Exception e) {
            System.out.println(e);
        }

    }

}
