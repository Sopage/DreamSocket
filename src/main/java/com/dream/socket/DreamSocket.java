package com.dream.socket;

import com.dream.socket.codec.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DreamSocket implements Runnable {
    private InetSocketAddress address;
    private String host;
    private int port;
    private ExecutorService pool;
    private Socket socket;
    private WriteRunnable writeRunnable;
    private HandleRunnable handleRunnable;
    private ByteProcess process;
    private boolean running;
    private boolean isReadBuffer;

    public DreamSocket() {
        writeRunnable = new WriteRunnable();
        handleRunnable = new HandleRunnable();
    }

    public DreamSocket(boolean isReadBuffer) {
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
        writeRunnable.setCodec(codec);
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

    public boolean isConnected() {
        if (socket != null && !socket.isClosed() && socket.isConnected() && isRunning()) {
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        synchronized (this) {
            while (isRunning()) {
                try {
                    if (address == null) {
                        address = new InetSocketAddress(host, port);
                    }
                    socket = new Socket();
                    socket.connect(address);
                    if (socket.isConnected()) {
                        writeRunnable.setOutputStream(socket.getOutputStream());
                        pool.execute(writeRunnable);
                        handleRunnable.status(Handle.STATUS_CONNECTED);
                        pool.execute(handleRunnable);
                        byte[] bytes = new byte[102400];
                        InputStream in = socket.getInputStream();
                        int len;
                        while ((len = in.read(bytes)) > 0) {
                            process.put(bytes, len);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    printError("连接错误");
                    try {
                        socket = null;
                        if (isRunning()) {
                            handleRunnable.status(Handle.STATUS_FAIL);
                            printError("6秒后尝试重连");
                            this.wait(6000);
                        }
                    } catch (InterruptedException ie) {
                        printError("重连发生异常");
                    }
                }
            }
        }
    }

    public void send(Object data) {
        if (writeRunnable != null) {
            writeRunnable.send(data);
        }
    }

    public void stop() {
        running = false;
        if (writeRunnable != null) {
            writeRunnable.stop();
        }
        if (handleRunnable != null) {
            handleRunnable.stop();
        }
        if (socket != null) {
            shutdownInput(socket);
            shutdownOutput(socket);
            close(socket);
            socket = null;
            if (handleRunnable != null) {
                handleRunnable.status(Handle.STATUS_DISCONNECT);
            }
        }
        if (pool != null && !pool.isShutdown()) {
            pool.shutdown();
        }

    }

    private static final class WriteRunnable implements Runnable {

        private Vector<Object> vector = new Vector<>();
        private Codec codec;
        private OutputStream out;
        private boolean sending;
        private ByteBuffer buffer = ByteBuffer.allocate(102400);

        private void setCodec(Codec codec) {
            this.codec = codec;
        }

        private void setOutputStream(OutputStream stream) {
            this.out = stream;
        }

        @Override
        public void run() {
            synchronized (this) {
                sending = true;
                while (sending) {
                    if (vector.size() == 0) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    while (vector.size() > 0) {
                        Object data = vector.remove(0);
                        buffer.clear();
                        codec.getEncode().encode(data, buffer);
                        buffer.flip();
                        if (out != null) {
                            try {
                                System.out.println("----->   " + data);
                                out.write(buffer.array(), 0, buffer.limit());
                                out.flush();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        private void stop() {
            sending = false;
            this.out = null;
            synchronized (this) {
                this.notify();
            }
        }

        private void send(Object data) {
            this.vector.add(data);
            synchronized (this) {
                this.notify();
            }
        }

    }

    private static void shutdownInput(Socket socket) {
        if (socket != null && !socket.isInputShutdown()) {
            try {
                socket.shutdownInput();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void shutdownOutput(Socket socket) {
        if (socket != null && !socket.isOutputShutdown()) {
            try {
                socket.shutdownOutput();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void close(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void printError(String text) {
        System.err.println(text);
    }
}