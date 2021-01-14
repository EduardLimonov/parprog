package ru.spbstu.telematics.java;

import java.util.concurrent.ExecutionException;

class Client {
    static int newId = 1;
    private final int id;
    private int need;

    Client(int need) {
        this.need = need;
        id = newId++;
    }

    void doService(Pump pump, GasStation gs) {
        Service service = new Service(this, pump);
        do {
            try {
                service.go();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gs.checkPump(getNeed(), pump); // возможно, на этом клиенте закончилось топливо в колонке, и он не смог полностью заправиться
        } while (!ok()); // пока клиент неудовлетворен, оказывать услугу
        System.out.println("Клиент " + this + " заправился на колонке " + pump + ". Осталось топлива: " + pump.getPetr() + ".");
    }

    int getNeed() {
        return need;
    }

    void refuel(int n) {
        need -= n;
    }

    boolean ok() {
        return need == 0;
    }

    public String toString() {
        return "#" + id;
    }
}
