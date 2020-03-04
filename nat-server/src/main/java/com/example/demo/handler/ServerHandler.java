package com.example.demo.handler;

import com.example.demo.net.TcpServer;
import com.example.demo.exception.GlobalException;
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

import java.util.HashMap;

/**
 * Created by wucao on 2019/2/27.
 */
public class ServerHandler extends MessageHandler {

    private TcpServer remoteConnectionServer = new TcpServer();

    private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private String password;
    private int port;

    private boolean register = false;

    public ServerHandler(String password) {
        this.password = password;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        Message message = (Message) msg;
        if (message.getType() == MessageType.REGISTER) {
            processRegister(message);
        } else if (register) {
            if (message.getType() == MessageType.DISCONNECTED) {
                processDisconnected(message);
            } else if (message.getType() == MessageType.DATA) {
                processData(message);
            } else if (message.getType() == MessageType.KEEPALIVE) {
                // 心跳包, 不处理
            } else {
                throw new GlobalException("Unknown type: " + message.getType());
            }
        } else {
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        remoteConnectionServer.close();
        if (register) {
            System.out.println("Stop server on port: " + port);
        }
    }

    /**
     * if natxMessage.getType() == NatxMessageType.REGISTER
     */
    private void processRegister(Message message) {
        HashMap<String, Object> metaData = new HashMap<>();

        String password = message.getMetaData().get("password").toString();
        if (this.password != null && !this.password.equals(password)) {
            metaData.put("success", false);
            metaData.put("reason", "Token is wrong");
        } else {
            int port = (int) message.getMetaData().get("port");

            try {

                ServerHandler thisHandler = this;
                remoteConnectionServer.bind(port, new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ByteArrayDecoder(), new ByteArrayEncoder(), new RemoteProxyHandler(thisHandler));
                        channels.add(ch);
                    }
                });

                metaData.put("success", true);
                this.port = port;
                register = true;
                System.out.println("Register success, start server on port: " + port);
            } catch (Exception e) {
                metaData.put("success", false);
                metaData.put("reason", e.getMessage());
                e.printStackTrace();
            }
        }

        Message sendBackMessage = new Message();
        sendBackMessage.setType(MessageType.REGISTER_RESULT);
        sendBackMessage.setMetaData(metaData);
        ctx.writeAndFlush(sendBackMessage);

        if (!register) {
            System.out.println("Client register error: " + metaData.get("reason"));
            ctx.close();
        }
    }

    /**
     * if natxMessage.getType() == NatxMessageType.DATA
     */
    private void processData(Message message) {
        channels.writeAndFlush(message.getData(), channel -> channel.id().asLongText().equals(message.getMetaData().get("channelId")));
    }

    /**
     * if natxMessage.getType() == NatxMessageType.DISCONNECTED
     * @param message
     */
    private void processDisconnected(Message message) {
        channels.close(channel -> channel.id().asLongText().equals(message.getMetaData().get("channelId")));
    }
}
