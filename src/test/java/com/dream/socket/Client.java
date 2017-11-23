package com.dream.socket;

import com.dream.socket.codec.DataProtocol;
import com.dream.socket.codec.MessageDecode;
import com.dream.socket.codec.MessageEncode;
import com.dream.socket.codec.MessageHandle;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class Client {

    private DreamTCPSocket socket;

    public static final class StringProtocol extends DataProtocol{

        String string;

        public StringProtocol(byte[] array) {
            string = new String(array);
        }

        public StringProtocol(byte[] array, int i, int limit) {
            string = new String(array, i, limit);
        }

        public String getString() {
            return string;
        }
    }

    public static void main(String[] args) {
        DreamUDPSocket socket = new DreamUDPSocket();
        socket.codec(new MessageDecode<StringProtocol>() {
            @Override
            protected StringProtocol decode(SocketAddress address, ByteBuffer buffer) {
                int limit = buffer.limit();
                byte[] array = new byte[limit];
                buffer.get(array);
                return new StringProtocol(array);
            }
        }, new MessageHandle<StringProtocol>() {
            @Override
            public void onStatus(int status) {

            }

            @Override
            public void onMessage(StringProtocol data) {
                System.out.println(data.getString());
            }
        }, new MessageEncode<StringProtocol>() {
            @Override
            public void encode(StringProtocol data, ByteBuffer buffer) {
                buffer.put(data.getString().getBytes());
            }
        });
        socket.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(int i=1; i<=10; i++){
            socket.send("localhost", 6969, new StringProtocol(("message -> " + i).getBytes()));
        }
//        StringBuilder writer = new StringBuilder();
//        writer.append("GET / HTTP/1.1\r\n");
//        writer.append("Host: www.oschina.net\r\n");
//        writer.append("Accept-Language: zh-cn\r\n");
//        writer.append("Connection: Keep-Alive\r\n");
//        writer.append("\r\n");
//        socket.send(writer.toString());
//
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        socket.stop();
    }
//
//    public void setSocket(DreamTCPSocket socket) {
//        this.socket = socket;
//    }
//
//    private Decode<Packet> decode;
//    private Encode<Packet> encode;
//
//    public Decode<Packet> getDecode() {
//        if (decode != null) {
//            return decode;
//        }
//        return decode = new Decode<Packet>() {
//            @Override
//            public Packet decode(ByteBuffer buffer) {
//                int limit = buffer.limit();
//                if (limit < Protocol.HEADER_LENGTH) {
//                    return null;
//                }
//                char start = (char) buffer.get();
//                byte version = buffer.get();
//                int length = buffer.getInt();//包的总长度 包括头
//                buffer.get(Protocol.RETAIN);
//                char xy = (char) buffer.get();
//                if (limit < length) {
//                    return null;
//                }
//                byte[] bytes = new byte[length - Protocol.HEADER_LENGTH];
//                buffer.get(bytes);
//                char end = (char) buffer.get();
//                return new Packet(bytes);
//            }
//        };
//    }
//
//    public Encode<Packet> getEncode() {
//        if (encode != null) {
//            return encode;
//        }
//        return encode = new Encode<Packet>() {
//            @Override
//            public void encode(Packet packet, ByteBuffer buffer) {
//                buffer.put(Protocol.START_TAG);
//                buffer.put(Protocol.VERSION);
//                buffer.putInt(packet.body.length + Protocol.HEADER_LENGTH);
//                buffer.put(Protocol.RETAIN);
//                buffer.put(Protocol.VERIFY_TAG);
//                buffer.put(packet.body);
//                buffer.put(Protocol.END_TAG);
//            }
//        };
//    }
//
//    public void onStatus(int status) {
//        if (status == Handle.STATUS_CONNECTED) {
//            socket.send(login());
//        }
//    }
//
//    public void onMessage(Packet data) {
//        try {
//            Protobuf.Body body = Protobuf.Body.parseFrom(data.body);
//            switch (body.getType()) {
//                case Type.BODY_ACK:
//                    System.out.println("onMessage: type=ack id=" + body.getId());
//                    break;
//                case Type.BODY_MESSAGE:
//                    Protobuf.Message message = Protobuf.Message.parseFrom(body.getContent());
//                    System.out.println("onMessage: type=message id=" + body.getId() + " message=" + message.getContent().toStringUtf8());
//                    socket.send(message("我已收到消息 id=" + body.getId()));
//                    break;
//                case Type.BODY_LOGIN:
//                    Protobuf.Response response = Protobuf.Response.parseFrom(body.getContent());
//                    System.out.println("onMessage: 登陆响应: " + response.getData().toStringUtf8());
//                    break;
//                default:
//                    break;
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    private Packet packet(int type, ByteString content) {
//        Protobuf.Body body = Protobuf.Body.newBuilder()
//                .setId(String.valueOf(System.currentTimeMillis()))
//                .setSender(1)
//                .setType(type)
//                .setContent(content).build();
//        return new Packet(body.toByteArray());
//    }
//
//    private Packet login() {
//        Protobuf.Login login = Protobuf.Login.newBuilder().setToke("toke1").build();
//        return packet(Type.BODY_LOGIN, login.toByteString());
//    }
//
//    private Packet message(String text) {
//        Protobuf.Message message = Protobuf.Message.newBuilder()
//                .setReceiver(101)
//                .setType(Type.MESSAGE_SINGLE)
//                .setContent(ByteString.copyFromUtf8(text)).build();
//        return packet(Type.BODY_MESSAGE, message.toByteString());
//    }
}
