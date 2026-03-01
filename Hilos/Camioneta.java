package Hilos;

import Recursos_Compartidos.CarreraGomones;

public class Camioneta implements Runnable {

    private CarreraGomones carrera;

    public Camioneta(CarreraGomones carrera) {
        this.carrera = carrera;
    }

    public void run() {

        while (true) {

            System.out.println("la camioneta espera los bolsos");

            carrera.transportarBolso();

            System.out.println("la camioneta transporta los bolsos");

            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }

        
    }
}