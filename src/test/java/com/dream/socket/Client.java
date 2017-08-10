package com.dream.socket;

import com.dream.socket.codec.Codec;
import com.dream.socket.codec.Decode;
import com.dream.socket.codec.Encode;
import com.dream.socket.codec.Handle;

import java.nio.ByteBuffer;

public class Client extends Codec<String, String> implements Handle<String>{

    private byte[] buffer = new byte[102400];

    public static void main(String[] args) {
        Client client = new Client();
        DreamSocket socket = new DreamSocket(false);
        socket.setAddress("127.0.0.1", 6969);
        socket.setHandle(client);
        socket.setCodec(client);
        socket.start();
        new Thread(()->{
            try {
                Thread.sleep(10000);
                socket.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        for(int i=1; i<=10; i++){
            socket.send("I am Client, The message index is " + i);
        }
    }

    @Override
    public Decode<String> getDecode() {
        return new Decode<String>() {
            @Override
            public String decode(ByteBuffer buffer) {
                if(buffer.limit() < 5){
                    return null;
                }
                char c = (char) buffer.get();
                int len = buffer.getInt();
                if(buffer.remaining() >= len){
                    buffer.get(Client.this.buffer, 0, len);
                    System.out.println("decode: start=" + String.valueOf(c)+ " len=" + len);
                    return new String(Client.this.buffer, 0, len);
                }
                return null;
            }
        };
    }

    @Override
    public Encode<String> getEncode() {
        return new Encode<String>() {
            @Override
            public void encode(String data, ByteBuffer buffer) {
                buffer.put(data.getBytes());
            }
        };
    }

    @Override
    public void onStatus(int status) {
        System.out.println("onStatus: " + status);
    }

    @Override
    public void onReceive(String data) {
        System.out.println("onReceive: " + data);
    }
}
