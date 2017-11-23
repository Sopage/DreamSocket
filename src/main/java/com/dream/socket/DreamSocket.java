package com.dream.socket;

import com.dream.socket.logger.Logger;
import com.dream.socket.logger.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class DreamSocket {

    private ExecutorService pool;
    private boolean running;

    public final boolean start() {
        if (running) {
            LoggerFactory.getLogger().info("Socket已连接！");
            return true;
        }
        if (pool != null) {
            if (!pool.isShutdown()) {
                LoggerFactory.getLogger().warn("停止线程池");
                pool.shutdownNow();
            }
            pool = null;
        }
        pool = Executors.newFixedThreadPool(3);
        LoggerFactory.getLogger().info("创建线程池");
        pool.execute(new StartRunnable());
        running = true;
        return true;
    }

    public final boolean stop() {
        if (!running) {
            LoggerFactory.getLogger().info("Socket已关闭！");
            return true;
        }
        running = false;
        onStop();
        if (pool != null && !pool.isShutdown()) {
            pool.shutdownNow();
            LoggerFactory.getLogger().warn("停止线程池");
        }
        return true;
    }

    protected abstract boolean onStart();

    protected abstract boolean onStop();

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

    protected boolean executeRunnable(Runnable runnable) {
        if (pool != null && !pool.isShutdown()) {
            pool.execute(runnable);
            return true;
        }
        return false;
    }

    public void setLogger(Logger logger) {
        LoggerFactory.setLogger(logger);
    }
}
