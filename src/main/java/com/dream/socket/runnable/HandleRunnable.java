package com.dream.socket.runnable;

import com.dream.socket.codec.MessageHandle;

import java.util.concurrent.LinkedBlockingQueue;

public class HandleRunnable<T> implements Runnable {

    private LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>();
    private MessageHandle<T> handle;
    private boolean running;

    public HandleRunnable(MessageHandle<T> handle) {
        this.handle = handle;
    }

    @Override
    public void run() {
        synchronized (this) {
            running = true;
            queue.clear();
            System.out.println("接收线程 -> 开启");
            try {
                while (running) {
                    T data = queue.take();
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
        System.out.println("接收线程 -> 结束");
    }

    public boolean put(T d) {
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
