package com.dream.socket;

import com.dream.socket.codec.Codec;
import com.dream.socket.codec.Decode;
import com.dream.socket.codec.Encode;
import com.dream.socket.codec.Handle;
import com.dream.socket.protobuf.Protobuf;
import com.dream.socket.protobuf.Type;
import com.dream.socket.protocol.Packet;
import com.dream.socket.protocol.Protocol;
import com.google.protobuf.ByteString;

import java.nio.ByteBuffer;

public class Client extends Codec<Packet, Packet> implements Handle<Packet> {

    private DreamSocket socket;

    public static void main(String[] args) {
        Client client = new Client();
        DreamSocket socket = new DreamSocket();
        client.setSocket(socket);
        socket.isReadBuffer(true);
        socket.setAddress("localhost", 6969);
        socket.setHandle(client);
        socket.setCodec(client);
        socket.start();
//        new Thread(()->{
//            try {
//                Thread.sleep(10000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            socket.stop();
//        }).start();
        for (int i = 1; i < 10; i++) {
            try {
                Thread.sleep(100);
                socket.send(client.message("I am user 1, index=" + i));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void setSocket(DreamSocket socket) {
        this.socket = socket;
    }

    private Decode<Packet> decode;
    private Encode<Packet> encode;

    @Override
    public Decode<Packet> getDecode() {
        if (decode != null) {
            return decode;
        }
        return decode = new Decode<Packet>() {
            @Override
            public Packet decode(ByteBuffer buffer) {
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
                return new Packet(bytes);
            }
        };
    }

    @Override
    public Encode<Packet> getEncode() {
        if (encode != null) {
            return encode;
        }
        return encode = new Encode<Packet>() {
            @Override
            public void encode(Packet packet, ByteBuffer buffer) {
                buffer.put(Protocol.START_TAG);
                buffer.put(Protocol.VERSION);
                buffer.putInt(packet.body.length + Protocol.HEADER_LENGTH);
                buffer.put(Protocol.RETAIN);
                buffer.put(Protocol.VERIFY_TAG);
                buffer.put(packet.body);
                buffer.put(Protocol.END_TAG);
            }
        };
    }

    @Override
    public void onStatus(int status) {
        if (status == Handle.STATUS_CONNECTED) {
            System.out.println("-----> status=" + status);
            socket.send(login());
        }
    }

    @Override
    public void onMessage(Packet data) {
        try {
            Protobuf.Body body = Protobuf.Body.parseFrom(data.body);
            switch (body.getType()) {
                case Type.BODY_ACK:
                    System.out.println("onMessage: type=ack id=" + body.getId());
                    break;
                case Type.BODY_MESSAGE:
                    Protobuf.Message message = Protobuf.Message.parseFrom(body.getContent());
                    System.out.println("onMessage: type=message id=" + body.getId() + " message=" + message.getContent().toStringUtf8());
                    break;
                case Type.BODY_LOGIN:
                    Protobuf.Response response = Protobuf.Response.parseFrom(body.getContent());
                    System.out.println("onMessage: 登陆响应: " + response.getData().toStringUtf8());
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Packet packet(int type, ByteString content) {
        Protobuf.Body body = Protobuf.Body.newBuilder()
                .setId(String.valueOf(System.currentTimeMillis()))
                .setSender(1)
                .setType(type)
                .setContent(content).build();
        return new Packet(body.toByteArray());
    }

    private Packet login() {
        Protobuf.Login login = Protobuf.Login.newBuilder().setToke("toke1").build();
        return packet(Type.BODY_LOGIN, login.toByteString());
    }

    private Packet message(String text) {
        Protobuf.Message message = Protobuf.Message.newBuilder()
                .setReceiver(2)
                .setType(Type.MESSAGE_SINGLE)
                .setContent(ByteString.copyFromUtf8(text)).build();
        return packet(Type.BODY_MESSAGE, message.toByteString());
    }
}
