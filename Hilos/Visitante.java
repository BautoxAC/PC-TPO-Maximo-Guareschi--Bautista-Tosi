package Hilos;

import java.util.Random;

import Objetos.*;

public class Visitante implements Runnable {

    private String nombre;
    private int[] fichas;
    private Parque parque;
    private int saldo;

    private Atraccion atraccion;

    private boolean enParque;

    public Visitante(String nombre, Parque parque) {

        this.nombre = nombre;
        this.fichas = new int[4];
        this.parque = parque;
        this.saldo = 0;

        fichas[1] = 0;

        this.enParque = false;

    }

    public String obtenerNombre() {
        return nombre;
    }

    public int obtenerFichas(String ficha) {
        return fichas[parque.traducirActividad(ficha)];
    }

    public void sacarFichas() {
        for (int i = 0; i < fichas.length; i++) {
            fichas[i] = 0;
        }
    }

    private void agregarFicha(String ficha) {

        int cantidadAAumentar = parque.obtenerValoresFicha(ficha);

        fichas[parque.traducirActividad(ficha)] += cantidadAAumentar;

    }

    private boolean entrarParque() {

        System.out.println(nombre + " trata de entrar al parque");

        boolean exito = parque.intentarEntrar();

        try {

            if (exito) {
                System.out.println(nombre + "esta entrado al parque");
                Thread.sleep(300);
                System.out.println(nombre + "entro al parque");
            } else {
                System.out.println(nombre
                        + " no pudo entrar ........ parque cerrado");
            }

            parque.liberarMolinete();

        } catch (Exception e) {
            System.out.println(e);
        }

        return exito;

    }

    private boolean tieneFichas() {
        int cantidadTotal = 0;

        for (int i = 0; i < fichas.length; i++) {
            cantidadTotal += fichas[i];
        }

        return (cantidadTotal > 30 || saldo > 30);
    }

    private void canjearFichas() {

        saldo += parque.canjearSaldo(this);

        Premio premio = parque.entrarAreaPremios(this);

        if (premio != null) {
            System.out.println("El visitante " + nombre + " recibio el premio ");
        }

    }

    private String elegirActividad() {

        String actividad;
        int decision;
        Random random = new Random();

        decision = random.nextInt(10) + 1;

        if (!parque.estaAbierto()) {
            decision = 0;
        }

        if (decision > 100) {
            actividad = "MR";
        } else if (decision > 10000000) {
            actividad = "AC";
        } else if (decision > 1) {
            actividad = "RV";
        } else {
            actividad = "Salir";
        }

        if (tieneFichas()) {
            actividad = "AreaPremios";
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

                System.out.println(nombre + " ENTRA a la actividad " + actividad);

                if (!actividad.equals("Salir") && !actividad.equals("AreaPremios")) {
                    atraccion = parque.obtenerAtraccion(actividad);

                    resultado = atraccion.entrar();

                    if (resultado) {
                        System.out.println(nombre + " ENTRO en la actividad y espera salir " + actividad);
                        this.esperarTiempo(actividad);
                        atraccion.salir();

                        agregarFicha(atraccion.obtenerTipoFichas());
                    }

                    System.out.println(nombre + " SALE de la actividad " + actividad);

                } else if (actividad.equals("AreaPremios")) {

                    canjearFichas();

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

                // System.out.println("entro");

                realizarActividades();

            }

        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    public int obtenerSaldo() {
        return this.saldo;
    }

    public void sacarSaldo(int saldo) {
        this.saldo -= saldo;
    }

    private void esperarTiempo(String actividad) {
        try {
            if (actividad.equals("RV")) {
                Thread.sleep(1500);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

}
