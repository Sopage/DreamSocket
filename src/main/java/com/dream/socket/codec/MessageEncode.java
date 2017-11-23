package com.dream.socket.codec;

import java.nio.ByteBuffer;

public abstract class MessageEncode<T extends DataProtocol> {


    /**
     * 编码方法
     *
     * @param data   要编码的对象
     * @param buffer 传入的ByteBuffer 建议只对ByteBuffer做put相关操作
     */
    public abstract void encode(T data, ByteBuffer buffer);

}
