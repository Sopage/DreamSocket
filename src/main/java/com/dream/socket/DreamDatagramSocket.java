package com.dream.socket;

import com.dream.socket.codec.Handle;
import com.dream.socket.config.Config;

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
        if (socket != null) {
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
                this.startSendAndHandler(this);
                final byte[] bytes = new byte[102400];
                DatagramPacket packet;
                while (isRunning()) {
                    packet = new DatagramPacket(bytes, bytes.length);
                    socket.receive(packet);
                    this.decode(packet.getData(), packet.getOffset(), packet.getLength());
                }
            } catch (Exception e) {
                Config.getConfig().getLogger().error("接收数据错误", e);
            } finally {
                try {
                    socket.disconnect();
                    socket.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
                socket = null;
                this.stopSendAndHandler();
                if (isRunning()) {
                    this.status(Handle.STATUS_FAIL);
                    try {
                        Config.getConfig().getLogger().warn("6秒好重新开启等待");
                        this.wait(6000);
                        Config.getConfig().getLogger().info("重新开启等待");
                    } catch (Exception e) {
                        Config.getConfig().getLogger().error("接收数据错误", e);
                    }
                }
            }
        }
    }

    @Override
    public SendRunnable getSendRunnable() {
        return send;
    }

    @Override
    public void onStart(Runnable runnable) {
        if (runnable instanceof SendRunnable && isConnected()) {
        } else if (runnable instanceof HandleRunnable && isConnected()) {

        }
    }

}
