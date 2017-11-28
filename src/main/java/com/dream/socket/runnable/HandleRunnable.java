package com.dream.socket.runnable;

import com.dream.socket.Status;
import com.dream.socket.codec.Message;
import com.dream.socket.codec.MessageHandle;
import com.dream.socket.logger.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;

public class HandleRunnable<T extends Message> implements Runnable {

    private LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>();
    private MessageHandle<T> handle;
    private boolean isHandle = false;

    public HandleRunnable(MessageHandle<T> handle) {
        this.handle = handle;
    }

    @Override
    public void run() {
        LoggerFactory.getLogger().info("开启 -> 接收线程");
        handle.onStatus(Status.STATUS_CONNECTED);
        isHandle = true;
        while (isHandle) {
            try {
                handing();
            } catch (Exception e) {
                LoggerFactory.getLogger().error("异常 -> 接收线程异常退出", e);
            }
        }
        LoggerFactory.getLogger().info("结束 -> 接收线程");
    }

    private void handing() throws Exception {
        while (true) {
            T data = queue.take();
            if (handle != null) {
                handle.onMessage(data);
            }
        }
    }

    public boolean put(T d) {
        try {
            this.queue.put(d);
            return true;
        } catch (Exception e) {
            LoggerFactory.getLogger().error("异常 -> 接收线程 queue.put() 异常", e);
        }
        return false;
    }

    public void stop(){
        isHandle = false;
    }

    public void status(int status) {
        if (handle != null) {
            handle.onStatus(status);
        }
    }
}
