package com.dream.socket.codec;

import com.dream.socket.runnable.HandleRunnable;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public abstract class MessageDecode<T extends DataProtocol> {

    private static int CACHE_BUFFER_LENGTH = 102400;
    private HandleRunnable<T> mHandleRunnable;
    private Map<SocketAddress, ByteBuffer> mAddressByteBufferMap = new HashMap<>();

    public void setHandleRunnable(HandleRunnable<T> handleRunnable) {
        this.mHandleRunnable = handleRunnable;
    }

    public synchronized void put(SocketAddress address, byte[] array, int offset, int length) {
        ByteBuffer buffer = mAddressByteBufferMap.get(address);
        if (buffer == null) {
            buffer = ByteBuffer.allocate(CACHE_BUFFER_LENGTH);
            mAddressByteBufferMap.put(address, buffer);
            buffer.flip();
        }
        if (buffer.limit() + length > buffer.capacity()) {
            //TODO 缓存区已满，丢弃读取的数据
            return;
        }

        buffer.compact();
        buffer.put(array, offset, length);
        buffer.flip();
        buffer.mark();

        T data;
        while (buffer.hasRemaining() && ((data = decode(address, buffer)) != null)) {
            data.mAddress = address;
            mHandleRunnable.put(data);
            buffer.compact();
            buffer.flip();
            buffer.mark();
        }
        buffer.reset();
    }

    protected abstract T decode(SocketAddress address, ByteBuffer buffer);

}
