package com.dream.socket;

import com.dream.socket.codec.Codec;
import com.dream.socket.config.Config;
import com.dream.socket.listener.OnStartListener;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class SendRunnable implements Runnable {
    private Codec codec;
    private boolean sending;
    private OnStartListener listener;

    private ByteBuffer buffer = ByteBuffer.allocate(102400);
    private LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<>();

    public void setCodec(Codec codec) {
        this.codec = codec;
    }

    public void setOnStartListener(OnStartListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        synchronized (this) {
            sending = true;
            queue.clear();
            Config.getConfig().getLogger().debug("发送线程 -> 开启");
            if (listener != null) {
                listener.onStart(this);
            }
            try {
                while (sending) {
                    Object data = queue.take();
                    if (!sending) {
                        continue;
                    }
                    buffer.clear();
                    codec.getEncode().encode(data, buffer);
                    buffer.flip();
                    if (!doSend(buffer.array(), 0, buffer.limit())) {
                        Config.getConfig().getLogger().error("数据没有被真正发送出去！");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Config.getConfig().getLogger().debug("发送线程 -> 结束");
    }

    public void stop() {
        sending = false;
        this.send(new Object());
    }

    public boolean send(Object data) {
        try {
            this.queue.put(data);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    protected abstract boolean doSend(byte[] buffer, int offset, int length);
}
