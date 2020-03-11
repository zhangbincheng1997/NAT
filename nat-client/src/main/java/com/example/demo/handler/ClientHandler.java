package com.example.demo.handler;

import com.example.demo.MainForm;
import com.example.demo.protocol.Utils;
import io.netty.channel.*;
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
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    // client to local net
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
        log.info("注册端口......");
        Message message = Message.of(MessageType.REGISTER, "", Utils.intToByteArray(proxy));
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("与服务器断开连接：{}", ctx.channel().id());
        ctx.close();
        channelGroup.close();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        switch (message.getType()) {
            case UNKNOWN:
                log.error("消息错误"); // UNKNOWN
                break;
            case REGISTER:
                break;
            case REGISTER_SUCCESS:
                MainForm.getInstance().showMessage("注册成功");
                log.info("注册成功");
                break;
            case REGISTER_FAILURE:
                MainForm.getInstance().showMessage("注册失败，端口占用");
                log.info("注册失败，端口占用");
                ctx.close();
                break;
            case CONNECTED:
                processConnected(ctx, message.getChannelId());
                break;
            case DISCONNECTED:
                String channelId = message.getChannelId();
                log.info("客户端->内网：断开连接 {}", channelId);
                MainForm.getInstance().showMessage("客户端->内网：断开连接"+channelId);
                channelMap.get(channelId).close();
                channelMap.remove(channelId);
                break;
            case DATA:
                channelId = message.getChannelId();
                log.info("客户端->内网：传递数据 {}", channelId);
                MainForm.getInstance().showMessage("客户端->内网：传递数据"+channelId);
                channelMap.get(channelId).writeAndFlush(message.getData());
                break;
            case KEEPALIVE:
                log.info("心跳检测成功...");
                MainForm.getInstance().showMessage("心跳检测成功......");
                break;
        }
    }

    private void processConnected(ChannelHandlerContext ctx, String channelId) {
        log.info("客户端收到的服务器的信息：{}", channelId);
        try {
            // client to local net
            localConnection.connect(localHost, localPort, new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(
                            new ByteArrayDecoder(),
                            new ByteArrayEncoder(),
                            new LocalProxyHandler(ctx.channel(), channelId));
                    channelMap.put(channelId, ch);
                    channelGroup.add(ch);
                }
            });
        } catch (Exception e) {
            log.error("本地端口无效：{}", localPort);
            Message message = Message.of(MessageType.DISCONNECTED, channelId);
            ctx.writeAndFlush(message);
            channelMap.remove(channelId);
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
