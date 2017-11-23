package com.dream.socket;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServer {

    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(6969);
        DatagramPacket packet = new DatagramPacket(new byte[500], 500);
        while (true){
            socket.receive(packet);
            System.out.println(new String(packet.getData(), 0, packet.getLength()));
            socket.send(packet);
        }
    }

}
