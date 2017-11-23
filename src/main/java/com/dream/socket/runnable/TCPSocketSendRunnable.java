package com.dream.socket.runnable;

import com.dream.socket.codec.DataProtocol;
import com.dream.socket.codec.MessageEncode;

import java.io.OutputStream;
import java.net.SocketAddress;

public final class TCPSocketSendRunnable<T extends DataProtocol> extends SendRunnable<T> {

    private OutputStream out;

    public TCPSocketSendRunnable(MessageEncode<T> encode) {
        super(encode);
    }

    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    protected boolean doSend(SocketAddress address, byte[] buffer, int offset, int length) {
        if (out != null) {
            try {
                out.write(buffer, offset, length);
                out.flush();
                return true;
            } catch (Exception e) {
                System.err.println("数据发送异常！");
            }
        } else {
            System.out.println("发送管道为NULL！");
        }
        return false;
    }

    @Override
    public void stop() {
        out = null;
        super.stop();
    }
}