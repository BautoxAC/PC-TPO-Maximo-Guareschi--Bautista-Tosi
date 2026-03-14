package Hilos;

import Recursos_Compartidos.CarreraGomones;

public class Camioneta implements Runnable {

    // hilo para carrera de gomones, controla los bolsos

    private CarreraGomones carrera;

    public Camioneta(CarreraGomones carrera) {
        this.carrera = carrera;
    }

    public void run() {

        while (true) {

            System.out.println("la camioneta espera los bolsos");

            carrera.esperarBolsos();

            System.out.println("la camioneta sube los bolsos");

            carrera.subirBolsos();

            System.out.println("la camioneta termino de subir los bolsos, va hacia el final");

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                System.out.println(e);
            }

            carrera.dejarBolsos();

        }

        
    }
}