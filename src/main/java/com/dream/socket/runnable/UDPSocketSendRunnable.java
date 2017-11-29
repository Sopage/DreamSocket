package com.dream.socket.runnable;

import com.dream.socket.codec.MessageCodec;
import com.dream.socket.logger.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

public class UDPSocketSendRunnable extends SendRunnable {

    private DatagramSocket mSocket;
    private DatagramPacket packet;

    public UDPSocketSendRunnable(MessageCodec codec, DatagramSocket socket) {
        super(codec);
        this.mSocket = socket;
    }

    @Override
    protected boolean doSend(SocketAddress address, byte[] buffer, int offset, int length) {
        if (mSocket != null) {
            try {
                if (packet == null) {
                    packet = new DatagramPacket(buffer, buffer.length);
                }
                packet.setData(buffer, offset, length);
                packet.setSocketAddress(address);
                mSocket.send(packet);
                return true;
            } catch (IOException e) {
                LoggerFactory.getLogger().error("数据发送异常！", e);
            }
        } else {
            LoggerFactory.getLogger().error("发送管道DatagramSocket为NULL！");
        }
        return false;
    }
}
