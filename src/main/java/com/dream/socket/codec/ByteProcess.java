package com.dream.socket.codec;

import com.dream.socket.HandleRunnable;

public abstract class ByteProcess {

    protected Codec codec;
    protected HandleRunnable handle;

    public void setCodec(Codec codec) {
        this.codec = codec;
    }

    public void setHandle(HandleRunnable handle) {
        this.handle = handle;
    }

    protected abstract boolean appendCache(byte[] bytes, int offset, int length);

    protected abstract void decode();

    public abstract boolean put(byte[] bytes, int offset, int length);

    public abstract void reset();

    public boolean codecIsNull() {
        return codec == null;
    }
}
