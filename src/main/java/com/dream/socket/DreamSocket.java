package com.dream.socket;

import com.dream.socket.codec.Handle;
import com.dream.socket.config.Config;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class DreamSocket extends DreamNetwork {

    private SocketSendRunnable send = new SocketSendRunnable();
    private InetSocketAddress address;
    private String host;
    private int port;
    private Socket socket;

    public void setAddress(String host, int port) {
        this.host = host;
        this.port = port;
        this.address = null;
    }


    @Override
    public boolean isConnected() {
        if (socket != null && !socket.isClosed() && socket.isConnected() && isRunning()) {
            return true;
        }
        return false;
    }

    @Override
    public SendRunnable getSendRunnable() {
        return send;
    }

    @Override
    protected void doStartUp() {
        synchronized (this) {
            while (isRunning()) {
                try {
                    if (address == null) {
                        address = new InetSocketAddress(host, port);
                    }
                    socket = new Socket();
                    socket.connect(address, 10000);
                    if (socket.isConnected()) {
                        send.setOutputStream(socket.getOutputStream());
                        this.startSendAndHandler(this);
                        byte[] bytes = new byte[102400];
                        InputStream in = socket.getInputStream();
                        int len;
                        while ((len = in.read(bytes)) > 0) {
                            this.decode(bytes, 0, len);
                        }
                    }
                } catch (Exception e) {
                    Config.getConfig().getLogger().error("连接出现错误", e);
                } finally {
                    try {
                        this.stopSendAndHandler();
                        socket = null;
                        if (isRunning()) {
                            this.status(Handle.STATUS_FAIL);
                            Config.getConfig().getLogger().warn("6秒后尝试重连");
                            this.wait(6000);
                        }
                    } catch (InterruptedException ie) {
                        Config.getConfig().getLogger().error("重连发生异常", ie);
                    }
                }
            }
        }
    }

    @Override
    public void onStart(Runnable runnable) {
        if (runnable instanceof SendRunnable && isConnected()) {
            this.status(Handle.STATUS_CONNECTED);
        } else if (runnable instanceof HandleRunnable && isConnected()) {

        }
    }

    @Override
    protected void doStop() {
        if (socket != null) {
            shutdownInput(socket);
            shutdownOutput(socket);
            close(socket);
            socket = null;
            this.status(Handle.STATUS_DISCONNECT);
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
}