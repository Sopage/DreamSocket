package com.dream.socket;

import com.dream.socket.codec.MessageDecode;
import com.dream.socket.codec.MessageEncode;
import com.dream.socket.codec.MessageHandle;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class TCPClient {


    public static void main(String[] args) {
        DreamTCPSocket socket = new DreamTCPSocket("localhost", 6969);
        socket.codec(new MessageDecode<StringMessage>() {
            @Override
            protected StringMessage decode(SocketAddress address, ByteBuffer buffer) {
                int limit = buffer.limit();
                if(limit < 4){
                    return null;
                }
                int len = buffer.getInt();
                if(buffer.remaining() >= len){
                    byte[] array = new byte[len];
                    buffer.get(array);
                    return new StringMessage(array);
                }
                return null;
            }
        }, new MessageHandle<StringMessage>() {
            @Override
            public void onStatus(int status) {
                switch (status){
                    case Status.STATUS_CONNECTED:
                        System.out.println("socket connected");
                        break;
                    case Status.STATUS_DISCONNECT:
                        System.out.println("socket disconnect");
                        break;
                    case Status.STATUS_FAIL:
                        System.out.println("socket fail");
                        break;
                }
            }

            @Override
            public void onMessage(StringMessage data) {
                System.out.println(data.getString());
            }
        }, new MessageEncode<StringMessage>() {
            @Override
            public void encode(StringMessage data, ByteBuffer buffer) {
                buffer.put(data.getString().getBytes());
            }
        });
        socket.start();
    }
}
