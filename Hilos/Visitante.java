package Hilos;

import java.util.Random;

import Objetos.*;

public class Visitante implements Runnable {

    private String nombre;
    private int[] fichas;
    private Parque parque;

    private Atraccion atraccion;

    private boolean enParque;

    public Visitante(String nombre, Parque parque) {

        this.nombre = nombre;
        this.fichas = new int[4];
        this.parque = parque;

        this.enParque = false;

    }

    public String obtenerNombre() {
        return nombre;
    }

    public int obtenerFichas(String ficha) {
        return fichas[parque.traducirActividad(ficha)];
    }

    private void agregarFicha(String ficha) {

        fichas[parque.traducirActividad(ficha)]++;

    }

    private boolean entrarParque() {

        System.out.println("[" + nombre + "] trata de entrar al parque");

        boolean exito = parque.intentarEntrar();

        try {

            if (exito) {
                System.out.println(nombre + "esta entrado al parque");
                Thread.sleep(3000);
                System.out.println(nombre + "entro al parque");
            } else {
                System.out.println(nombre
                        + " no pudo entrar ..................................................... parque cerrado");
            }

            parque.liberarMolinete();

        } catch (Exception e) {
            System.out.println(e);
        }

        return exito;

    }

    private String elegirActividad() {

        String actividad;
        int decision;
        Random random = new Random();

        decision = random.nextInt(10) + 1;

        if (decision > 3) {
            actividad = "MR";
        } else {
            actividad = "Salir";
        }

        return actividad;

    }

    private void realizarActividades() {

        enParque = true;
        String actividad;
        boolean resultado;

        try {

            while (enParque) {

                actividad = elegirActividad();

                System.out.println("le salio " + actividad);

                if (!actividad.equals("Salir")) {
                    atraccion = parque.obtenerAtraccion(actividad);

                    resultado = atraccion.entrar();

                    if (resultado) {
                        atraccion.salir();

                        agregarFicha(atraccion.obtenerTipoFichas());

                    }

                    
                } else {
                    enParque = false;
                }

                Thread.sleep(1000);

            }

            System.out.println(nombre + "sale del parque ..................");

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void run() {

        try {

            boolean pudoEntrar = entrarParque();

            if (pudoEntrar) {

                //System.out.println("entro");

                realizarActividades();

            }

        } catch (Exception e) {
            // TODO: handle exception
        }

    }

}
