package com.dream.socket;

import com.dream.socket.codec.Message;
import com.dream.socket.codec.MessageDecode;
import com.dream.socket.codec.MessageEncode;
import com.dream.socket.codec.MessageHandle;
import com.dream.socket.logger.LoggerFactory;
import com.dream.socket.runnable.HandleRunnable;
import com.dream.socket.runnable.UDPSocketSendRunnable;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

public class DreamUDPSocket extends DreamSocket {

    private DatagramSocket mSocket;
    private UDPSocketSendRunnable mSocketSendRunnable;
    private MessageDecode mMessageDecode;
    private HandleRunnable mHandleRunnable;

    public <T extends Message> void codec(MessageDecode<T> messageDecode, MessageHandle<T> messageHandle, MessageEncode<T> messageEncode) {
        this.mHandleRunnable = new HandleRunnable<>(messageHandle);
        messageDecode.setHandleRunnable(mHandleRunnable);
        this.mMessageDecode = messageDecode;
        this.mSocketSendRunnable = new UDPSocketSendRunnable<>(messageEncode);
    }

    @Override
    public boolean onStart() {
        synchronized (this) {
            try {
                LoggerFactory.getLogger().info("开始创建UDP管道");
                mSocket = new DatagramSocket();
            } catch (Exception e) {
                LoggerFactory.getLogger().error("UDP管道创建失败！", e);
                return false;
            }
            mSocketSendRunnable.setDatagramSocket(mSocket);
            executeRunnable(mSocketSendRunnable);
            executeRunnable(mHandleRunnable);
            byte[] buffer = new byte[500];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                while (isRunning()) {
                    packet.setData(buffer, 0, buffer.length);
                    mSocket.receive(packet);
                    mMessageDecode.decode(packet.getSocketAddress(), packet.getData(), packet.getOffset(), packet.getLength());
                }
            } catch (Exception e) {
                LoggerFactory.getLogger().error("UDP receive执行异常！", e);
            } finally {
                try {
                    mSocket = null;
                    if (isRunning()) {
                        this.mHandleRunnable.status(Status.STATUS_FAIL);
                        LoggerFactory.getLogger().info("6秒后重新创建UDP管道");
                        this.wait(6000);
                    }
                } catch (Exception ie) {
                    LoggerFactory.getLogger().error("wait奔溃！", ie);
                }
            }

        }
        return false;
    }

    @Override
    public boolean onStop() {
        if (mHandleRunnable != null) {
            mHandleRunnable.stop();
            mHandleRunnable.status(Status.STATUS_DISCONNECT);
        }
        if (mSocketSendRunnable != null) {
            mSocketSendRunnable.stop();
        }
        if (mSocket != null) {
            LoggerFactory.getLogger().info("关闭UDP管道");
            mSocket.close();
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

    public boolean send(SocketAddress address, Message data) {
        if (mSocketSendRunnable != null) {
            data.setRemoteAddress(address);
            mSocketSendRunnable.send(data);
            return true;
        }
        return false;
    }
}
