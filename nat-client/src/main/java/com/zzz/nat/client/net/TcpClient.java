package com.zzz.nat.client.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TcpClient {

    private Channel channel;

    public void connect(String host, int port, ChannelInitializer channelInitializer) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // 负责读写数据的线程池
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(channelInitializer) // handler发生在初始化，childHandler发生在客户端连接成功
                    .option(ChannelOption.SO_KEEPALIVE, true);
            channel = b.connect(host, port).sync().channel();
            channel.closeFuture().addListener((ChannelFutureListener) future -> {
                workerGroup.shutdownGracefully();
            });
        } catch (Exception e) {
            workerGroup.shutdownGracefully();
            throw e;
        }
    }

    public synchronized void close() {
        if (channel != null) {
            channel.close();
        }
    }
}