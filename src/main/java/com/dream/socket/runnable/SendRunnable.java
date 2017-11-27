package com.dream.socket.runnable;

import com.dream.socket.codec.Message;
import com.dream.socket.codec.MessageEncode;
import com.dream.socket.logger.LoggerFactory;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class SendRunnable<T extends Message> implements Runnable {

    private MessageEncode<T> encode;
    private ByteBuffer buffer = ByteBuffer.allocate(102400);
    private LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<>();

    public SendRunnable(MessageEncode<T> encode) {
        this.encode = encode;
    }

    @Override
    public void run() {
        synchronized (this) {
            queue.clear();
            LoggerFactory.getLogger().info("开启 -> 发送线程");
            try {
                while (true) {
                    T data = queue.take();
                    buffer.clear();
                    encode.encode(data, buffer);
                    buffer.flip();
                    if (!doSend(data.mAddress, buffer.array(), 0, buffer.limit())) {
                        LoggerFactory.getLogger().error("数据没有被发送出去！");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LoggerFactory.getLogger().info("结束 -> 发送线程");
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

    protected abstract boolean doSend(SocketAddress address, byte[] buffer, int offset, int length);
}
