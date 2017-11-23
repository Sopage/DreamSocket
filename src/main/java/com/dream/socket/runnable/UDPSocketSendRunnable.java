package com.dream.socket.runnable;

import com.dream.socket.codec.DataProtocol;
import com.dream.socket.codec.MessageEncode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

public class UDPSocketSendRunnable<T extends DataProtocol> extends SendRunnable<T> {

    private DatagramSocket mSocket;
    private DatagramPacket packet;
    public UDPSocketSendRunnable(MessageEncode<T> encode) {
        super(encode);
    }

    public void setDatagramSocket(DatagramSocket socket) {
        this.mSocket = socket;
    }

    @Override
    protected boolean doSend(SocketAddress address, byte[] buffer, int offset, int length) {
        if (mSocket != null) {
            try {
                if(packet == null){
                    packet = new DatagramPacket(buffer, buffer.length);
                }
                packet.setData(buffer, offset, length);
                packet.setSocketAddress(address);
                mSocket.send(packet);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
