package com.dream.socket;

import com.dream.socket.codec.Handle;
import com.dream.socket.config.Config;
import com.dream.socket.listener.OnStartListener;

import java.util.concurrent.LinkedBlockingQueue;

public class HandleRunnable implements Runnable {

    private LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();
    private Handle handle;
    private boolean running;
    private OnStartListener listener;

    public void setHandle(Handle handle) {
        this.handle = handle;
    }

    public void setOnStartListener(OnStartListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        synchronized (this) {
            running = true;
            queue.clear();
            Config.getConfig().getLogger().debug("start 开启接收线程！");
            if (listener != null) {
                listener.onStart(this);
            }
            try {
                while (running) {
                    Object data = queue.take();
                    if (!running) {
                        return;
                    }
                    if (handle != null) {
                        handle.onMessage(data);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Config.getConfig().getLogger().debug("stop 结束接收线程！");
    }

    public boolean put(Object d) {
        try {
            if (running) {
                this.queue.put(d);
                return true;
            }
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
        this.put(new Object());
    }

    public boolean handleIsNull() {
        return handle == null;
    }
}
