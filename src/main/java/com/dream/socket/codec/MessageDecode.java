package com.dream.socket.codec;

import com.dream.socket.runnable.HandleRunnable;

import java.nio.ByteBuffer;

public abstract class MessageDecode<T> {

    private static int CACHE_BUFFER_LENGTH = 102400;
    private final ByteBuffer mBuffer = ByteBuffer.allocate(CACHE_BUFFER_LENGTH);
    private HandleRunnable<T> mHandleRunnable;

    public MessageDecode() {
        mBuffer.flip();
    }

    public void setHandleRunnable(HandleRunnable<T> handleRunnable) {
        this.mHandleRunnable = handleRunnable;
    }

    public synchronized void put(byte[] array, int offset, int length) {
        if (mBuffer.limit() + length > mBuffer.capacity()) {
            //TODO 缓存区已满，丢弃读取的数据
            return;
        }

        mBuffer.compact();
        mBuffer.put(array, offset, length);
        mBuffer.flip();
        mBuffer.mark();

        T data;
        while (mBuffer.hasRemaining() && ((data = decode(mBuffer)) != null)) {
            mHandleRunnable.put(data);
            mBuffer.compact();
            mBuffer.flip();
            mBuffer.mark();
        }
        mBuffer.reset();
    }

    protected abstract T decode(ByteBuffer buffer);

}
