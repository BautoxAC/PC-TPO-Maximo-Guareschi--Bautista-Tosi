package Hilos;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.io.IOException;
import java.util.Random;

import Objetos.*;
import Recursos_Compartidos.CarreraGomones;
import Recursos_Compartidos.Parque;

public class Visitante extends Thread {

    private String nombre;
    private int[] fichas; // arreglo de las fichas
    private Parque parque;
    private int saldo;

    private Atraccion atraccion; // atraccion elegida actual

    private boolean enParque; // variable para saber si esta o no en parque

    private WriterVisitante wri; 

   

    public Visitante(String nombre, Parque parque, WriterVisitante wri) {
       
        this.nombre = nombre;
        this.fichas = new int[4];
        this.parque = parque;
        this.saldo = 0;
        this.wri= wri;

        this.enParque = false;

    }

    public void run() {

        boolean pudoEntrar;

        try {

            while (true) {

                pudoEntrar = entrarParque();

                if (pudoEntrar) {

                    realizarActividades();

                }

                wri.escribir("El visitante " + nombre + " se va a dormir ...");

                Thread.sleep(20000); // cuando sale o no pudo entrar, se va a dormir antes de volver a tratar de
                                     // entrar al parque

            }

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    // metodo de entrar al parque

    private boolean entrarParque() {

        wri.escribir(nombre + " trata de entrar al parque");

        boolean exito = parque.intentarEntrar(); // metodo de entrar al parque

        try {

            if (exito) {
                wri.escribir(nombre + "esta entrado al parque");
                Thread.sleep(300);
                wri.escribir(nombre + " entro al parque");
            } else {
                wri.escribir(nombre
                        + " no pudo entrar ........ parque cerrado");
            }

            parque.liberarMolinete(); // libera el molinete cuando termina de pasar

        } catch (Exception e) {
           System.out.println(e);
        }

        return exito;

    }

    // metodos PRIVADOS de fichas (tiene y canjear)

    private boolean tieneFichas() {
        int cantidadTotal = 0;

        for (int i = 0; i < fichas.length; i++) {
            cantidadTotal += fichas[i];
        }

        return (cantidadTotal > 30 || saldo > 30); // si tiene mas de 30 de saldo o tiene mas de 30 fichas en total,
                                                   // puede entrar a areapremios
    }

    private void canjearFichas() {

        this.saldo += parque.canjearSaldo(); // canjea las fichas por saldo

        Premio premio = parque.entrarAreaPremios(); // recibe el premio correspondiente

        if (premio != null) {
            wri.escribir("El visitante " + nombre + " recibio el premio ");
        }

    }

    // metodos PRIVADOS de actividades (elegir y realizar)

    private String elegirActividad() {

        String actividad;
        int decision;
        Random random = new Random();

        decision = random.nextInt(10) + 1; // toma una decicion, con un numero random del 1 al 10

        if (!parque.estaAbierto()) {
            decision = 10;
        }

        if (decision <= 1) {
            actividad = "MR";
        } else if (decision <= 3) {
            actividad = "AC";
        } else if (decision <= 4) {
            actividad = "RV";
        } else if (decision <= 6) {
            actividad = "CG";
        } else if (decision <= 7) {
            actividad = "CO";
        } else if (decision <= 9) {
            actividad = "TE";
        } else {
            actividad = "Salir";
        }

        if (decision == 8 && tieneFichas()) {
            actividad = "AreaPremios";
        }

        return actividad; // retorna la actividad elegida

    }

    private void realizarActividades() {

        enParque = true;
        String actividad;
        boolean resultado;

        try {

            while (enParque) { // mientras este dentro del parque, elige una actividad

                actividad = elegirActividad();

                wri.escribir(nombre + " TRATA DE ENTRAR a la actividad " + actividad);

                if (!actividad.equals("Salir") && !actividad.equals("AreaPremios")) { // si no es salir no areapremios,
                                                                                      // es una atraccion
                    atraccion = parque.obtenerAtraccion(actividad);

                    resultado = atraccion.entrar();

                    if (resultado) {
                        wri.escribir(nombre + " ENTRO Y ESPERA salir de actividad " + actividad);
                        this.esperarTiempo(actividad);
                        atraccion.salir();

                        agregarFicha(atraccion.obtenerTipoFichas());
                    }

                    wri.escribir(nombre + " SALE de la actividad " + actividad);

                } else if (actividad.equals("AreaPremios")) {

                    canjearFichas();

                } else {
                    enParque = false;
                }

                Thread.sleep(1000);

            }

            wri.escribir(nombre + "sale del parque ..................");

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    // metodo de esperar

    private void esperarTiempo(String actividad) { // esperan con respecto a la actividad elegida
        boolean espera = true;
        try {
            if (actividad.equals("RV")) {
                Thread.sleep(1500);
            } else if (actividad.equals("CG")) {
                wri.escribir("El visitante esta yendo en la carrera ....");
                espera = atraccion.preparar();
                if (espera) {
                    Thread.sleep(3000);
                }
            } else if (actividad.equals("CO")) {
                wri.escribir("El visitante come ....");
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    // metodo de sacar saldo

    public void sacarSaldo(int saldo) {
        this.saldo -= saldo;
    }

    // metodos de modificar fichas

    public void sacarFichas() { // metodo para sacar todas las fichas cuando se transforman en saldo
        for (int i = 0; i < fichas.length; i++) {
            fichas[i] = 0;
        }
    }

    public void agregarFicha(String ficha) {

        int cantidadAAumentar = parque.obtenerValoresFicha(ficha); // obtiene de parque cuanto vale la ficha

        if (cantidadAAumentar != 0) {
            fichas[parque.traducirActividad(ficha)] += cantidadAAumentar; // obtiene de parque la posicion del arreglo
                                                                          // de la ficha y suma su valor
        }

    }

    // metodos de obtenecion de datos (getters)

    public String obtenerNombre() {
        return nombre;
    }

    public int obtenerFichas(String ficha) {
        return fichas[parque.traducirActividad(ficha)]; // obtiene de parque la posicion del arreglo en base al nombre
                                                        // de la ficha
    }

    public int obtenerSaldo() {
        return this.saldo;
    }

}
