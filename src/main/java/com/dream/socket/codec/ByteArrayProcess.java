package com.dream.socket.codec;

import java.nio.ByteBuffer;

public class ByteArrayProcess extends ByteProcess {

    private static int CACHE_BUFFER_LENGTH = 102400;

    /**
     * 缓存没有被解码的缓冲区
     */
    private final byte[] cache = new byte[CACHE_BUFFER_LENGTH];
    /**
     * 解码需要操作的buffer
     */
    private final ByteBuffer buffer = ByteBuffer.allocate(CACHE_BUFFER_LENGTH);

    /**
     * 用于和缓存缓冲区交换用的缓冲区
     */
    private final byte[] swap = new byte[buffer.capacity()];

    /**
     * 缓存的长度
     */
    private int cacheLength = 0;

    @Override
    protected boolean appendCache(byte[] bytes, int offset, int length) {
        if (cacheLength + length > cache.length) {
            //TODO 缓存区已满，丢弃读取的数据
            return false;
        }
        print("接收到数据, 上次遗留数据长度: cacheLength=" + cacheLength + "  接收的数据长度:  readLength=" + length);
        //把读取到的数据拷贝到上次缓存缓冲区的后面
        System.arraycopy(bytes, offset, cache, cacheLength, length);
        //缓存长度=上次的缓存长度+读取的数据长度
        cacheLength = cacheLength + length;
        print("拼接遗留数据和读取数据放入缓存, 长度: cacheLength=" + cacheLength);
        return true;
    }

    @Override
    protected void decode() {
        //把缓存放入buffer中解码
        buffer.put(cache, 0, cacheLength);
        //计算buffer并切换到读模式
        buffer.flip();
        //先标记当前开始读取的点，用于后面不够解码后reset操作
        buffer.mark();
        Object data;
        //判断如果ByteBuffer后面有可读数据并且解码一次
        while (buffer.hasRemaining() && ((data = codec.getDecode().decode(buffer)) != null)) {
            print("成功解码一条数据");
            //把解码的数据回调给Handler
            handle.put(data);
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

    @Override
    public boolean put(byte[] bytes, int length) {
        return this.put(bytes, 0, length);
    }

    @Override
    public boolean put(byte[] bytes, int offset, int length) {
        if (appendCache(bytes, offset, length)) {
            decode();
            return true;
        }
        return false;
    }


}
