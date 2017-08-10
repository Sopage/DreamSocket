package com.dream.socket;

import com.dream.socket.codec.Codec;
import com.dream.socket.codec.Handle;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DreamDatagramSocket implements Runnable {

    private ExecutorService pool;
    private InetSocketAddress address;
    private String host;
    private int port;
    private Codec codec;
    private DatagramSocket socket;
    private boolean running;
    private HandleRunnable handleRunnable;

    public DreamDatagramSocket() {
        handleRunnable = new HandleRunnable();
    }

    public void connect(String host, int port) {
        this.host = host;
        this.port = port;
        this.address = null;
    }

    public <D, E> void setCodec(Codec<D, E> codec, Handle<D> handle) {
        this.codec = codec;
        this.handleRunnable.setHandle(handle);
    }

    public void start() {
        if (codec == null) {
            throw new NullPointerException("请设置编解码器");
        }
        if (handleRunnable.handle == null) {
            throw new NullPointerException("请设置消息处理器");
        }
        if (address == null) {
            throw new NullPointerException("请设置远程连接地址");
        }
        if (running) {
            return;
        }
        if (pool != null) {
            if (!pool.isShutdown()) {
                pool.shutdown();
            }
            pool = null;
        }
        pool = Executors.newFixedThreadPool(3);
        running = true;
        pool.execute(this);
    }

    private boolean isRunning() {
        return this.running;
    }

    @Override
    public void run() {
        synchronized (this) {
            try {
                socket = new DatagramSocket();
                pool.execute(handleRunnable);
                final ByteBuffer buffer = ByteBuffer.allocate(102400);
                final byte[] cache = new byte[buffer.capacity()];
                int cacheLength = 0;
                final byte[] swap = new byte[buffer.capacity()];
                final byte[] bytes = new byte[buffer.capacity()];
                DatagramPacket packet;
                while (isRunning()) {
                    packet = new DatagramPacket(bytes, bytes.length);
                    socket.receive(packet);
                    int readLength = packet.getLength();
                    if (cacheLength + readLength > cache.length) {
                        continue;
                    }
                    print("接收到数据, 上次遗留数据长度: cacheLength=" + cacheLength + "  接收的数据长度:  readLength=" + readLength);
                    System.arraycopy(packet.getData(), 0, cache, cacheLength, readLength);
                    cacheLength = cacheLength + readLength;
                    print("拼接遗留数据和读取数据放入缓存, 长度: cacheLength=" + cacheLength);
                    buffer.put(cache, 0, cacheLength);
                    buffer.flip();
                    buffer.mark();
                    Object data;
                    //判断如果ByteBuffer后面有可读数据并且解码一次
                    while (buffer.hasRemaining() && ((data = codec.decode(buffer)) != null)) {
                        print("成功解码一条数据");
                        //把解码的数据回调给Handler
                        handleRunnable.put(data);
                        //再次判断ByteBuffer后面是否还有可读数据
                        if (buffer.hasRemaining()) {
                            print("还有未解码数据");
                            //ByteBuffer剩余没有读取的数据长度
                            int remaining = buffer.remaining();
                            //ByteBuffer当前读取的位置
                            int position = buffer.position();
                            //拷贝缓存剩余长度的数据到交换缓冲区
                            System.arraycopy(cache, position, swap, 0, remaining);
                            //在把交换缓冲区的数据拷贝的缓存缓冲区用于下次解码
                            System.arraycopy(swap, 0, cache, 0, remaining);
                            //重置缓存缓冲区长度为剩余数据长度
                            cacheLength = remaining;
                            //再次清除重置解码的ByteBuffer
                            buffer.clear();
                            buffer.put(cache, 0, cacheLength);
                            //切换到读模式
                            buffer.flip();
                        }
                        //再次标记当前开始读取点
                        buffer.mark();
                        //上面解码完成后重置到make读取点
                        buffer.reset();
                        //判断是否还有数据可读
                        if (buffer.hasRemaining()) {
                            print("退出解码，还有未解码数据");
                            //剩余可读长度
                            int remaining = buffer.remaining();
                            //将剩余数据拷贝到缓存缓冲区
                            buffer.get(cache, 0, remaining);
                            //缓存数据长度为当前剩余数据长度
                            cacheLength = remaining;
                        } else {
                            //如果没有可读的数据 缓存数据长度为0
                            cacheLength = 0;
                        }
                        //清除重置解码的ByteBuffer
                        buffer.clear();
                        print("最后遗留数据长度: cacheLength=" + cacheLength + " content: " + new String(cache, 0, 100));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static final class HandleRunnable<D> implements Runnable {

        private Vector<D> vector = new Vector<>();
        private Handle<D> handle;
        private boolean running;

        private void setHandle(Handle<D> handle) {
            this.handle = handle;
        }

        @Override
        public void run() {
            synchronized (this) {
                running = true;
                while (running) {
                    if (vector.size() == 0) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    while (vector.size() > 0) {
                        D data = vector.remove(0);
                        if (handle != null) {
                            handle.onReceive(data);
                        }
                    }
                }
            }
        }

        private void put(D d) {
            vector.add(d);
            synchronized (this) {
                this.notify();
            }
        }

        private void status(int status) {
            if (handle != null) {
                handle.onStatus(status);
            }
        }

        private void stop() {
            running = false;
            synchronized (this) {
                this.notify();
            }
        }
    }

    private static void print(String text) {
        System.out.println(text);
    }

    private static void printError(String text) {
        System.err.println(text);
    }
}
