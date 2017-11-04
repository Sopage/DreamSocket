package com.dream.socket.runnable;

import com.dream.socket.codec.MessageEncode;

import java.io.OutputStream;

public final class SocketSendRunnable<T> extends SendRunnable<T> {

    private OutputStream out;

    public SocketSendRunnable(MessageEncode<T> encode) {
        super(encode);
    }

    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    protected boolean doSend(byte[] buffer, int offset, int length) {
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
