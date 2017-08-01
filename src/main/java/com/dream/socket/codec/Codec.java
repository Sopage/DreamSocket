package com.dream.socket.codec;

import java.nio.ByteBuffer;

/**
 * 编解码接口
 *
 * @param <D> 解码成的对象
 * @param <E> 要编码的对象
 */
public interface Codec<D, E> {

    /**
     * 解码方法
     * <b>禁止调用ByteBuffer类的mark()方法和reset()方法，建议只调用get相关方法操作</b>
     *
     * @param buffer 传入的ByteBuffer对象, 建议只调用get相关方法操作
     * @return 返回组装的类型
     */
    D decode(ByteBuffer buffer);


    /**
     * 编码方法
     *
     * @param data   要编码的对象
     * @param buffer 传入的ByteBuffer 建议只对ByteBuffer做put相关操作
     */
    void encode(E data, ByteBuffer buffer);
}
