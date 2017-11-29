package com.dream.socket;

import com.dream.socket.codec.Message;
import com.dream.socket.logger.LoggerFactory;
import com.dream.socket.runnable.SendRunnable;
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
    private SendRunnable mSendRunnable;

    public DreamTCPSocket(String host, int port) {
        this.mHost = host;
        this.mPort = port;
    }

    @Override
    protected void onStart() {
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
                        if (mSendRunnable == null) {
                            mSendRunnable = new TCPSocketSendRunnable(mMessageCodec, mSocket.getOutputStream());
                        }
                        startRunnable(mSendRunnable);
                        startRunnable(mHandleRunnable);
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
    }

    @Override
    public boolean send(Message message) {
        if (mSendRunnable != null) {
            message.setRemoteAddress(mAddress);
            return mSendRunnable.send(message);
        }
        return false;
    }

    @Override
    public boolean send(SocketAddress address, Message message) {
        if (message != null) {
            return send(message);
        }
        return false;
    }

    @Override
    protected void onStop() {
        if (mHandleRunnable != null) {
            mHandleRunnable.stop();
            mHandleRunnable.status(Status.STATUS_DISCONNECT);
        }
        if (mSendRunnable != null) {
            mSendRunnable.stop();
        }
        if (mSocket != null) {
            shutdownInput(mSocket);
            shutdownOutput(mSocket);
            close(mSocket);
            mSocket = null;
        }
        mSendRunnable = null;
        mHandleRunnable = null;
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
                LoggerFactory.getLogger().error("关闭Socket输入异常", e);
            }
        }
    }

    private static void shutdownOutput(Socket socket) {
        if (socket != null && !socket.isOutputShutdown()) {
            try {
                LoggerFactory.getLogger().info("关闭Socket输出...");
                socket.shutdownOutput();
            } catch (IOException e) {
                LoggerFactory.getLogger().error("关闭Socket输出异常", e);
            }
        }
    }

    private static void close(Socket socket) {
        if (socket != null) {
            try {
                LoggerFactory.getLogger().info("关闭Socket...");
                socket.close();
            } catch (IOException e) {
                LoggerFactory.getLogger().error("关闭Socket异常", e);
            }
        }
    }
}
