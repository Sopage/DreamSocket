package com.dream.socket.codec;

import com.dream.socket.logger.LoggerFactory;
import com.dream.socket.runnable.HandleRunnable;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public abstract class MessageDecode<T extends Message> {

    private static final int CACHE_BUFFER_LENGTH = 102400;
    private HandleRunnable<T> mHandleRunnable;
    private Map<SocketAddress, ByteBuffer> mAddressByteBufferMap = new HashMap<>();

    public void setHandleRunnable(HandleRunnable<T> handleRunnable) {
        this.mHandleRunnable = handleRunnable;
    }

    public final void decode(SocketAddress address, byte[] array, int offset, int length) {
        ByteBuffer buffer = mAddressByteBufferMap.get(address);
        if (buffer == null) {
            buffer = ByteBuffer.allocate(CACHE_BUFFER_LENGTH);
            mAddressByteBufferMap.put(address, buffer);
            buffer.flip();
            LoggerFactory.getLogger().info("创建 " + address.toString() + " 数据缓冲区ByteBuffer");
        }
        LoggerFactory.getLogger().info(String.format("%s 上次未解码 position=%d limit=%d", address.toString(), buffer.position(), buffer.limit()));
        if (buffer.limit() + length > buffer.capacity()) {
            LoggerFactory.getLogger().warn(address.toString() + " -> decode缓存区已满，读取的数据被丢弃!!!!!");
            return;
        }
        buffer.compact();
        buffer.put(array, offset, length);
        buffer.flip();
        buffer.mark();
        LoggerFactory.getLogger().info(String.format("%s 合并未解码 position=%d limit=%d", address.toString(), buffer.position(), buffer.limit()));
        T data;
        LoggerFactory.getLogger().info(address.toString() + " 开始解码数据");
        while (buffer.hasRemaining() && ((data = decode(address, buffer)) != null)) {
            LoggerFactory.getLogger().info(address.toString() + " 成功解码一条数据");
            data.setRemoteAddress(address);
            mHandleRunnable.put(data);
            buffer.compact();
            buffer.flip();
            buffer.mark();
        }
        LoggerFactory.getLogger().info(address.toString() + " 退出解码");
        buffer.reset();
        LoggerFactory.getLogger().info(String.format("%s 未解码数据 position=%d limit=%d", address.toString(), buffer.position(), buffer.limit()));
    }

    protected abstract T decode(SocketAddress address, ByteBuffer buffer);

}
