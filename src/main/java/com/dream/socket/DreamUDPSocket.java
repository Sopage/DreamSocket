package com.dream.socket;

import com.dream.socket.codec.Message;
import com.dream.socket.logger.LoggerFactory;
import com.dream.socket.runnable.SendRunnable;
import com.dream.socket.runnable.UDPSocketSendRunnable;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

public class DreamUDPSocket extends DreamSocket {

    private DatagramSocket mSocket;
    private SendRunnable mSendRunnable;

    @Override
    public void onStart() {
        synchronized (this) {
            try {
                LoggerFactory.getLogger().info("开始创建UDP管道");
                mSocket = new DatagramSocket();
            } catch (Exception e) {
                LoggerFactory.getLogger().error("UDP管道创建失败！", e);
                return;
            }
            if (mSendRunnable == null) {
                mSendRunnable = new UDPSocketSendRunnable(mMessageCodec, mSocket);
            }
            startRunnable(mSendRunnable);
            startRunnable(mHandleRunnable);
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
    }

    @Override
    public void onStop() {
        if (mHandleRunnable != null) {
            mHandleRunnable.stop();
            mHandleRunnable.status(Status.STATUS_DISCONNECT);
        }
        if (mSendRunnable != null) {
            mSendRunnable.stop();
        }
        if (mSocket != null) {
            LoggerFactory.getLogger().info("关闭UDP管道");
            mSocket.close();
            mSocket = null;
            if (mHandleRunnable != null) {
                mHandleRunnable.status(Status.STATUS_DISCONNECT);
            }
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

    public boolean send(SocketAddress address, Message data) {
        if (mSendRunnable != null) {
            data.setRemoteAddress(address);
            mSendRunnable.send(data);
            return true;
        }
        return false;
    }
}
