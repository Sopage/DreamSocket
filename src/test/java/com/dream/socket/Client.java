package com.dream.socket;

import com.dream.socket.codec.Codec;
import com.dream.socket.codec.Decode;
import com.dream.socket.codec.Encode;
import com.dream.socket.codec.Handle;
import com.dream.socket.protobuf.Protobuf;
import com.dream.socket.protocol.Protocol;

import java.nio.ByteBuffer;

public class Client extends Codec<String, String> implements Handle<String> {

    private byte[] buffer = new byte[102400];

    public static void main(String[] args) {
        Client client = new Client();
        DreamSocket socket = new DreamSocket();
        socket.isReadBuffer(true);
        socket.setAddress("127.0.0.1", 6969);
        socket.setHandle(client);
        socket.setCodec(client);
        socket.start();
        new Thread(()->{
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            socket.stop();
        }).start();
        for (int i = 1; i < 1000; i++) {
            try {
                socket.send("I am Client, The message index is " + i);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public Decode<String> getDecode() {
        return new Decode<String>() {
            @Override
            public String decode(ByteBuffer buffer) {
                int limit = buffer.limit();
                if (limit < Protocol.HEADER_LENGTH) {
                    return null;
                }
                char start = (char) buffer.get();
                byte version = buffer.get();
                int length = buffer.getInt();//包的总长度 包括头
                buffer.get(Protocol.RETAIN);
                char xy = (char) buffer.get();
                if (limit < length) {
                    return null;
                }
                byte[] bytes = new byte[length - Protocol.HEADER_LENGTH];
                buffer.get(bytes);
                char end = (char) buffer.get();
                return new String(bytes);
            }
        };
    }

    @Override
    public Encode<String> getEncode() {
        return new Encode<String>() {
            @Override
            public void encode(String data, ByteBuffer buffer) {
                Protobuf.Text text = Protobuf.Text.newBuilder().setText(data).build();
                Protobuf.Message.Builder builder = Protobuf.Message.newBuilder();
                builder.setId(String.valueOf(System.currentTimeMillis()));
                builder.setType(1);
                builder.setSender(666666);
                builder.setContent(text.getTextBytes());
                Protobuf.Message message = builder.build();
                byte[] array = message.toByteArray();
                buffer.put(Protocol.START_TAG);
                buffer.put(Protocol.VERSION);
                buffer.putInt(array.length + Protocol.HEADER_LENGTH);
                buffer.put(Protocol.RETAIN);
                buffer.put(Protocol.VERIFY_TAG);
                buffer.put(array);
                buffer.put(Protocol.END_TAG);
            }
        };
    }

    @Override
    public void onStatus(int status) {
        System.out.println("onStatus: " + status);
    }

    @Override
    public void onMessage(String data) {
        System.out.println("onMessage: " + data);
    }
}
