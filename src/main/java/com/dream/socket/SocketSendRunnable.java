package com.dream.socket;

import com.dream.socket.codec.Encode;
import com.dream.socket.config.Config;

import java.io.OutputStream;
import java.nio.ByteBuffer;

public final class SocketSendRunnable<T> extends SendRunnable<T> {

    private OutputStream out;

    public SocketSendRunnable(Encode<T> encode) {
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
                Config.getConfig().getLogger().error("数据发送异常！", e);
            }
        } else {
            Config.getConfig().getLogger().warn("发送管道为NULL！");
        }
        return false;
    }

    @Override
    public void stop() {
        out = null;
        super.stop();
    }
}
