package com.dream.socket;

import java.net.InetSocketAddress;

public class TestAddress {

    public static void main(String[] args) {
        InetSocketAddress address1 = new InetSocketAddress("127.0.0.1", 6969);
        InetSocketAddress address2 = new InetSocketAddress("127.0.0.1", 6969);
        System.out.println(address1.equals(address2));
        System.out.println(address1.hashCode());
        System.out.println(address2.hashCode());
    }

}
