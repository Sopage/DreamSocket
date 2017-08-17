package com.dream.socket;

import com.dream.socket.codec.Handle;
import com.dream.socket.config.Config;
import com.dream.socket.listener.OnStartListener;

import java.util.Vector;

public class HandleRunnable implements Runnable {

    private Vector<Object> vector = new Vector<>();
    private Handle handle;
    private boolean running;
    private OnStartListener listener;

    public void setHandle(Handle handle) {
        this.handle = handle;
    }

    public void setOnStartListener(OnStartListener listener){
        this.listener = listener;
    }

    @Override
    public void run() {
        synchronized (this) {
            running = true;
            Config.getConfig().getLogger().debug("start 开启接收线程！");
            if(listener != null){
                listener.onStart(this);
            }
            while (running) {
                if (vector.size() == 0) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        Config.getConfig().getLogger().error("接收线程等待异常！", e);
                    }
                }

                while (vector.size() > 0) {
                    Object data = vector.remove(0);
                    if (handle != null) {
                        handle.onMessage(data);
                    }
                }
            }
        }
        Config.getConfig().getLogger().debug("stop 结束接收线程！");
    }

    public boolean put(Object d) {
        if(running){
            vector.add(d);
            synchronized (this) {
                this.notify();
            }
            return true;
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
        synchronized (this) {
            this.notify();
        }
    }

    public boolean handleIsNull(){
        return handle == null;
    }
}
