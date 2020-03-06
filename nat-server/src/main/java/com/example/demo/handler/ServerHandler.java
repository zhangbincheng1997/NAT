package com.example.demo.handler;

import com.alibaba.fastjson.JSON;
import com.example.demo.net.TcpServer;
import com.example.demo.exception.GlobalException;
import com.example.demo.protocol.Utils;
import com.example.demo.web.entity.Blacklist;
import com.example.demo.web.service.BlacklistService;
import com.example.demo.web.service.NetworkService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.util.concurrent.GlobalEventExecutor;
import com.example.demo.protocol.Message;
import com.example.demo.protocol.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;

@Slf4j
public class ServerHandler extends MessageHandler {

    private TcpServer remoteServer = new TcpServer();
    private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private boolean register = false;

    @Autowired
    private BlacklistService blacklistService;
    @Autowired
    private NetworkService networkService;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Message message = (Message) msg;
        if (message.getType() == MessageType.REGISTER) {
            processRegister(message);
        } else if (register) {
            if (message.getType() == MessageType.DISCONNECTED) {
                channels.close(channel ->
                        channel.id().asLongText().equals(message.getChannelId()));
            } else if (message.getType() == MessageType.DATA) {
                channels.writeAndFlush(message.getData(), channel ->
                        channel.id().asLongText().equals(message.getChannelId()));
            } else if (message.getType() == MessageType.KEEPALIVE) {
                log.info("心跳检测成功...");
            }
        } else {
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        remoteServer.close();
    }

    private void processRegister(Message message) {
        int port = Utils.byteArrayToInt(message.getData()); // port
        System.out.println("端口："+port);
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientAddress = socketAddress.getAddress().getHostAddress();
//        if(blacklistService.findByHost(clientAddress)) {
//            log.info("黑名单：{}", clientAddress);
//            return;
//        }
        try {
            ServerHandler thisHandler = this;
            remoteServer.bind(port, new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(
                            new ByteArrayDecoder(),
                            new ByteArrayEncoder(),
                            new RemoteProxyHandler(thisHandler));
                    channels.add(ch);
                }
            });
            register = true;

            Message sendBackMessage = new Message();
            sendBackMessage.setType(MessageType.REGISTER_RESULT);
            sendBackMessage.setChannelId("0000");
            sendBackMessage.setData("success".getBytes());
            ctx.writeAndFlush(sendBackMessage);

            // TODO 获取客户端地址和端口
            log.info("注册成功！客户端{}, 监听端口{}", clientAddress, port);
            return;
        } catch (Exception e) {
            log.error("客户端注册失败");
            Message sendBackMessage = new Message();
            sendBackMessage.setType(MessageType.REGISTER_RESULT);
            sendBackMessage.setChannelId("0000");
            sendBackMessage.setData("failure".getBytes());
            e.printStackTrace();
        }
        ctx.close();
    }
}
