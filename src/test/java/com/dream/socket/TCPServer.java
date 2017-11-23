package com.dream.socket;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(6969);
        while (true){
            Socket socket = server.accept();
            OutputStream out = socket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(out);
            for(int i=1; i<=10; i++){
                byte[] array = ("Message -> " + i).getBytes();
                dos.writeInt(array.length);
                dos.flush();
                Thread.sleep(1000);
                dos.write(array);
                dos.flush();
            }
            socket.close();
        }
    }

}
