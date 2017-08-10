package com.dream.socket;

import com.dream.socket.codec.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DreamDatagramSocket implements Runnable {

    private ExecutorService pool;
    private InetSocketAddress address;
    private String host;
    private int port;
    private DatagramSocket socket;
    private ByteProcess process;
    private HandleRunnable handleRunnable;
    private boolean running;
    private boolean isReadBuffer;

    public DreamDatagramSocket() {
        handleRunnable = new HandleRunnable();
    }

    public DreamDatagramSocket(boolean isReadBuffer) {
        this();
        this.isReadBuffer = isReadBuffer;
    }

    public void setAddress(String host, int port) {
        this.host = host;
        this.port = port;
        this.address = null;
    }

    public void setHandle(Handle handle) {
        handleRunnable.setHandle(handle);
    }

    public void setCodec(Codec codec) {
        if (isReadBuffer) {
            process = new ByteBufferProcess();
        } else {
            process = new ByteArrayProcess();
        }
        process.setCodec(codec);
        process.setHandle(handleRunnable);
    }

    public void start() {
        if (process.codecIsNull()) {
            throw new NullPointerException("请设置编解码器");
        }
        if (handleRunnable.handleIsNull()) {
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

    private boolean isRunning() {
        return this.running;
    }

    @Override
    public void run() {
        synchronized (this) {
            try {
                address = new InetSocketAddress(host, port);
                socket = new DatagramSocket();
                pool.execute(handleRunnable);
                final byte[] bytes = new byte[102400];
                DatagramPacket packet;
                while (isRunning()) {
                    packet = new DatagramPacket(bytes, bytes.length);
                    socket.receive(packet);
                    int length = packet.getLength();
                    process.put(packet.getData(), length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void send(String data) {
        byte[] buffer = data.getBytes();
        try {
            socket.send(new DatagramPacket(buffer, 0, buffer.length, address));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
