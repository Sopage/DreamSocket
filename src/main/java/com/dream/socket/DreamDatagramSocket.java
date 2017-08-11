package com.dream.socket;

import com.dream.socket.codec.Handle;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DreamDatagramSocket extends DreamNetwork {

    private DatagramSocketSendRunnable send = new DatagramSocketSendRunnable();
    private DatagramSocket socket;

    public void setSendAddress(String host, int port) {
        send.changeAddress(host, port);
    }

    @Override
    protected void doStop() {
        if(socket != null){
            socket.close();
            socket = null;
            this.status(Handle.STATUS_DISCONNECT);
        }
    }

    @Override
    public boolean isConnected() {
        return socket != null && isRunning() && !socket.isClosed() && socket.isConnected();
    }

    @Override
    protected void doStartUp() {
        synchronized (this) {
            try {
                socket = new DatagramSocket();
                send.setDatagramSocket(socket);
                startHandler();
                startSend();
                final byte[] bytes = new byte[102400];
                DatagramPacket packet;
                while (isRunning()) {
                    packet = new DatagramPacket(bytes, bytes.length);
                    socket.receive(packet);
                    this.decode(packet.getData(), packet.getOffset(), packet.getLength());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public SendRunnable getSendRunnable() {
        return send;
    }

}
