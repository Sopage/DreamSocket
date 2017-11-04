package com.dream.socket;

public interface Status {

    /**
     * 连接成功
     */
    int STATUS_CONNECTED = 0;

    /**
     * 断开连接
     */
    int STATUS_DISCONNECT = 1;

    /**
     * 连接失败
     */
    int STATUS_FAIL = 2;

}
