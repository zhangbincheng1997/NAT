package com.example.demo.handler;

import com.example.demo.protocol.Utils;
import io.netty.channel.*;
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

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<Message> {

    private TcpClient localConnection = new TcpClient();

    private int proxy;
    private String localAddress;
    private int localPort;

    // 保存本地连接（到web程序的channel）
    private ConcurrentHashMap<String, Channel> channelMap = new ConcurrentHashMap<>();
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public ClientHandler(int proxy, String localAddress, int localPort) {
        this.proxy = proxy;
        this.localAddress = localAddress;
        this.localPort = localPort;
    }

    // 建立连接 发起注册请求
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("发起连接请求"); // TODO
        Message message = new Message();
        message.setType(MessageType.REGISTER);
        message.setChannelId("0000");
        message.setData(Utils.intToByteArray(proxy));
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channelGroup.close();
        log.info("与服务器{}连接已经断开", ctx.channel().id()); // TODO
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        if (message.getType() == MessageType.REGISTER_RESULT) {
            processRegisterResult(ctx, message);
        } else if (message.getType() == MessageType.CONNECTED) {
            processConnected(ctx, message);
        } else if (message.getType() == MessageType.DISCONNECTED) {
            processDisconnected(message);
        } else if (message.getType() == MessageType.DATA) {
            processData(message);
        } else if (message.getType() == MessageType.KEEPALIVE) {
            log.info("心跳检测成功...");
        } else {
            log.error("消息错误"); // UNKNOWN
        }
    }

    private void processRegisterResult(ChannelHandlerContext ctx, Message message) {
        if (Arrays.equals(message.getData(), "success".getBytes())) {
            log.info("注册成功");
        } else {
            log.info("注册失败"); // TODO 可能是端口占用
            ctx.close();
        }
    }

    private void processConnected(ChannelHandlerContext ctx, Message message) throws Exception {
        String channelId = message.getChannelId();
        log.info("客户端收到的 代理服务器的信息：{}", channelId);
        try {
            // 建立内网通向程序的通道
            localConnection.connect(localAddress, localPort, new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(
                            new ByteArrayDecoder(),
                            new ByteArrayEncoder(),
                            new LocalProxyHandler(ctx.channel(), channelId));
                    channelMap.put(channelId, ch); // 添加内网服务映射
                    channelGroup.add(ch);
                }
            });
        } catch (Exception e) {
            log.error("本地端口{}无效", localPort);
            Message result = new Message();
            result.setType(MessageType.DISCONNECTED);
            result.setChannelId(channelId);
            result.setData(null);
            ctx.writeAndFlush(result);
            channelMap.remove(channelId);
        }
    }

    private void processDisconnected(Message message) {
        String channelId = message.getChannelId();
        log.info("断开连接：服务端->客户端->代理客户端 {}", channelId);
        Channel ctx = channelMap.get(channelId);
        if (ctx != null) {
            ctx.close();
            channelMap.remove(channelId);
        }
    }

    private void processData(Message message) {
        String channelId = message.getChannelId();
        System.out.println("====" + channelId);
        log.info("接受数据：服务端->客户端->代理客户端 {}", channelId);
        Channel ctx = channelMap.get(channelId);
        if (ctx != null) {
            ctx.writeAndFlush(message.getData());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("捕获异常...", cause);
    }
}
