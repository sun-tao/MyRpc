# MyRpc
a rpc framework based on java

## 架构

![图片](./总体架构.jpg)

## 特性

1. 使用Netty作为底层网络通信框架，NIO提升网络通信效率
2. 采用Netty长连接机制，避免不必要的重复连接开销
3. 使用心跳包维护上述长连接，客户端定期向服务端发心跳，并期望服务端进行心跳回复
4. 服务端健康检查机制：对服务端维护的连接中长时间没收到心跳的客户端主动断开，节省服务端资源
5. 客户端健康检查机制：对长时间没有收到回复的服务端连接，将该连接断开，并标记该连接对应的服务端为死亡状态，从客户端缓存的服务提供者列表中移出。
6. 客户端采用`Failover`重连机制，应对连接远程服务端失败的情况
7. 客户端实现负载均衡算法选择连接的服务端(实现随机负载均衡和一致性哈希负载均衡算法)
8. 两种序列化和反序列化方式(java原生+protobuf)
9. 自定义注解，方便服务提供者和消费者注册和消费服务
10. 集成Spring框架，通过注解注册服务，通过注解消费服务
11. 实现SPI机制，实现 接口实现类的动态插拔
12. 采用了单例设计模式
13. 异步IO场景下，使用`CompletableFuture`阻塞地同步获取响应结果。优化了原本的自旋获取响应结果，减少CPU负载。
14. 使用zookeeper的watch机制，配合本地缓存provider列表，异步更新本地缓存，使本地缓存中存储最新的provider列表。优化了原本每次调用都需要与zookeeper进行通信的流程。
15. 添加路由策略模块，在服务结点上线时，支持自定义路由策略，辅助服务的灰度发布，定义黑名单、白名单等流量治理功能。

## 项目模块

- rpc-framework-core:rpc核心功能部分代码实现
- rpc-example-client:测试用客户端
- rpc-example-server:测试用服务端
- rpc-framework-api:rpc服务api注册

## 传输协议

使用自定义的**可扩展**的RPC通信协议规定数据格式，解决TCP粘包、拆包问题。 

```
+---------------+---------------+-----------------+-------------+-----------------+-----------------+
|  Magic Number |  Total Length | Header Length   | Version     |  MessageType	  |	Serializer Type	|
|    4 bytes    |    4 bytes    |     2 bytes     |   1 bytes   |	1 byte		  |		1 byte		|
+---------------+---------------+
|  Message ID   |Extended Header|
|    2 bytes    |   unknown    	|
+---------------+---------------+-----------------+-------------+-----------------+
|                          Data Bytes                           |				  |
|                   Length: ${Data Length}                      |				  |
+---------------------------------------------------------------+-----------------+
```
```

字段					解释
Magic Number		 魔数，表识一个 RPC 协议包，0x12345678
Total Length         整体长度
Header Length        头部长度
Version   			 协议版本
Message Type		 消息类型，标明这是一个调用请求还是调用响应
Serializer Type		 序列化器类型，标明这个包的数据的序列化方式
Message ID			 消息ID
Extended Header		 扩展头部
Data Bytes			 传输的对象，通常是一个RpcRequest或RpcResponse对象，取决于Package Type字段，对象的序列化方式取决于Serializer Type字段。
```