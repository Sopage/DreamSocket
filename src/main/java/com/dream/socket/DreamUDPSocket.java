package com.dream.socket;

import com.dream.socket.codec.DataProtocol;
import com.dream.socket.codec.MessageDecode;
import com.dream.socket.codec.MessageEncode;
import com.dream.socket.codec.MessageHandle;
import com.dream.socket.runnable.HandleRunnable;
import com.dream.socket.runnable.UDPSocketSendRunnable;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class DreamUDPSocket extends DreamSocket {

    private DatagramSocket mSocket;
    private UDPSocketSendRunnable mSocketSendRunnable;
    private MessageDecode mMessageDecode;
    private HandleRunnable mHandleRunnable;

    public <T extends DataProtocol> void codec(MessageDecode<T> messageDecode, MessageHandle<T> messageHandle, MessageEncode<T> messageEncode) {
        this.mHandleRunnable = new HandleRunnable<>(messageHandle);
        messageDecode.setHandleRunnable(mHandleRunnable);
        this.mMessageDecode = messageDecode;
        this.mSocketSendRunnable = new UDPSocketSendRunnable<>(messageEncode);
    }

    @Override
    public boolean onStart() {
        synchronized (this) {
            try {
                mSocket = new DatagramSocket();
            } catch (Exception e) {
                e.printStackTrace();
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
                    mMessageDecode.put(packet.getSocketAddress(), packet.getData(), packet.getOffset(), packet.getLength());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    mSocket = null;
                    if (isRunning()) {
                        this.mHandleRunnable.status(Status.STATUS_FAIL);
                        System.out.println("6秒后尝试重连");
                        this.wait(6000);
                        System.out.println("开始重连");
                    }
                } catch (Exception ie) {
                    System.out.println("重连发生异常");
                }
            }

        }
        return false;
    }

    @Override
    public boolean onStop() {
        if (mSocketSendRunnable != null) {
            mSocketSendRunnable.stop();
        }
        if (this.mHandleRunnable != null) {
            this.mHandleRunnable.stop();
        }
        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
            mHandleRunnable.status(Status.STATUS_DISCONNECT);
        }
        return true;
    }

    @Override
    public boolean isConnected() {
        if (mSocket != null && !mSocket.isClosed() && mSocket.isConnected() && isRunning()) {
            return true;
        }
        return false;
    }

    public boolean send(String host, int port, DataProtocol data) {
        if (mSocketSendRunnable != null) {
            SocketAddress address = new InetSocketAddress(host, port);
            data.mAddress = address;
            mSocketSendRunnable.send(data);
            return true;
        }
        return false;
    }
}
