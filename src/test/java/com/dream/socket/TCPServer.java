package com.dream.socket;

import com.dream.socket.protocol.Protocol;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class TCPServer {

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
                    byte[] body = String.format(text, i).getBytes();
                    byteBuffer.clear();

                    byteBuffer.put(Protocol.START_TAG);
                    byteBuffer.put(Protocol.VERSION);
                    byteBuffer.putInt(Protocol.HEADER_LENGTH + body.length);
                    byteBuffer.flip();
                    dos.write(byteBuffer.array(), 0, byteBuffer.limit());
                    dos.flush();
                    Thread.sleep(1000);

                    byteBuffer.clear();
                    byteBuffer.put(Protocol.RETAIN);
                    byteBuffer.flip();
                    dos.write(byteBuffer.array(), 0, byteBuffer.limit());
                    dos.flush();
                    Thread.sleep(1000);

                    byteBuffer.clear();
                    byteBuffer.put(Protocol.VERIFY_TAG);
                    byteBuffer.flip();
                    dos.write(byteBuffer.array(), 0, byteBuffer.limit());
                    dos.flush();
                    Thread.sleep(1000);

                    dos.write(body);
                    dos.flush();
                    Thread.sleep(1000);

                    byteBuffer.clear();
                    byteBuffer.put(Protocol.END_TAG);
                    byteBuffer.flip();
                    dos.write(byteBuffer.array(), 0, byteBuffer.limit());
                    dos.flush();

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
