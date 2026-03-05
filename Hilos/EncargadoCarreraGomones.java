package Hilos;

import Recursos_Compartidos.CarreraGomones;

public class EncargadoCarreraGomones implements Runnable {

    private CarreraGomones carreraGomones;

    public EncargadoCarreraGomones(CarreraGomones carreraGomones) {

        this.carreraGomones = carreraGomones;

    }

    // encargado de iniciar la carrera de gomones

    public void run() {

        try {

            while (true) {

                System.out.println("Espera a que hayan gomones para la carrera .......");

                carreraGomones.habilitarCarrera(); // se bloquea esperando a que esten todos los gomones

                System.out.println("COMIENZA LA CARRERA DE GOMONES .....");

            }

        } catch (Exception e) {
            System.out.println(e);
        }

    }

}
