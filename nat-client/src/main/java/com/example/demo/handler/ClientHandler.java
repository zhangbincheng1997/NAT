package com.example.demo.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.util.concurrent.GlobalEventExecutor;
import com.example.demo.net.TcpClient;
import com.example.demo.protocol.Message;
import com.example.demo.protocol.MessageType;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ClientHandler extends MessageHandler {

    private int proxy;
    private String localAddress;
    private int localPort;

    private ConcurrentHashMap<String, MessageHandler> channelHandlerMap = new ConcurrentHashMap<>();
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public ClientHandler(int proxy, String localAddress, int localPort) {
        this.proxy = proxy;
        this.localAddress = localAddress;
        this.localPort = localPort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Message message = new Message();
        message.setType(MessageType.REGISTER);
        HashMap<String, Object> metaData = new HashMap<>();
        metaData.put("port", proxy);
        message.setMetaData(metaData);
        ctx.writeAndFlush(message);
        super.channelActive(ctx); // set TODO
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channelGroup.close();
        log.error("连接断开，请重启");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Message message = (Message) msg;
        if (message.getType() == MessageType.REGISTER_RESULT) {
            processRegisterResult(message);
        } else if (message.getType() == MessageType.CONNECTED) {
            processConnected(message);
        } else if (message.getType() == MessageType.DISCONNECTED) {
            processDisconnected(message);
        } else if (message.getType() == MessageType.DATA) {
            processData(message);
        } else if (message.getType() == MessageType.KEEPALIVE) {
            log.info("心跳检测成功...");
        }
    }

    private void processRegisterResult(Message message) {
        if ((Boolean) message.getMetaData().get("success")) {
            log.info("注册成功");
        } else {
            log.info("注册失败：" + message.getMetaData().get("reason"));
            ctx.close();
        }
    }

    private void processConnected(Message message) throws Exception {
        String channelId = message.getMetaData().get("channelId").toString();
        try {
            ClientHandler thisHandler = this;
            TcpClient localConnection = new TcpClient();
            localConnection.connect(localAddress, localPort, new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    LocalProxyHandler localProxyHandler = new LocalProxyHandler(thisHandler, channelId);
                    ch.pipeline().addLast(
                            new ByteArrayDecoder(),
                            new ByteArrayEncoder(),
                            localProxyHandler);
                    channelHandlerMap.put(channelId, localProxyHandler);
                    channelGroup.add(ch);
                }
            });
        } catch (Exception e) {
            Message result = new Message();
            result.setType(MessageType.DISCONNECTED);
            HashMap<String, Object> metaData = new HashMap<>();
            metaData.put("channelId", channelId);
            result.setMetaData(metaData);
            ctx.writeAndFlush(result);
            channelHandlerMap.remove(channelId);
            throw e;
        }
    }

    private void processDisconnected(Message message) {
        String channelId = message.getMetaData().get("channelId").toString();
        MessageHandler handler = channelHandlerMap.get(channelId);
        if (handler != null) {
            handler.getCtx().close();
            channelHandlerMap.remove(channelId);
        }
    }

    private void processData(Message message) {
        String channelId = message.getMetaData().get("channelId").toString();
        MessageHandler handler = channelHandlerMap.get(channelId);
        if (handler != null) {
            ChannelHandlerContext ctx = handler.getCtx();
            ctx.writeAndFlush(message.getData());
        }
    }
}
