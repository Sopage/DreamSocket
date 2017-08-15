package com.dream.socket.protobuf;

/**
 * 消息类型
 */
public interface Type {

    /**
     * 应答
     */
    byte BODY_ACK = -1;

    /**
     * 登录
     */
    byte BODY_MESSAGE = 0;

    /**
     * 登录
     */
    byte BODY_LOGIN = 1;

    /**
     * 退出
     */
    byte BODY_LOGOUT = 2;

    /**
     * 推送消息
     */
    byte BODY_PUSH = 3;

    /**
     * 单向消息(单聊消息)
     */
    byte MESSAGE_SINGLE = 1;

    /**
     * 多向消息(群消息)
     */
    byte MESSAGE_GROUP = 2;

}
