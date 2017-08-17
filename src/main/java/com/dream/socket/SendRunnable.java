package com.dream.socket;

import com.dream.socket.codec.Codec;
import com.dream.socket.config.Config;
import com.dream.socket.listener.OnStartListener;

import java.nio.ByteBuffer;
import java.util.Vector;

public abstract class SendRunnable implements Runnable {
    private Vector<Object> vector = new Vector<>();
    private Codec codec;
    private boolean sending;
    private OnStartListener listener;

    private ByteBuffer buffer = ByteBuffer.allocate(102400);

    public void setCodec(Codec codec) {
        this.codec = codec;
    }

    public void setOnStartListener(OnStartListener listener){
        this.listener = listener;
    }

    @Override
    public void run() {
        synchronized (this) {
            sending = true;
            Config.getConfig().getLogger().debug("start 开启发送线程！");
            if(listener != null){
                listener.onStart(this);
            }
            while (sending) {
                if (vector.size() == 0) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        Config.getConfig().getLogger().error("发送线程等待异常！", e);
                    }
                }

                while (vector.size() > 0) {
                    Object data = vector.remove(0);
                    buffer.clear();
                    codec.getEncode().encode(data, buffer);
                    buffer.flip();
                    if (!doSend(buffer.array(), 0, buffer.limit())) {
                        Config.getConfig().getLogger().error("数据没有被真正发送出去！");
                    }
                }
            }
        }
        Config.getConfig().getLogger().debug("stop 结束发送线程！");
    }

    public void stop() {
        sending = false;
        synchronized (this) {
            this.notify();
        }
    }

    public boolean send(Object data) {
        if (!sending) {
            return false;
        }
        this.vector.add(data);
        synchronized (this) {
            this.notify();
        }
        return true;
    }

    protected abstract boolean doSend(byte[] buffer, int offset, int length);
}
