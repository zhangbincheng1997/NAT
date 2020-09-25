package com.example.demo.server.handler;

import com.example.demo.common.handler.ServerCommonHandler;
import com.example.demo.server.net.TcpServer;
import com.example.demo.common.protocol.Utils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.util.concurrent.GlobalEventExecutor;
import com.example.demo.common.protocol.Message;
import com.example.demo.common.protocol.MessageType;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class ServerHandler extends ServerCommonHandler<Message> {

    private TcpServer remoteServer = new TcpServer();
    private ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("与客户端连接...");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("与客户端断开连接...");
        remoteServer.close();
        channelGroup.close();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        switch (message.getType()) {
            case UNKNOWN:
                log.error("消息错误...");
                ctx.close();
                break;
            case REGISTER:
                processRegister(ctx, message);
                break;
            case CONNECTED:
                log.error("连接成功！");
                break;
            case DATA:
                log.error("转发成功！");
                channelGroup.writeAndFlush(message.getData(), channel -> channel.id().asLongText().equals(message.getChannelId()));
                break;
            case DISCONNECTED:
                log.error("请求关闭...");
                channelGroup.close(channel -> channel.id().asLongText().equals(message.getChannelId()));
                break;
            case KEEPALIVE:
                log.info("扑通扑通...");
                break;
        }
    }

    private void processRegister(ChannelHandlerContext ctx, Message message) {
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientAddress = socketAddress.getAddress().getHostAddress();
        int port = Utils.byteArrayToInt(message.getData());
        try {
            remoteServer.bind(port, new ChannelInitializer<SocketChannel>() { // 10000
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(
                            new ByteArrayDecoder(),
                            new ByteArrayEncoder(),
                            new RemoteProxyHandler(ctx)); // client <-> server
                    channelGroup.add(ch); // 代理服务端Group
                }
            });
            log.info("注册成功！客户端{}，监听端口：{}", clientAddress, port);
            Message result = Message.of(MessageType.REGISTER_SUCCESS);
            ctx.writeAndFlush(result);
        } catch (Exception e) {
            log.error("注册失败！客户端{}，端口占用：{}", clientAddress, port);
            Message result = Message.of(MessageType.REGISTER_FAILURE);
            ctx.writeAndFlush(result);
            ctx.close();
        }
    }
}
