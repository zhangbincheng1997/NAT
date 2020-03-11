# NAT 内网穿透

1. 微信、支付宝支付
2. 小程序应用
3. 个人网站
4. ......

## 自定义协议
```
private MessageType type;
private String channelId;
private byte[] data;

UNKNOWN(0),
REGISTER(1),
REGISTER_RESULT(2),
CONNECTED(3),
DISCONNECTED(4),
DATA(5),
KEEPALIVE(6);
```
报文 = 报文总长度 + Type(4) + CHANNEL_ID_SIZE(4) + CHANNEL_ID + DATA_SIZE(4) + DATA

## 拆包粘包
LengthFieldBasedFrameDecoder

## 心跳检测
IdleStateHandler

## 转发流程
| 机器 | 标识 | 端口 |
| :---: | :---: | :---: |
| 内网服务 | service | 8080 |
| 客户端 | client | 随机 |
| 服务端 | server | 8888 |
| 代理服务 | proxy | 10000 |

外网地址：http://www.littleredhat1997.com/  
内网地址：http://localhost/  
http://localhost:8080/ <=> http://www.littleredhat1997.com:10000/

1. 启动服务端 nat-server.jar
 - server绑定端口8888

2. 启动客户端 nat-client.jar
 - Java Swing图形界面
 
3. 开启代理 点击`开启服务`
 - client连接server
 - client->server：请求注册服务
 1. 第一步
 - server->client：返回注册结果
 2. 第二步
 - server绑定proxy
 - proxy->server：成功代理（是否可以直接返回给client呢）
 - server->client：成功代理
 - client连接service

4. 外网访问链接 http://www.littleredhat1997.com:10000/
 - proxy->server：转发请求
 - server->client：请求资源
 - client->service：请求资源
 - service->server：直接响应资源（跳过client）
 - server->proxy：输出结果

4. 关闭代理 点击`关闭服务`
- client->server：关闭连接
- client->service：关闭连接
- server->proxy：关闭连接

5. 长时间不访问
- proxy->client：关闭连接
- client->service：关闭连接
- service->server：关闭连接