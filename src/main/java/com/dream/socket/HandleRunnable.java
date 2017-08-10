package com.dream.socket;

import com.dream.socket.codec.Handle;

import java.util.Vector;

public class HandleRunnable implements Runnable {

    private Vector vector = new Vector();
    private Handle handle;
    private boolean running;

    public void setHandle(Handle handle) {
        this.handle = handle;
    }

    @Override
    public void run() {
        synchronized (this) {
            running = true;
            while (running) {
                if (vector.size() == 0) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                while (vector.size() > 0) {
                    Object data = vector.remove(0);
                    if (handle != null) {
                        handle.onReceive(data);
                    }
                }
            }
        }
    }

    public void put(Object d) {
        vector.add(d);
        synchronized (this) {
            this.notify();
        }
    }

    public void status(int status) {
        if (handle != null) {
            handle.onStatus(status);
        }
    }

    public void stop() {
        running = false;
        synchronized (this) {
            this.notify();
        }
    }

    public boolean handleIsNull(){
        return handle == null;
    }
}
