package com.dream.socket;

import com.dream.socket.codec.Codec;

import java.nio.ByteBuffer;
import java.util.Vector;

public abstract class SendRunnable implements Runnable {
    private Vector<Object> vector = new Vector<>();
    private Codec codec;
    private boolean sending;
    private ByteBuffer buffer = ByteBuffer.allocate(102400);

    public void setCodec(Codec codec) {
        this.codec = codec;
    }

    @Override
    public void run() {
        synchronized (this) {
            sending = true;
            while (sending) {
                if (vector.size() == 0) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                while (vector.size() > 0) {
                    Object data = vector.remove(0);
                    buffer.clear();
                    codec.getEncode().encode(data, buffer);
                    buffer.flip();
                    doSend(buffer.array(), 0, buffer.limit());
                }
            }
        }
    }

    public void stop() {
        sending = false;
        synchronized (this) {
            this.notify();
        }
    }

    public void send(Object data) {
        this.vector.add(data);
        synchronized (this) {
            this.notify();
        }
    }

    protected abstract void doSend(byte[] buffer, int offset, int length);
}
