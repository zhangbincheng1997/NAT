package com.example.demo.server;

import com.example.demo.common.codec.MessageDecoder;
import com.example.demo.common.codec.MessageEncoder;
import com.example.demo.server.handler.ServerHandler;
import com.example.demo.server.net.TcpServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {

    public static void main(String[] args) {
        int port = 8888;
        if (args.length != 0) port = Integer.parseInt(args[0]);

        TcpServer server = new TcpServer();
        try {
            server.bind(port, new ChannelInitializer<SocketChannel>() { // 8888
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(
                            // 拆包粘包
                            new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4),
                            new MessageDecoder(),
                            new MessageEncoder(),
                            // 心跳检测
                            new IdleStateHandler(60, 0, 0),
                            new ServerHandler());
                }
            });
            log.info("启动服务器：" + port);
        } catch (Exception e) {
            log.error("无法启动服务器");
        }
    }
}
