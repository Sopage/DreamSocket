package com.dream.socket;

import com.dream.socket.codec.*;
import com.dream.socket.config.Config;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class DreamTCPSocket extends DreamSocket {

    private static final int SOCKET_CONNECT_TIMEOUT = 10000;
    private InetSocketAddress address;
    private String host;
    private int port;
    private Socket socket;
    private SocketSendRunnable send;
    private ByteProcess process;
    private HandleRunnable handle;

    public DreamTCPSocket(String host, int port){
        this.host = host;
        this.port = port;
    }

    public <T> void setEncode(Encode<T> encode){
        this.send = new SocketSendRunnable<T>(encode);
    }

    public <T> void setDecode(Decode<T> decode){
        this.process = new ByteBufferProcess<T>(decode);
    }

    public void setHandle(Handle handle){
        this.handle = new HandleRunnable(handle);
    }

    @Override
    public boolean onStart() {
        synchronized (this) {
            while (isRunning()) {
                try {
                    if (address == null) {
                        address = new InetSocketAddress(host, port);
                    }
                    socket = new Socket();
                    socket.connect(address, SOCKET_CONNECT_TIMEOUT);
                    if (socket.isConnected()) {
                        System.out.println("连接成功");
                        send.setOutputStream(socket.getOutputStream());
                        addExecuteRunnable(send);
                        this.process.setHandle(handle);
                        addExecuteRunnable(handle);
                        byte[] bytes = new byte[102400];
                        InputStream in = socket.getInputStream();
                        int length;
                        while ((length = in.read(bytes)) > 0) {
                            this.process.put(bytes, 0, length);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket = null;
                        if (isRunning()) {
                            this.handle.status(Handle.STATUS_FAIL);
                            Config.getConfig().getLogger().warn("6秒后尝试重连");
                            this.wait(6000);
                            Config.getConfig().getLogger().info("开始重连");
                        }
                    } catch (Exception ie) {
                        Config.getConfig().getLogger().error("重连发生异常", ie);
                    }
                }
            }
        }
        return false;
    }

    public void send(Object data){
        if(send != null){
            send.send(data);
        }
    }

    @Override
    public boolean onStop() {
        if(send != null){
            send.stop();
        }
        if(handle != null){
            handle.stop();
        }
        if (socket != null) {
            shutdownInput(socket);
            shutdownOutput(socket);
            close(socket);
            socket = null;
            handle.status(Handle.STATUS_DISCONNECT);
        }
        return true;
    }

    @Override
    public boolean isConnected() {
        if (socket != null && !socket.isClosed() && socket.isConnected() && isRunning()) {
            return true;
        }
        return false;
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
