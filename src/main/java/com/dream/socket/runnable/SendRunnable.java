package com.dream.socket.runnable;

import com.dream.socket.codec.MessageEncode;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class SendRunnable<T> implements Runnable {

    private boolean sending;
    private MessageEncode<T> encode;
    private ByteBuffer buffer = ByteBuffer.allocate(102400);
    private LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>();

    public SendRunnable(MessageEncode<T> encode) {
        this.encode = encode;
    }

    @Override
    public void run() {
        synchronized (this) {
            sending = true;
            queue.clear();
            System.out.println("发送线程 -> 开启");
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
                        System.out.println("数据没有被真正发送出去！");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("发送线程 -> 结束");
    }

    public void stop() {
        sending = false;
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
