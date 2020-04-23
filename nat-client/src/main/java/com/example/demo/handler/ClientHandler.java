package com.example.demo.handler;

import com.example.demo.MainForm;
import com.example.demo.protocol.Utils;
import io.netty.channel.Channel;
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
import com.example.demo.net.TcpClient;
import com.example.demo.protocol.Message;
import com.example.demo.protocol.MessageType;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<Message> {

    private TcpClient localConnection = new TcpClient();
    private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    // 代理客户端映射
    private ConcurrentHashMap<String, Channel> channelMap = new ConcurrentHashMap<>();
    private int proxy;
    private String localHost;
    private int localPort;

    public ClientHandler(int proxy, String localHost, int localPort) {
        this.proxy = proxy;
        this.localHost = localHost;
        this.localPort = localPort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("注册端口...");
        Message message = Message.of(MessageType.REGISTER, "", Utils.intToByteArray(proxy));
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("与服务端断开连接...");
        MainForm.getInstance().showMessage("与服务端断开连接...");
        MainForm.getInstance().restart();
        channels.close();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        switch (message.getType()) {
            case UNKNOWN:
                log.error("消息错误...");
                ctx.close();
                break;
            case REGISTER:
                break;
            case REGISTER_SUCCESS:
                log.info("注册成功！");
                MainForm.getInstance().showMessage("注册成功！");
                MainForm.getInstance().stop();
                break;
            case REGISTER_FAILURE:
                log.error("注册失败，端口占用！");
                MainForm.getInstance().showMessage("注册失败，端口占用！");
                MainForm.getInstance().restart();
                ctx.close();
                break;
            case CONNECTED:
                processConnected(ctx, message);
                break;
            case DISCONNECTED:
                processDisconnected(message);
                break;
            case DATA:
                processData(message);
                break;
            case KEEPALIVE:
                break;
        }
    }

    private void processConnected(ChannelHandlerContext ctx, Message message) {
        String channelId = message.getChannelId();
        try {
            localConnection.connect(localHost, localPort, new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(
                            new ByteArrayDecoder(),
                            new ByteArrayEncoder(),
                            new LocalProxyHandler(ctx, channelId));
                    channels.add(ch);
                    channelMap.put(channelId, ch);
                    log.info("建立连接成功：{}", channelId);
                    MainForm.getInstance().showMessage("建立连接成功：" + channelId);
                }
            });
        } catch (Exception e) {
            log.error("本地端口无效：{}", localPort);
            MainForm.getInstance().showMessage("本地端口无效：" + localPort);
            MainForm.getInstance().restart();
            ctx.close();
        }
    }

    private void processDisconnected(Message message) {
        String channelId = message.getChannelId();
        Channel channel = channelMap.get(channelId);
        if (channel != null) {
            channel.close();
            channelMap.remove(channelId);
            log.info("关闭连接...");
            MainForm.getInstance().showMessage("关闭连接...");
        }
    }

    private void processData(Message message) {
        String channelId = message.getChannelId();
        Channel channel = channelMap.get(channelId);
        if (channel != null) {
            channel.writeAndFlush(message.getData());
            log.info("转发数据...");
            MainForm.getInstance().showMessage("转发数据...");
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            // 客户端负责发送心跳包
            if (e.state() == IdleState.WRITER_IDLE) {
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
