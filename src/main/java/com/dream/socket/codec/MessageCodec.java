package com.dream.socket.codec;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

public interface MessageCodec {

    Message decode(SocketAddress address, ByteBuffer buffer);

    void encode(Message message, ByteBuffer buffer);
}
