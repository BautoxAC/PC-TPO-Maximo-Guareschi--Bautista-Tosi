package Hilos;

import Recursos_Compartidos.Teatro;

public class EncargadoTeatro implements Runnable {
    private Teatro teatro;

    public EncargadoTeatro(Teatro teatro) {
        this.teatro = teatro;
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("Se habilita la entrada al teatro");
                Thread.sleep(4000);
                teatro.ponerEncurso();
                System.out.println("Esta en curso la obra de teatro");
                Thread.sleep(6000);
                System.out.println("Se termino la obra de teatro");
                teatro.sacarEnCurso();
                Thread.sleep(4000);
            } catch (Exception e) {
                // TODO: handle exception
            }

        }
    }

}
