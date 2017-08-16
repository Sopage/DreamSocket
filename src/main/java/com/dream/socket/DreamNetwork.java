package com.dream.socket;

import com.dream.socket.codec.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class DreamNetwork implements Runnable {

    private ExecutorService pool;
    private SendRunnable sendRunnable;
    private boolean isReadBuffer;
    private boolean running;
    private HandleRunnable handle;
    private ByteProcess process;

    public DreamNetwork() {
        handle = new HandleRunnable();
    }

    public void isReadBuffer(boolean isReadBuffer) {
        this.isReadBuffer = isReadBuffer;
    }

    public void setHandle(Handle handle) {
        this.handle.setHandle(handle);
    }

    public void setCodec(Codec codec) {
        if (sendRunnable == null) {
            sendRunnable = getSendRunnable();
        }
        sendRunnable.setCodec(codec);
        if (isReadBuffer) {
            process = new ByteBufferProcess();
        } else {
            process = new ByteArrayProcess();
        }
        process.setCodec(codec);
        process.setHandle(handle);
    }

    public final void start() {
        if (process.codecIsNull()) {
            throw new NullPointerException("请设置编解码器");
        }
        process.reset();
        if (handle.handleIsNull()) {
            throw new NullPointerException("请设置消息处理器");
        }
        if (running) {
            return;
        }
        if (pool != null) {
            if (!pool.isShutdown()) {
                pool.shutdown();
            }
            pool = null;
        }
        pool = Executors.newFixedThreadPool(3);
        running = true;
        pool.execute(this);
    }

    public final void stop() {
        running = false;
        stopSendAndHandler();
        doStop();
        if (pool != null && !pool.isShutdown()) {
            pool.shutdown();
        }
    }

    protected abstract void doStop();

    public abstract boolean isConnected();

    protected abstract void doStartUp();

    public abstract SendRunnable getSendRunnable();

    @Override
    public final void run() {
        doStartUp();
    }

    public boolean isRunning() {
        return this.running;
    }

    protected void decode(byte[] bytes, int offset, int length) {
        this.process.put(bytes, offset, length);
    }

    public void send(Object data) {
        if (sendRunnable != null) {
            sendRunnable.send(data);
        }
    }

    protected void startSendAndHandler() {
        this.process.reset();
        this.pool.execute(sendRunnable);
        this.pool.execute(handle);
    }

    protected void stopSendAndHandler() {
        if (sendRunnable != null) {
            sendRunnable.stop();
        }
        if (handle != null) {
            handle.stop();
        }
    }

    protected void status(int status) {
        if (this.handle != null) {
            this.handle.status(status);
        }
    }

    protected void printError(String text) {
        System.err.println("DreamSocket: " + text);
    }
}
