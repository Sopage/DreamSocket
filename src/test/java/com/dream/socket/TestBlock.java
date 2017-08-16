package com.dream.socket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class TestBlock {

    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newCachedThreadPool();
        B b = new B();
        pool.execute(b);
        for(int i=1; i<=10; i++){
            b.add(String.valueOf(i));
            Thread.sleep(500);
        }
        pool.shutdownNow();
        for(int i=1; i<=10; i++){
            b.add(String.valueOf(i));
            Thread.sleep(500);
        }
    }

    private static class B implements Runnable{
        LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();

        @Override
        public void run() {
            while (true){
                try {
                    String s = queue.take();
                    System.out.println(s);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        public void add(String s){
            try {
                queue.put(s);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
