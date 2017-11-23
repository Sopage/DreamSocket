package com.dream.socket.test;

import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class TestByteBuffer {

    public static void main(String[] args) throws Exception {
        ServerSocket ss = new ServerSocket(6969);
        while (true) {
            Socket socket = ss.accept();
            OutputStream os = socket.getOutputStream();

            ByteBuffer buffer = ByteBuffer.allocate(100);
            byte[] text = ("message1").getBytes();
            buffer.putInt(text.length);
            buffer.put(text);
            text = ("message2").getBytes();
            buffer.putInt(text.length);
            buffer.put(text);
            text = ("message3").getBytes();
            buffer.putInt(text.length);
            buffer.put(text);
            buffer.flip();
            os.write(buffer.array(), buffer.position(), buffer.limit());
            os.flush();

//            for (int i = 0; i < 10; i++) {
//                ByteBuffer buffer = ByteBuffer.allocate(100);
//                byte[] text = ("message1" + i).getBytes();
//
//                buffer.putInt(text.length * 3);
//                buffer.flip();
//                os.write(buffer.array(), buffer.position(), buffer.limit());
//                os.flush();
//                Thread.sleep(2000);
//
//                buffer.clear();
//                buffer.put(text);
//                buffer.flip();
//                os.write(buffer.array(), buffer.position(), buffer.limit());
//                os.flush();
//                Thread.sleep(2000);
//
//                buffer.clear();
//                buffer.put(("message2" + i).getBytes());
//                buffer.flip();
//                os.write(buffer.array(), buffer.position(), buffer.limit());
//                os.flush();
//                Thread.sleep(2000);
//
//                buffer.clear();
//                buffer.put(("message3" + i).getBytes());
//                buffer.flip();
//                os.write(buffer.array(), buffer.position(), buffer.limit());
//                os.flush();
//            }

        }
    }

}
