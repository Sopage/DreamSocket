package com.dream.socket;

import com.dream.socket.codec.Message;

public class StringMessage extends Message {
    private String string;

    public StringMessage(byte[] array) {
        string = new String(array);
    }

    public StringMessage(byte[] array, int i, int limit) {
        string = new String(array, i, limit);
    }

    public String getString() {
        return string;
    }
}
