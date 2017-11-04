package com.dream.socket;

import com.dream.socket.codec.Encode;
import com.dream.socket.config.Config;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class SendRunnable<T> implements Runnable {

    private boolean sending;
    private Encode<T> encode;
    private ByteBuffer buffer = ByteBuffer.allocate(102400);
    private LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>();

    public SendRunnable(Encode<T> encode) {
        this.encode = encode;
    }

    @Override
    public void run() {
        synchronized (this) {
            sending = true;
            queue.clear();
            Config.getConfig().getLogger().debug("发送线程 -> 开启");
            try {
                while (sending) {
                    T data = queue.take();
                    if (!sending) {
                        continue;
                    }
                    buffer.clear();
                    encode.encode(data, buffer);
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
//        this.send(new Object());
    }

    public boolean send(T data) {
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
