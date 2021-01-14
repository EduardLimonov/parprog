package ru.spbstu.telematics.java;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

class Pump implements Callable<Integer> {
    static int newId = 1;
    final static int PETROLEUM = 999;
    int petr = PETROLEUM;
    private final int id;
    Client client;
    ReentrantLock lock = new ReentrantLock();

    Pump() {
        id = newId++;
    }

    public String toString() {
        return "#" + id;
    }

    void clientComes(Client cl) {
        client = cl;
    }

    public Integer call() {
        int wn = client.getNeed(), got = 0;

        while (got < wn && petr > 0) {
            got++;
            petr--;
        }
        return got;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public int getPetr() {
        return petr; // количество оставшегося топлива
    }

    public void refuel() {
        int add = (PETROLEUM - petr) / 10;
        for (int i = 0; i < 10; i++)
            petr += add; // накачать топливо в колонку
    }
}
