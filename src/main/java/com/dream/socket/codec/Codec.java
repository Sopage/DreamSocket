package com.dream.socket.codec;

/**
 * 编解码
 *
 * @param <D> 解码成的对象
 * @param <E> 要编码的对象
 */
public abstract class Codec<D, E> {

    public abstract Decode<D> getDecode();

    public abstract Encode<E> getEncode();
}
