package ru.spbstu.telematics.java;

import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

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
        for (int i = 0; i < 10; i ++)
            petr += add; // накачать топливо в колонку
    }
}

class Service {
    Client client;
    Pump pump;

    public Service(Client cl, Pump p) {
        client = cl;
        pump = p;
    }

    public void go() throws ExecutionException, InterruptedException {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        try {
            pump.getLock().lock(); // клиент занял колонку (насос)
            pump.clientComes(client);
            Future<Integer> got = ex.submit(pump);
            client.refuel(got.get()); // клиент заливает столько топлива, сколько накачала бензоколонка (насос)
            pump.clientComes(null);
            ex.shutdown();
        }
        finally {
            pump.getLock().unlock(); // клиент освободил колонку (насос)
        }
    }
}

class Manager implements Runnable {
    GasStation gs;
    Semaphore occuped = new Semaphore(GasStation.NUMBER_OF_PUMPS);

    public Manager(GasStation gasStation) {
        gs = gasStation;
    }

    private Pump findPump() {
        try {
            occuped.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (Pump p: gs.getPumps())
            if (!p.getLock().isLocked())
                return p;

        return null; // null никогда не будет возвращено; заглушка, чтобы скомпилировалось
    }

    public void run() {
        while (!gs.isEnded()) {
            if (gs.getClients().isEmpty())
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            if (!gs.getClients().isEmpty()) {
                // чтобы не создавать лишних потоков, которые будут ждать клиента
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            gs.getClient().doService(findPump(), gs);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        occuped.release();
                    }
                }).start();
            }
        }
    }

    void waitForClients() {
        try {
            occuped.acquire(GasStation.NUMBER_OF_PUMPS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void breakLocks() {
        occuped.release(GasStation.NUMBER_OF_PUMPS);
    }
}



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
