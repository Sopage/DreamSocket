package com.dream.socket.runnable;

import com.dream.socket.codec.Message;
import com.dream.socket.codec.MessageCodec;
import com.dream.socket.logger.LoggerFactory;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class SendRunnable implements Runnable {

    private MessageCodec mCodec;
    private ByteBuffer mBuffer = ByteBuffer.allocate(102400);
    private LinkedBlockingQueue<Message> mQueue = new LinkedBlockingQueue<>();
    private boolean isSend = false;

    public SendRunnable(MessageCodec codec) {
        this.mCodec = codec;
    }

    @Override
    public void run() {
        LoggerFactory.getLogger().info("开启 -> 发送线程");
        isSend = true;
        while (isSend) {
            try {
                sending();
            } catch (Exception e) {
                LoggerFactory.getLogger().error("异常 -> 发送线程异常退出", e);
            }
        }
        LoggerFactory.getLogger().info("结束 -> 发送线程");
    }

    private void sending() throws Exception {
        while (true) {
            Message data = mQueue.take();
            mBuffer.clear();
            mCodec.encode(data, mBuffer);
            mBuffer.flip();
            if (!doSend(data.getRemoteAddress(), mBuffer.array(), 0, mBuffer.limit())) {
                LoggerFactory.getLogger().error("数据没有被发送出去！");
            }
        }
    }

    public boolean send(Message data) {
        try {
            this.mQueue.put(data);
            return true;
        } catch (Exception e) {
            LoggerFactory.getLogger().error("异常 -> 发送线程 LinkedBlockingQueue.put() 异常", e);
        }
        return false;
    }

    public void stop() {
        isSend = false;
    }

    protected abstract boolean doSend(SocketAddress remoteAddress, byte[] buffer, int offset, int length);
}
