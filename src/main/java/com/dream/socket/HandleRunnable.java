package com.dream.socket;

import com.dream.socket.codec.Handle;
import com.dream.socket.config.Config;

import java.util.concurrent.LinkedBlockingQueue;

public class HandleRunnable implements Runnable {

    private LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
    private Handle handle;
    private boolean running;

    public HandleRunnable(Handle handle) {
        this.handle = handle;
    }

    @Override
    public void run() {
        synchronized (this) {
            running = true;
            queue.clear();
            Config.getConfig().getLogger().debug("接收线程 -> 开启");
            try {
                while (running) {
                    Object data = queue.take();
                    if (!running) {
                        continue;
                    }
                    if (handle != null) {
                        handle.onMessage(data);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Config.getConfig().getLogger().debug("接收线程 -> 结束");
    }

    public boolean put(Object d) {
        try {
            this.queue.put(d);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void status(int status) {
        if (handle != null) {
            handle.onStatus(status);
        }
    }

    public void stop() {
        running = false;
    }
}
