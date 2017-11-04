package com.dream.socket.codec;

import com.dream.socket.HandleRunnable;

public abstract class ByteProcess<T> {

    protected Decode<T> decode;
    protected HandleRunnable handle;

    public ByteProcess(Decode<T> decode) {
        this.decode = decode;
    }

    public void setHandle(HandleRunnable handle) {
        this.handle = handle;
    }

    protected abstract boolean appendCache(byte[] bytes, int offset, int length);

    protected abstract void decode();

    public abstract boolean put(byte[] bytes, int offset, int length);

    public abstract void reset();

}
