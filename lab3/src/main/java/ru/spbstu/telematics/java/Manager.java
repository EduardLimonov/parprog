package ru.spbstu.telematics.java;

import java.util.concurrent.Semaphore;

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
        for (Pump p : gs.getPumps())
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
