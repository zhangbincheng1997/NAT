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
    private boolean register = false;

    // 不需要channelActive

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        if (!register) {
            if (message.getType() == MessageType.REGISTER) {
                processRegister(ctx, message);
            } else {
                log.info("未注册端口...");
                ctx.close();
                return;
            }
        }
        if (message.getType() == MessageType.DISCONNECTED) {
            log.info("断开连接：客户端->服务器->代理服务器 {}", message.getChannelId());
            channels.close(channel ->
                    channel.id().asLongText().equals(message.getChannelId()));
        } else if (message.getType() == MessageType.DATA) {
            // 10000 -> 8888
            log.info("接受数据：客户端->服务器->代理服务器 {}", message.getChannelId());
            channels.writeAndFlush(message.getData(), channel ->
                    channel.id().asLongText().equals(message.getChannelId()));
        } else if (message.getType() == MessageType.KEEPALIVE) {
            log.info("心跳检测成功...");
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("断开连接：服务器->客户端 {}", ctx.channel().id());
        remoteServer.close();
    }

    private void processRegister(ChannelHandlerContext ctx, Message message) {
        int port = Utils.byteArrayToInt(message.getData()); // port
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
            register = true;

            Message sendBackMessage = new Message();
            sendBackMessage.setType(MessageType.REGISTER_RESULT);
            sendBackMessage.setChannelId("0000");
            sendBackMessage.setData("success".getBytes());
            ctx.writeAndFlush(sendBackMessage);

            log.info("注册成功！客户端{}, 监听端口{}", clientAddress, port);
            return;
        } catch (Exception e) {
            log.error("客户端注册失败，端口占用");
            Message sendBackMessage = new Message();
            sendBackMessage.setType(MessageType.REGISTER_RESULT);
            sendBackMessage.setChannelId("0000");
            sendBackMessage.setData("failure".getBytes());
            ctx.writeAndFlush(sendBackMessage);
        }
        ctx.close();
    }

    // 心跳检测
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            // IdleState.READER_IDLE 一段时间内没有数据接收
            // IdleState.WRITER_IDLE 一段时间内没有数据发送
            if (e.state() == IdleState.READER_IDLE) {
                log.info("一段时间内没有数据接收{}", ctx.channel().id());
                ctx.close();
            } else if (e.state() == IdleState.WRITER_IDLE) {
                log.info("心跳检测...");
                Message message = new Message();
                message.setType(MessageType.KEEPALIVE);
                message.setChannelId("0000");
                message.setData(null);
                ctx.writeAndFlush(message);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("捕获异常...", cause);
    }
}
