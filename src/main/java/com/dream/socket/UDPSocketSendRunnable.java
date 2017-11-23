package com.dream.socket;

import com.dream.socket.codec.DataProtocol;
import com.dream.socket.codec.MessageEncode;
import com.dream.socket.runnable.SendRunnable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

public class UDPSocketSendRunnable<T extends DataProtocol> extends SendRunnable<T> {

    private DatagramSocket mSocket;

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
                mSocket.send(new DatagramPacket(buffer, offset, length, address));
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
