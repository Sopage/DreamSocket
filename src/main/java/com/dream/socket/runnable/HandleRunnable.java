package com.dream.socket.runnable;

import com.dream.socket.Status;
import com.dream.socket.codec.Message;
import com.dream.socket.codec.MessageHandle;
import com.dream.socket.logger.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;

public class HandleRunnable<T extends Message> implements Runnable {

    private LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>();
    private MessageHandle<T> handle;

    public HandleRunnable(MessageHandle<T> handle) {
        this.handle = handle;
    }

    @Override
    public void run() {
        synchronized (this) {
            queue.clear();
            LoggerFactory.getLogger().info("开启 -> 接收线程");
            try {
                handle.onStatus(Status.STATUS_CONNECTED);
                while (true) {
                    T data = queue.take();
                    if (handle != null) {
                        handle.onMessage(data);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LoggerFactory.getLogger().info("结束 -> 接收线程");
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
}
