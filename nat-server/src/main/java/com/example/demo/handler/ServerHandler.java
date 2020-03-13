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
    private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // NONE
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
                log.error("消息错误..."); // UNKNOWN
                ctx.close();
                break;
            case REGISTER:
                processRegister(ctx, Utils.byteArrayToInt(message.getData()));
                break;
            case REGISTER_SUCCESS:
            case REGISTER_FAILURE:
            case CONNECTED:
                break;
            case DISCONNECTED:
                log.info("断开连接...");
                channels.close(channel -> channel.id().asLongText().equals(message.getChannelId()));
                break;
            case DATA:
                log.info("传递数据...");
                channels.writeAndFlush(message.getData(), channel -> channel.id().asLongText().equals(message.getChannelId()));
                break;
            case KEEPALIVE:
                log.info("心跳检测成功...");
                break;
        }
    }

    private void processRegister(ChannelHandlerContext ctx, int port) {
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientAddress = socketAddress.getAddress().getHostAddress();
        try {
            remoteServer.bind(port, new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(
                            new ByteArrayDecoder(),
                            new ByteArrayEncoder(),
                            new RemoteProxyHandler(ctx));
                    channels.add(ch);
                }
            });
            Message message = Message.of(MessageType.REGISTER_SUCCESS);
            ctx.writeAndFlush(message);
            log.info("注册成功！客户端{}，监听端口：{}", clientAddress, port);
        } catch (Exception e) {
            Message message = Message.of(MessageType.REGISTER_FAILURE);
            ctx.writeAndFlush(message);
            ctx.close();
            log.error("注册失败！客户端{}，端口占用：{}", clientAddress, port);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                log.info("一段时间内没有数据接收：{}", ctx.channel().id());
                ctx.close();
            } else if (e.state() == IdleState.WRITER_IDLE) {
                log.info("心跳检测...");
                Message message = Message.of(MessageType.KEEPALIVE);
                ctx.writeAndFlush(message);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("捕获异常...", cause);
    }
}
