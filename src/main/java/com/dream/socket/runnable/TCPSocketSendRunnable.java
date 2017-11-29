package com.dream.socket.runnable;

import com.dream.socket.codec.MessageCodec;
import com.dream.socket.logger.LoggerFactory;

import java.io.OutputStream;
import java.net.SocketAddress;

public final class TCPSocketSendRunnable extends SendRunnable {

    private OutputStream mOutputStream;

    public TCPSocketSendRunnable(MessageCodec codec, OutputStream out) {
        super(codec);
        this.mOutputStream = out;
    }

    @Override
    protected boolean doSend(SocketAddress address, byte[] buffer, int offset, int length) {
        if (mOutputStream != null) {
            try {
                mOutputStream.write(buffer, offset, length);
                mOutputStream.flush();
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
