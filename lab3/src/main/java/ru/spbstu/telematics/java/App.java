package ru.spbstu.telematics.java;

import static java.lang.Thread.sleep;

public class App
{
    public static void main( String[] args )
    {
        Client[] clients = new Client[10];
        for (int i = 9; i >= 0; i--)
            clients[i] = new Client(i * 500);
        GasStation gs = new GasStation(clients);
        gs.start();
        try {
            sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        gs.newClient(new Client(11000));
        try {
            sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        gs.end();
    }
}
