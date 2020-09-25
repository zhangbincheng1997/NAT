package com.zzz.nat.client.handler;

import com.zzz.nat.client.MainForm;
import com.zzz.nat.common.handler.ClientCommonHandler;
import com.zzz.nat.common.protocol.Utils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import com.zzz.nat.client.net.TcpClient;
import com.zzz.nat.common.protocol.Message;
import com.zzz.nat.common.protocol.MessageType;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ClientHandler extends ClientCommonHandler<Message> {

    private TcpClient localConnection = new TcpClient();
    private ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    // 代理映射
    public static ConcurrentHashMap<String, Channel> channelMap = new ConcurrentHashMap<>();
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
        MainForm.getInstance().showMessage("注册端口...");
        Message message = Message.of(MessageType.REGISTER, "", Utils.intToByteArray(proxy));
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("与服务端断开连接...");
        MainForm.getInstance().showMessage("与服务端断开连接...");
        MainForm.getInstance().start();
        localConnection.close();
        channelGroup.close();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        switch (message.getType()) {
            case UNKNOWN:
                log.error("消息错误...");
                MainForm.getInstance().showMessage("消息错误...");
                ctx.close();
                break;
            case REGISTER_SUCCESS:
                log.info("注册成功！");
                MainForm.getInstance().showMessage("注册成功！");
                MainForm.getInstance().stop();
                break;
            case REGISTER_FAILURE:
                log.error("注册失败，端口占用！");
                MainForm.getInstance().showMessage("注册失败，端口占用！");
                MainForm.getInstance().start();
                break;
            case CONNECTED:
                log.error("请求连接...");
                MainForm.getInstance().showMessage("请求连接...");
                processConnected(ctx, message);
                break;
            case DATA:
                log.error("请求转发...");
                MainForm.getInstance().showMessage("请求转发...");
                processData(message);
                break;
            case DISCONNECTED:
                log.info("关闭成功！");
                MainForm.getInstance().showMessage("关闭成功！");
                processDisconnected(message);
                break;
        }
    }

    private void processConnected(ChannelHandlerContext ctx, Message message) {
        String remoteProxyChannelId = message.getChannelId();
        try {
            localConnection.connect(localHost, localPort, new ChannelInitializer<SocketChannel>() { // 8080
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(
                            new ByteArrayDecoder(),
                            new ByteArrayEncoder(),
                            new LocalProxyHandler(ctx, remoteProxyChannelId)); // client <-> server
                    channelGroup.add(ch); // 代理客户端Group
                    channelMap.put(remoteProxyChannelId, ch); // 代理服务端ID和代理客户端Channel的映射
                }
            });
        } catch (Exception e) {
            log.error("本地端口无效：" + localPort);
            MainForm.getInstance().showMessage("本地端口无效：" + localPort);
            MainForm.getInstance().start();
            ctx.close();
            channelMap.remove(remoteProxyChannelId);
        }
    }

    private void processData(Message message) {
        String channelId = message.getChannelId();
        Channel channel = channelMap.get(channelId);
        if (channel != null) {
            channel.writeAndFlush(message.getData());
        }
    }

    private void processDisconnected(Message message) {
        String channelId = message.getChannelId();
        Channel channel = channelMap.get(channelId);
        if (channel != null) {
            channel.close();
            channelMap.remove(channelId);
        }
    }
}
