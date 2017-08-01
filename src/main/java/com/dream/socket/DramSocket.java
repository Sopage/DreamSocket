package com.dream.socket;

import com.dream.socket.codec.Codec;
import com.dream.socket.codec.Handle;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DramSocket implements Runnable {
    private InetSocketAddress address;
    private ExecutorService pool;
    private Socket socket;
    private WriteRunnable writeRunnable;
    private Codec codec;
    private Handle handle;
    private boolean running;

    public DramSocket() {
        writeRunnable = new WriteRunnable();
    }

    public void connect(String host, int port) {
        this.address = new InetSocketAddress(host, port);
    }

    public void connect(InetSocketAddress address) {
        this.address = address;
    }

    public <D, E> void setCodec(Codec<D, E> codec, Handle<D> handle) {
        this.codec = codec;
        this.handle = handle;
        writeRunnable.setCodec(this.codec);
    }

    public void start() {
        if (codec == null) {
            throw new NullPointerException("请设置编解码器");
        }
        if (handle == null) {
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
        pool = Executors.newFixedThreadPool(2);
        running = true;
        pool.execute(this);
    }

    private boolean isRunning() {
        return this.running;
    }

    public boolean isConnected() {
        if (socket != null && !socket.isClosed() && socket.isConnected() && isRunning()) {
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        synchronized (this) {
            while (isRunning()) {
                try {
                    socket = new Socket();
                    socket.connect(address);
                    if (socket.isConnected()) {
                        writeRunnable.setOutputStream(socket.getOutputStream());
                        pool.execute(writeRunnable);
                        handle.onStatus(Handle.STATUS_CONNECTED);
                        readByBuffer(socket, codec, handle);//阻塞方法
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        handle.onStatus(Handle.STATUS_FAIL);
                        socket = null;
                        if (isRunning()) {
                            this.wait(6000);
                        }
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }
        }
    }

    public void send(Object data) {
        if (writeRunnable != null) {
            writeRunnable.send(data);
        }
    }

    public void stop() {
        running = false;
        if (writeRunnable != null) {
            writeRunnable.stop();
            writeRunnable = null;
        }
        if (socket != null) {
            shutdownInput(socket);
            shutdownOutput(socket);
            close(socket);
            socket = null;
            if (handle != null) {
                handle.onStatus(Handle.STATUS_DISCONNECT);
            }
        }
        if (pool != null && !pool.isShutdown()) {
            pool.shutdown();
        }

    }

    private static <D, E> void readByBytes(Socket socket, Codec<D, E> codec, Handle handle) throws Exception {
        //解码需要操作的buffer
        final ByteBuffer buffer = ByteBuffer.allocate(102400);
        //读取的缓冲区
        byte[] bytes = new byte[buffer.capacity()];
        //缓存没有被解码的缓冲区
        byte[] cache = new byte[buffer.capacity()];
        //用于和缓存缓冲区交换用的缓冲区
        byte[] swap = new byte[buffer.capacity()];
        //缓存的长度
        int cacheLength = 0;
        //读取到的数据长度
        int readLength;
        InputStream in = socket.getInputStream();
        while ((readLength = in.read(bytes)) > 0) {
            int length;
            if ((length = (cacheLength + readLength)) > cache.length) {
                //缓存区已满，丢弃读取的数据
                continue;
            }
            print("接收到数据, 上次遗留数据长度: cacheLength=" + cacheLength + "  接收的数据长度:  readLength=" + readLength);
            //把读取到的数据拷贝到上次缓存缓冲区的后面
            System.arraycopy(bytes, 0, cache, cacheLength, readLength);
            //缓存长度=上次的缓存长度+读取的数据长度
            cacheLength = length;
            print("拼接遗留数据和读取数据放入缓存, 长度: cacheLength=" + cacheLength);
            //把缓存放入buffer中解码
            buffer.put(cache, 0, cacheLength);
            //切换到读模式
            buffer.flip();
            //先标记当前开始读取的点，用于后面不够解码后reset操作
            buffer.mark();

            D data;
            //判断如果ByteBuffer后面有可读数据并且解码一次
            while (buffer.hasRemaining() && ((data = codec.decode(buffer)) != null)) {
                print("成功解码一条数据");
                //把解码的数据回调给Handler
                handle.onReceive(data);
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
            }
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

    private static <D, E> void readByBuffer(Socket socket, Codec<D, E> codec, Handle handle) throws Exception {
        //解码需要操作的buffer
        final ByteBuffer buffer = ByteBuffer.allocate(102400);
        //缓存没有被解码的缓冲区
        final ByteBuffer cache = ByteBuffer.allocate(102400);
        //计算cache buffer数据相关信息
        cache.flip();
        //读取的缓冲区
        byte[] bytes = new byte[buffer.capacity()];
        //读取到的数据长度
        int readLength;
        InputStream in = socket.getInputStream();
        while ((readLength = in.read(bytes)) > 0) {
            //把position下标设置到最后面用户继续往后拼接数据
            if (cache.limit() + readLength > cache.capacity()) {
                //缓存区已满，丢弃读取的数据
                continue;
            }
            print("接收到数据, 上次遗留数据长度: cacheLength=" + cache.limit() + "  接收的数据长度:  readLength=" + readLength);
            cache.position(cache.limit());
            //重置limit的长度为缓存最大长度
            cache.limit(cache.capacity());
            //将读取到的数据放入缓存buffer
            cache.put(bytes, 0, readLength);
            //计算cache buffer数据相关信息
            cache.flip();
            print("拼接遗留数据和读取数据放入缓存, 长度: cacheLength=" + cache.limit());
            //把缓存重新加入到buffer中进行解码
            buffer.put(cache.array(), cache.position(), cache.limit());
            //计算buffer数据相关信息
            buffer.flip();
            //先标记当前开始读取的点，用于后面不够解码后reset操作
            buffer.mark();

            D data;
            //判断如果ByteBuffer后面有可读数据并且解码一次
            while (buffer.hasRemaining() && ((data = codec.decode(buffer)) != null)) {
                print("成功解码一条数据");
                //把解码的数据回调给Handler
                handle.onReceive(data);
                //再次判断ByteBuffer后面是否还有可读数据
                if (buffer.hasRemaining()) {
                    print("还有未解码数据");
                    //清除重置cache ByteBuffer
                    cache.clear();
                    //把剩余buffer中的数据放置到缓存buffer中
                    cache.put(buffer.array(), buffer.position(), buffer.remaining());
                    //再次计算cache buffer数据相关信息
                    cache.flip();
                    //再次清除重置解码的ByteBuffer
                    buffer.clear();
                    //再次把缓存重新加入到buffer中进行解码
                    buffer.put(cache.array(), cache.position(), cache.limit());
                    //计算buffer数据相关信息
                    buffer.flip();
                }
                //再次标记当前开始读取点
                buffer.mark();
            }
            //上面解码完成后重置到make读取点
            buffer.reset();
            //判断是否还有数据可读
            if (buffer.hasRemaining()) {
                print("退出解码，还有未解码数据");
                //清除重置cache ByteBuffer
                cache.clear();
                //把缓存重新加入到buffer中进行解码
                cache.put(buffer.array(), buffer.position(), buffer.limit());
            } else {
                //清除重置cache ByteBuffer
                cache.clear();
            }
            //计算cache buffer数据相关信息
            cache.flip();
            //清除重置解码的ByteBuffer
            buffer.clear();
            print("最后遗留数据长度: cacheLength=" + cache.limit() + " content: " + new String(cache.array(), 0, 100));
        }
    }

    public static final class WriteRunnable<D, E> implements Runnable {
        private Vector<E> vector = new Vector<>();
        private Codec<D, E> codec;
        private OutputStream out;
        private boolean sending;
        private ByteBuffer buffer = ByteBuffer.allocate(102400);

        public void setCodec(Codec<D, E> codec) {
            this.codec = codec;
        }

        private void setOutputStream(OutputStream stream) {
            this.out = stream;
        }

        @Override
        public void run() {
            synchronized (this) {
                sending = true;
                while (sending) {
                    if (vector.size() == 0) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    while (vector.size() > 0) {
                        E data = vector.remove(0);
                        buffer.clear();
                        codec.encode(data, buffer);
                        buffer.flip();
                        if (out != null) {
                            try {
                                out.write(buffer.array(), 0, buffer.limit());
                                out.flush();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        private void stop() {
            sending = false;
            this.out = null;
            synchronized (this) {
                this.notify();
            }
        }

        public void send(E data) {
            this.vector.add(data);
            synchronized (this) {
                this.notify();
            }
        }
    }

    private static void shutdownInput(Socket socket) {
        if (socket != null && !socket.isInputShutdown()) {
            try {
                socket.shutdownInput();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void shutdownOutput(Socket socket) {
        if (socket != null && !socket.isOutputShutdown()) {
            try {
                socket.shutdownOutput();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void close(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void print(String text) {
        System.out.println(text);
    }
}