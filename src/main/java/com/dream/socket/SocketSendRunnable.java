package com.dream.socket;

import com.dream.socket.config.Config;

import java.io.OutputStream;

public final class SocketSendRunnable extends SendRunnable {

    private OutputStream out;

    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    protected void doSend(byte[] buffer, int offset, int length) {
        if (out != null) {
            try {
                out.write(buffer, offset, length);
                out.flush();
            } catch (Exception e) {
                Config.getConfig().getLogger().error("发送数据异常！", e);
            }
        } else {
            Config.getConfig().getLogger().warn("发送管道为NULL！");
        }
    }

    @Override
    public void stop() {
        out = null;
        super.stop();
    }
}
