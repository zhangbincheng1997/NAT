package com.example.demo.handler;

import com.example.demo.net.TcpServer;
import com.example.demo.protocol.Utils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.GlobalEventExecutor;
import com.example.demo.protocol.Message;
import com.example.demo.protocol.MessageType;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class ServerHandler extends SimpleChannelInboundHandler<Message> {

    private TcpServer remoteServer = new TcpServer();
    private ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE); // 全局单例

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("与客户端连接...");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("与客户端断开连接...");
        remoteServer.close();
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
                log.info("心跳检测..."); // 服务端接收心跳包
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
                    channelGroup.add(ch); // 代理服务器Group
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

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            // 服务端负责心跳检测
            if (e.state() == IdleState.READER_IDLE) {
                log.info("一段时间内没有数据接收：{}", ctx.channel().id());
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("捕获异常...", cause);
        ctx.close();
    }
}
