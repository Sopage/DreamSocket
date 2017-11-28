package com.dream.socket.codec;

import java.net.SocketAddress;

public abstract class Message {
    private SocketAddress mRemoteAddress;

    public void setRemoteAddress(SocketAddress remoteAddress) {
        this.mRemoteAddress = remoteAddress;
    }

    public SocketAddress getRemoteAddress() {
        return mRemoteAddress;
    }
}
