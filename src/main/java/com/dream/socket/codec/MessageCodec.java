package com.dream.socket.codec;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

public interface MessageCodec<T extends Message> {

    T decode(SocketAddress address, ByteBuffer buffer);

    void encode(T message, ByteBuffer buffer);
}
