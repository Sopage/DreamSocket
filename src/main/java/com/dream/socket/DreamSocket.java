package com.dream.socket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class DreamSocket {

    private ExecutorService pool;
    private boolean running;

    public final boolean start() {
        if (running) {
            return false;
        }
        if (pool != null) {
            if (!pool.isShutdown()) {
                pool.shutdownNow();
            }
            pool = null;
        }
        pool = Executors.newFixedThreadPool(3);
        pool.execute(new StartRunnable());
        running = true;
        return true;
    }

    public final boolean stop() {
        if (!running) {
            return false;
        }
        onStop();
        if (pool != null && !pool.isShutdown()) {
            pool.shutdownNow();
        }
        running = false;
        return true;
    }

    public abstract boolean onStart();

    public abstract boolean onStop();

    public abstract boolean isConnected();

    public class StartRunnable implements Runnable {

        @Override
        public void run() {
            onStart();
        }
    }

    public boolean isRunning() {
        return this.running;
    }

    protected boolean addExecuteRunnable(Runnable runnable){
        if(pool != null && !pool.isShutdown()){
            pool.execute(runnable);
            return true;
        }
        return false;
    }
}
