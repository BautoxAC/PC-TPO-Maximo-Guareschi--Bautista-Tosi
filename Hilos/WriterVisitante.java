package Hilos;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class WriterVisitante {
    static BufferedWriter writer;

    public WriterVisitante() {
        try {

            writer = new BufferedWriter(new FileWriter("salida.txt"));
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public  synchronized void escribir(String texto) {
        try {
            writer.write(texto);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
