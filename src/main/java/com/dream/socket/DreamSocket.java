package com.dream.socket;

import com.dream.socket.codec.Message;
import com.dream.socket.codec.MessageCodec;
import com.dream.socket.codec.MessageDecode;
import com.dream.socket.codec.MessageHandle;
import com.dream.socket.logger.Logger;
import com.dream.socket.logger.LoggerFactory;
import com.dream.socket.runnable.HandleRunnable;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class DreamSocket {

    private ExecutorService pool;
    private boolean running;
    protected MessageCodec mMessageCodec;
    protected MessageDecode mMessageDecode;
    protected HandleRunnable mHandleRunnable;

    public final boolean start() {
        if (mMessageCodec == null || mHandleRunnable == null) {
            return false;
        }
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
        startRunnable(new StartRunnable());
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

    public final void codec(MessageCodec codec) {
        this.mMessageCodec = codec;
        this.mMessageDecode = new MessageDecode() {
            @Override
            protected Message decode(SocketAddress address, ByteBuffer buffer) {
                Message message = codec.decode(address, buffer);
                if (message != null) {
                    message.setRemoteAddress(address);
                    if (mHandleRunnable != null) {
                        mHandleRunnable.put(message);
                    }
                }
                return message;
            }
        };
    }

    public final void handle(MessageHandle handle) {
        this.mHandleRunnable = new HandleRunnable(handle);
    }

    public abstract boolean send(Message message);

    public abstract boolean send(SocketAddress address, Message message);

    protected abstract void onStart();

    protected abstract void onStop();

    public abstract boolean isConnected();

    private final class StartRunnable implements Runnable {

        @Override
        public void run() {
            onStart();
        }
    }

    public final boolean isRunning() {
        return this.running;
    }

    protected boolean startRunnable(Runnable runnable) {
        if (pool != null && !pool.isShutdown() && runnable != null) {
            pool.execute(runnable);
            return true;
        }
        return false;
    }

    public void setLogger(Logger logger) {
        LoggerFactory.setLogger(logger);
    }
}
