package ru.spbstu.telematics.java;

import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;


public class GasStation {
    final static int NUMBER_OF_PUMPS = 3;

    protected BlockingQueue<Client> clients;
    protected final Manager manager;
    protected Pump[] pumps;
    boolean ended = false;

    GasStation(Client [] clients) {
        this.clients = new LinkedBlockingQueue<Client>(Arrays.asList(clients));
        manager = new Manager(this);
        pumps = new Pump[NUMBER_OF_PUMPS];
        for (int i = 0; i < pumps.length; i++)
            pumps[i] = new Pump();
    }

    public void newClient(Client client) {
        clients.add(client);
        synchronized (manager) {
            manager.notify();
        }
    }

    void checkPump(int need, Pump pump) {
        pump.getLock().lock();
        if (need > pump.getPetr()) {
            pump.refuel(); // требуется больше топлива, чем осталось в колонке
            System.out.println("Колонка " + pump + " была заправлена");
        }
        pump.getLock().unlock();
    }

    Pump[] getPumps() {
        return pumps;
    }

    Client getClient() throws InterruptedException {
        return clients.take();
    }

    public void start() {
        Thread t = new Thread(manager);
        t.setDaemon(true); // чтобы работа программы завершалась без вызова end()
        t.start();
    }

    public void end() {
        manager.waitForClients();
        ended = true;
        synchronized (manager) {
            manager.notify();
        }
        System.out.println("Заправка завершает работу");
        manager.breakLocks();
    }

    public void restart() {
        ended = false;
        start();
    }

    boolean isEnded() {
        return ended;
    }

    public BlockingQueue<Client> getClients() {
        return clients;
    }
}
