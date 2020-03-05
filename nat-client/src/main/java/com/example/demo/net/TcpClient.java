package com.example.demo.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TcpClient {

    private Channel channel;

    public void connect(String host, int port, ChannelInitializer channelInitializer) {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(channelInitializer)
                    .option(ChannelOption.SO_KEEPALIVE, true);
            channel = b.connect(host, port).sync().channel();
            channel.closeFuture().addListener((ChannelFutureListener) future -> workerGroup.shutdownGracefully());
        } catch (Exception e) {
            workerGroup.shutdownGracefully();
            e.printStackTrace();
        }
    }

    public synchronized void close() {
        if (channel != null) {
            channel.close();
        }
    }
}