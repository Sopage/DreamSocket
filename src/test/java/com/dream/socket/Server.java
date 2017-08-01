package com.dream.socket;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class Server {

    public static void main(String[] args) {
        ServerSocket server = null;
        try {
            server = new ServerSocket(6969);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (server == null) {
            return;
        }
        while (true) {
            try {
                Socket socket = server.accept();
                new ReadThread(socket);
                new WriteThread(socket);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private static final class WriteThread extends Thread {
        private Socket socket;

        private WriteThread(Socket socket) {
            this.socket = socket;
            this.start();
        }

        @Override
        public void run() {
            try {
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                String text = "Hi, I am is Server, The message index is %d";
                ByteBuffer byteBuffer = ByteBuffer.allocate(102400);
                for (int i = 1; i <= 100; i++) {
                    byteBuffer.clear();
                    byte[] buffer = String.format(text, i).getBytes();
                    dos.write((byte) '>');
                    Thread.sleep(1000);
                    dos.writeInt(buffer.length);
                    Thread.sleep(1000);
                    dos.write(buffer, 0, buffer.length - 5);
                    Thread.sleep(1000);
                    dos.write(buffer, buffer.length - 5, 5);
                    Thread.sleep(1000);

                    byteBuffer.put((byte)'>');
                    byteBuffer.putInt(buffer.length);
                    byteBuffer.put(buffer);
                    byteBuffer.put((byte)'>');
                    byteBuffer.putInt(buffer.length);
                    byteBuffer.put(buffer);
                    byteBuffer.put((byte)'>');
                    byteBuffer.putInt(buffer.length);
                    byteBuffer.put(buffer);
                    byteBuffer.put((byte)'>');
                    byteBuffer.putInt(buffer.length);
                    byteBuffer.put(buffer);
                    byteBuffer.put((byte)'>');
                    byteBuffer.putInt(buffer.length);
                    byteBuffer.put(buffer);
                    byteBuffer.put((byte)'>');
                    byteBuffer.putInt(buffer.length);
                    byteBuffer.put(buffer);
                    byteBuffer.put((byte)'>');
                    byteBuffer.putInt(buffer.length);
                    byteBuffer.put(buffer);
                    byteBuffer.put((byte)'>');
                    byteBuffer.putInt(buffer.length);
                    byteBuffer.put(buffer);
                    byteBuffer.put((byte)'>');
                    byteBuffer.putInt(buffer.length);
                    byteBuffer.put(buffer);
                    byteBuffer.put((byte)'>');
                    byteBuffer.putInt(buffer.length);
                    byteBuffer.put(buffer);
                    byteBuffer.flip();
                    dos.write(byteBuffer.array(), 0, byteBuffer.limit());
                }
            } catch (Exception e) {
                System.err.println("client OutputStream Exception");
            }
        }
    }

    private static final class ReadThread extends Thread {

        private Socket socket;

        private ReadThread(Socket socket) {
            this.socket = socket;
            this.start();
        }

        @Override
        public void run() {
            try {
                InputStream is = socket.getInputStream();
                byte[] buffer = new byte[102400];
                int len;
                while ((len = is.read(buffer)) > 0) {
                    System.out.println("client: " + new String(buffer, 0, len));
                }
            } catch (IOException e) {
                System.err.println("client InputStream Exception");
            }
        }
    }

}
