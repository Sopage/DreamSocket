package com.dream.socket;

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
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() {
        out = null;
        super.stop();
    }
}
