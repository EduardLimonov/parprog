package ru.spbstu.telematics.java;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
        } finally {
            pump.getLock().unlock(); // клиент освободил колонку (насос)
        }
    }
}
