package com.dream.socket.runnable;

import com.dream.socket.codec.Message;
import com.dream.socket.codec.MessageEncode;
import com.dream.socket.logger.LoggerFactory;

import java.io.OutputStream;
import java.net.SocketAddress;

public final class TCPSocketSendRunnable<T extends Message> extends SendRunnable<T> {

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
                LoggerFactory.getLogger().error("数据发送异常！", e);
            }
        } else {
            LoggerFactory.getLogger().error("发送管道OutputStream为NULL！");
        }
        return false;
    }
}
