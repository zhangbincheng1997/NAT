package com.example.demo.handler;

import com.example.demo.exception.GlobalException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.util.concurrent.GlobalEventExecutor;
import com.example.demo.net.TcpConnection;
import com.example.demo.protocol.Message;
import com.example.demo.protocol.MessageType;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler extends MessageHandler {

    private int port;
    private String password;
    private String proxyAddress;
    private int proxyPort;

    private ConcurrentHashMap<String, MessageHandler> channelHandlerMap = new ConcurrentHashMap<>();
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public ClientHandler(int port, String password, String proxyAddress, int proxyPort) {
        this.port = port;
        this.password = password;
        this.proxyAddress = proxyAddress;
        this.proxyPort = proxyPort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        // register client information
        Message message = new Message();
        message.setType(MessageType.REGISTER);
        HashMap<String, Object> metaData = new HashMap<>();
        metaData.put("port", port);
        metaData.put("password", password);
        message.setMetaData(metaData);
        ctx.writeAndFlush(message);

        super.channelActive(ctx);
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
            // 心跳包, 不处理
        } else {
            throw new GlobalException("Unknown type: " + message.getType());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channelGroup.close();
        System.out.println("Loss connection to Natx server, Please restart!");
    }

    /**
     * if natxMessage.getType() == NatxMessageType.REGISTER_RESULT
     */
    private void processRegisterResult(Message message) {
        if ((Boolean) message.getMetaData().get("success")) {
            System.out.println("Register to Natx server");
        } else {
            System.out.println("Register fail: " + message.getMetaData().get("reason"));
            ctx.close();
        }
    }

    /**
     * if natxMessage.getType() == NatxMessageType.CONNECTED
     */
    private void processConnected(Message natxMessage) throws Exception {

        try {
            ClientHandler thisHandler = this;
            TcpConnection localConnection = new TcpConnection();
            localConnection.connect(proxyAddress, proxyPort, new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    LocalProxyHandler localProxyHandler = new LocalProxyHandler(thisHandler, natxMessage.getMetaData().get("channelId").toString());
                    ch.pipeline().addLast(new ByteArrayDecoder(), new ByteArrayEncoder(), localProxyHandler);

                    channelHandlerMap.put(natxMessage.getMetaData().get("channelId").toString(), localProxyHandler);
                    channelGroup.add(ch);
                }
            });
        } catch (Exception e) {
            Message message = new Message();
            message.setType(MessageType.DISCONNECTED);
            HashMap<String, Object> metaData = new HashMap<>();
            metaData.put("channelId", natxMessage.getMetaData().get("channelId"));
            message.setMetaData(metaData);
            ctx.writeAndFlush(message);
            channelHandlerMap.remove(natxMessage.getMetaData().get("channelId"));
            throw e;
        }
    }

    /**
     * if natxMessage.getType() == NatxMessageType.DISCONNECTED
     */
    private void processDisconnected(Message message) {
        String channelId = message.getMetaData().get("channelId").toString();
        MessageHandler handler = channelHandlerMap.get(channelId);
        if (handler != null) {
            handler.getCtx().close();
            channelHandlerMap.remove(channelId);
        }
    }

    /**
     * if natxMessage.getType() == NatxMessageType.DATA
     */
    private void processData(Message message) {
        String channelId = message.getMetaData().get("channelId").toString();
        MessageHandler handler = channelHandlerMap.get(channelId);
        if (handler != null) {
            ChannelHandlerContext ctx = handler.getCtx();
            ctx.writeAndFlush(message.getData());
        }
    }
}
