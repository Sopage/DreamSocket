package com.dream.socket;

import com.dream.socket.codec.Message;
import com.dream.socket.codec.MessageDecode;
import com.dream.socket.codec.MessageEncode;
import com.dream.socket.codec.MessageHandle;
import com.dream.socket.logger.LoggerFactory;
import com.dream.socket.runnable.HandleRunnable;
import com.dream.socket.runnable.TCPSocketSendRunnable;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class DreamTCPSocket extends DreamSocket {

    private static final int SOCKET_CONNECT_TIMEOUT = 10000;
    private SocketAddress mAddress;
    private String mHost;
    private int mPort;
    private Socket mSocket;
    private TCPSocketSendRunnable mSocketSendRunnable;
    private MessageDecode mMessageDecode;
    private HandleRunnable mHandleRunnable;

    public DreamTCPSocket(String host, int port) {
        this.mHost = host;
        this.mPort = port;
    }

    public <T extends Message> void codec(MessageDecode<T> messageDecode, MessageHandle<T> messageHandle, MessageEncode<T> messageEncode) {
        this.mHandleRunnable = new HandleRunnable<>(messageHandle);
        messageDecode.setHandleRunnable(mHandleRunnable);
        this.mMessageDecode = messageDecode;
        this.mSocketSendRunnable = new TCPSocketSendRunnable<>(messageEncode);
    }

    @Override
    protected boolean onStart() {
        synchronized (this) {
            while (isRunning()) {
                try {
                    if (mAddress == null) {
                        mAddress = new InetSocketAddress(mHost, mPort);
                    }
                    mSocket = new Socket();
                    LoggerFactory.getLogger().info("开始连接 -> " + mAddress.toString());
                    mSocket.connect(mAddress, SOCKET_CONNECT_TIMEOUT);
                    if (mSocket.isConnected()) {
                        LoggerFactory.getLogger().info("连接成功");
                        mSocketSendRunnable.setOutputStream(mSocket.getOutputStream());
                        executeRunnable(mSocketSendRunnable);
                        executeRunnable(mHandleRunnable);
                        byte[] bytes = new byte[102400];
                        InputStream in = mSocket.getInputStream();
                        int length;
                        while ((length = in.read(bytes)) > 0) {
                            this.mMessageDecode.decode(mAddress, bytes, 0, length);
                        }
                    } else {
                        LoggerFactory.getLogger().error("连接失败！");
                    }
                } catch (Exception e) {
                    LoggerFactory.getLogger().error("连接异常", e);
                } finally {
                    try {
                        mSocket = null;
                        if (isRunning()) {
                            this.mHandleRunnable.status(Status.STATUS_FAIL);
                            LoggerFactory.getLogger().info("6秒后尝试重连");
                            this.wait(6000);
                            LoggerFactory.getLogger().info("开始重连 -> " + mAddress.toString());
                        }
                    } catch (Exception ie) {
                        LoggerFactory.getLogger().error("wait异常", ie);
                    }
                }
            }
        }
        return false;
    }

    public <T extends Message> boolean send(T data) {
        if (mSocketSendRunnable != null) {
            data.mAddress = mAddress;
            return mSocketSendRunnable.send(data);
        }
        return false;
    }

    @Override
    protected boolean onStop() {
        if (mSocket != null) {
            shutdownInput(mSocket);
            shutdownOutput(mSocket);
            close(mSocket);
            mSocket = null;
            if (mHandleRunnable != null) {
                mHandleRunnable.status(Status.STATUS_DISCONNECT);
            }
        }
        mSocketSendRunnable = null;
        mHandleRunnable = null;
        return true;
    }

    @Override
    public boolean isConnected() {
        if (mSocket != null && !mSocket.isClosed() && mSocket.isConnected() && isRunning()) {
            return true;
        }
        return false;
    }

    private static void shutdownInput(Socket socket) {
        if (socket != null && !socket.isInputShutdown()) {
            try {
                LoggerFactory.getLogger().info("关闭Socket输入...");
                socket.shutdownInput();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void shutdownOutput(Socket socket) {
        if (socket != null && !socket.isOutputShutdown()) {
            try {
                LoggerFactory.getLogger().info("关闭Socket输出...");
                socket.shutdownOutput();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void close(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                LoggerFactory.getLogger().info("关闭Socket...");
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
