package com.dream.socket;

import com.dream.socket.codec.Message;
import com.dream.socket.codec.MessageCodec;
import com.dream.socket.codec.MessageHandle;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class UDPClient {

    public static void main(String[] args) {
        DreamUDPSocket socket = new DreamUDPSocket();
        socket.codec(new MessageCodec() {
            @Override
            public Message decode(SocketAddress address, ByteBuffer buffer) {
                int limit = buffer.limit();
                byte[] array = new byte[limit];
                buffer.get(array);
                return new StringMessage(array);
            }

            @Override
            public void encode(Message message, ByteBuffer buffer) {
                if(message instanceof StringMessage){
                    buffer.put(((StringMessage) message).getString().getBytes());
                }
            }
        });
        socket.handle(new MessageHandle() {
            @Override
            public void onStatus(int status) {

            }

            @Override
            public void onMessage(Message message) {
                if(message instanceof StringMessage){
                    System.out.println(((StringMessage) message).getString());
                }
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
