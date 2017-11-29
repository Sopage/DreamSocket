package com.dream.socket;

import com.dream.socket.codec.Message;
import com.dream.socket.codec.MessageCodec;
import com.dream.socket.codec.MessageHandle;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class TCPClient {


    public static void main(String[] args) {
        DreamTCPSocket socket = new DreamTCPSocket("localhost", 6969);
        socket.codec(new MessageCodec() {
            @Override
            public Message decode(SocketAddress address, ByteBuffer buffer) {
                int limit = buffer.limit();
                if (limit < 4) {
                    return null;
                }
                int len = buffer.getInt();
                if (buffer.remaining() >= len) {
                    byte[] array = new byte[len];
                    buffer.get(array);
                    return new StringMessage(array);
                }
                return null;
            }

            @Override
            public void encode(Message message, ByteBuffer buffer) {

            }
        });
        socket.handle(new MessageHandle() {
            @Override
            public void onStatus(int status) {

            }

            @Override
            public void onMessage(Message message) {

            }
        });
        socket.start();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        socket.stop();
    }
}
