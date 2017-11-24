package com.dream.socket;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {

    public static void main(String[] args) {
        ServerSocket server = null;
        try {
            server = new ServerSocket(6969);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (server == null) {
            return;
        }
        while (true) {
            Socket socket = null;
            try {
                socket = server.accept();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (socket == null) {
                continue;
            }
            try {
                OutputStream out = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(out);
                for (int i = 1; i <= 10; i++) {
                    byte[] array = ("Message -> " + i).getBytes();
                    dos.writeInt(array.length);
                    dos.flush();
                    Thread.sleep(1000);
                    dos.write(array);
                    dos.flush();
                    Thread.sleep(1000);
                }
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

}
