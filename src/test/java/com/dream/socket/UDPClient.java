package com.dream.socket;

import com.dream.socket.codec.MessageDecode;
import com.dream.socket.codec.MessageEncode;
import com.dream.socket.codec.MessageHandle;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class UDPClient {

    public static void main(String[] args) {
        DreamUDPSocket socket = new DreamUDPSocket();
        socket.codec(new MessageDecode<StringMessage>() {
            @Override
            protected StringMessage decode(SocketAddress address, ByteBuffer buffer) {
                int limit = buffer.limit();
                byte[] array = new byte[limit];
                buffer.get(array);
                return new StringMessage(array);
            }
        }, new MessageHandle<StringMessage>() {
            @Override
            public void onStatus(int status) {

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
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 1; i <= 10; i++) {
            socket.send(new InetSocketAddress("localhost", 6969), new StringMessage(("message 6969 -> " + i).getBytes()));
            socket.send(new InetSocketAddress("localhost", 6960), new StringMessage(("message 6960 -> " + i).getBytes()));
            socket.send(new InetSocketAddress("localhost", 6961), new StringMessage(("message 6961 -> " + i).getBytes()));
            socket.send(new InetSocketAddress("localhost", 6962), new StringMessage(("message 6962 -> " + i).getBytes()));
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        socket.stop();
    }
}
