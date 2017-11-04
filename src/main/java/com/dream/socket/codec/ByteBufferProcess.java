package com.dream.socket.codec;

import com.dream.socket.config.Config;

import java.nio.ByteBuffer;

public class ByteBufferProcess<T> extends ByteProcess {

    private static int CACHE_BUFFER_LENGTH = 102400;

    //解码需要操作的buffer
    private final ByteBuffer buffer = ByteBuffer.allocate(CACHE_BUFFER_LENGTH);
    //缓存没有被解码的缓冲区
    private final ByteBuffer cache = ByteBuffer.allocate(CACHE_BUFFER_LENGTH);

    public ByteBufferProcess(Decode<T> decode) {
        super(decode);
        //计算cache buffer数据相关信息
        cache.flip();
    }

    @Override
    protected boolean appendCache(byte[] bytes, int offset, int length) {
        Config.getConfig().getLogger().debug(String.format("1.收到数据-> 缓存{length=\"%d\"} 接收{length=\"%d\"}", cache.limit(), length));
        //把position下标设置到最后面用户继续往后拼接数据
        if (cache.limit() + length > cache.capacity()) {
            Config.getConfig().getLogger().error("解码缓存区已满！消息被丢弃");
            //TODO 缓存区已满，丢弃读取的数据
            return false;
        }
        cache.position(cache.limit());
        //重置limit的长度为缓存最大长度
        cache.limit(cache.capacity());
        //将读取到的数据放入缓存buffer
        cache.put(bytes, 0, length);
        //计算cache buffer数据相关信息
        cache.flip();
        Config.getConfig().getLogger().debug(String.format("2.合并数据-> 缓存{length=\"%d\"}", cache.limit()));
        return true;
    }

    @Override
    protected void decode() {
        //把缓存重新加入到buffer中进行解码
        buffer.put(cache.array(), cache.position(), cache.limit());
        //计算buffer数据相关信息
        buffer.flip();
        //先标记当前开始读取的点，用于后面不够解码后reset操作
        buffer.mark();
        Object data;
        //判断如果ByteBuffer后面有可读数据并且解码一次
        while (buffer.hasRemaining() && ((data = this.decode.decode(buffer)) != null)) {
            Config.getConfig().getLogger().debug(String.format("3.成功解码-> Buffer{剩余=\"%d\"}", buffer.remaining()));
            //把解码的数据回调给Handler
            handle.put(data);
            //再次判断ByteBuffer后面是否还有可读数据
            if (buffer.hasRemaining()) {
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
        Config.getConfig().getLogger().debug(String.format("4.剩余数据-> 缓存{length=\"%d\"}", cache.limit()));
    }

    @Override
    public synchronized boolean put(byte[] bytes, int offset, int length) {
        if (appendCache(bytes, offset, length)) {
            decode();
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        cache.clear();
        cache.flip();
    }
}
