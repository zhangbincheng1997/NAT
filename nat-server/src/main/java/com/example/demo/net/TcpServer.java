package com.example.demo.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class TcpServer {

    private Channel channel;

    public synchronized void bind(int port, ChannelInitializer channelInitializer) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // 负责处理连接的线程池
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // 负责读写数据的线程池
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(channelInitializer) // handler发生在初始化，childHandler发生在客户端连接成功
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            channel = b.bind(port).sync().channel();
            channel.closeFuture().addListener((ChannelFutureListener) future -> {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            });
        } catch (Exception e) {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            throw e;
        }
    }

    public synchronized void close() {
        if (channel != null) {
            channel.close();
        }
    }
}
