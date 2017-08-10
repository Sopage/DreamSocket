package com.dream.socket;

import com.dream.socket.codec.Codec;
import com.dream.socket.codec.Decode;
import com.dream.socket.codec.Encode;
import com.dream.socket.codec.Handle;
import com.dream.socket.protocol.Protocol;

import java.nio.ByteBuffer;

public class UDPClient extends Codec<String, String> implements Handle<String> {

    public static void main(String[] args) {
        UDPClient client = new UDPClient();
        DreamDatagramSocket socket = new DreamDatagramSocket();
        socket.setAddress("127.0.0.1", 6969);
        socket.setCodec(client);
        socket.setHandle(client);
        socket.start();
        for(int i=1; i<11; i++){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            socket.send("String -> "+i);
        }
    }

    @Override
    public Decode<String> getDecode() {
        return new Decode<String>() {
            @Override
            public String decode(ByteBuffer buffer) {
                int limit = buffer.limit();
                if(limit < Protocol.HEADER_LENGTH){
                    return null;
                }
                char start = (char)buffer.get();
                byte version = buffer.get();
                int length = buffer.getInt();//包的总长度 包括头
                buffer.get(Protocol.RETAIN);
                char xy = (char)buffer.get();
                if(limit < length){
                    return null;
                }
                byte[] bytes = new byte[length - Protocol.HEADER_LENGTH];
                buffer.get(bytes);
                char end = (char)buffer.get();
                System.out.println(new String(bytes));
                return new String(bytes);
            }
        };
    }

    @Override
    public Encode<String> getEncode() {
        return null;
    }

    @Override
    public void onStatus(int status) {

    }

    @Override
    public void onReceive(String data) {

    }
}
