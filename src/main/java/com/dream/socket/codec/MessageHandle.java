package com.dream.socket.codec;

public interface MessageHandle<T extends Message> {

    /**
     * 连接状态回调
     *
     * @param status 状态
     */
    void onStatus(int status);

    /**
     * 消息回调
     * 解码后的消息会回调onReceive方法
     * 请在这里做业务相关的处理，建议单独开个线程处理
     *
     * @param message 消息
     */
    void onMessage(T message);
}
