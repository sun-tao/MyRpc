# MyRpc
a rpc framework based on java

## 架构
![图片](./总体架构.jpg)
<center style="font-size:14px;color:orange;text-decoration:underline">初始架构</center> 


![MyRpc框架重构架构图](./MyRpc框架重构.jpg)
<center style="font-size:14px;color:orange;text-decoration:underline">重构架构</center> 

  ![myrpc框架连接层重构.jpg](myrpc%E6%A1%86%E6%9E%B6%E8%BF%9E%E6%8E%A5%E5%B1%82%E9%87%8D%E6%9E%84.jpg)
<center style="font-size:14px;color:orange;text-decoration:underline">连接层重构</center> 

  ![时间轮设计.jpg](%E6%97%B6%E9%97%B4%E8%BD%AE%E8%AE%BE%E8%AE%A1.jpg)
<center style="font-size:14px;color:orange;text-decoration:underline">时间轮设计</center> 

<img src="线程模型.jpg" alt="线程模型" style="zoom:67%;" />

<center style="font-size:14px;color:orange;text-decoration:underline">线程模型</center> 

## 特性

1. 使用Netty作为底层网络通信框架，NIO提升网络通信效率

2. 采用Netty长连接机制，避免不必要的重复连接开销

3. 使用心跳包维护上述长连接，客户端定期向服务端发心跳，并期望服务端进行心跳回复

4. 服务端健康检查机制：对服务端维护的连接中长时间没收到心跳的客户端主动断开，节省服务端资源

5. 客户端健康检查机制：对长时间没有收到回复的服务端连接，将该连接断开，并标记该连接对应的服务端为死亡状态，从客户端缓存的服务提供者列表中移出。

6. ~~客户端采用`Failover`重连机制，应对连接远程服务端失败的情况~~

   客户端的初始化时连接服务端失败将会将其放入时间轮中，延时重试。

7. 客户端实现负载均衡算法选择连接的服务端(实现权重随机负载均衡和一致性哈希负载均衡算法)

8. 两种序列化和反序列化方式(java原生+hessian)

9. 自定义注解，方便服务提供者和消费者注册和消费服务

10. ~~集成Spring框架，通过注解注册服务，通过注解消费服务~~

11. 采用SPI机制+URL配置总线，实现 接口实现类的动态插拔

12. 采用了单例设计模式、适配器模式（codecAdapt服务端多例适配）、装饰器模式（channelHandler的传递增强）

13. ~~异步IO场景下，使用`CompletableFuture`阻塞地同步获取响应结果。优化了原本的自旋获取响应结果，减少CPU负载~~。

    引入CompletableFuture实现调用端、服务端全链路异步。

14. 使用zookeeper的watch机制，配合本地缓存provider列表，异步更新本地缓存，使本地缓存中存储最新的provider列表。优化了原本每次调用都需要与zookeeper进行通信的流程。

15. 添加路由策略模块，在服务结点上线时，支持自定义路由策略，辅助服务的灰度发布，定义黑名单、白名单等流量治理功能。

16. ~~添加客户端异常重试机制，如果客户端发送请求时出现网络问题或者服务端下线等异常情况，RPC框架能够抓到异常进行重新发送，重新发送会剔除发生异常的结点。默认重试次数为5次。~~

    集群容错FailOver策略，retry次数可配置。

17. ~~添加客户端超时重试机制，客户端发送请求后调用future.get(timeout)等待一定时间，如果仍没有收到响应则判定本次请求超时，重新发送请求，默认超时时间为5s。~~

    客户端请求基于时间轮实现超时机制，超时后使CompletableFuture异常完成，删除内存中map的entry，来避免海量请求超时后oom的情况。

18. 服务端和客户端引入myrpc框架线程池，实现io线程->框架线程切换，减少底层nio线程的开销，更快释放io线程，增大服务的吞吐量，降低延时。

19. 增加http/1协议支持，http协议的协议穿透性更强，header和body分离对于网关解析也更友好。

20. 容错模块完善，引入Sentienl组件实现服务端基于QPS的限流。客户端新增MockCluster支持本地mock，实现降级功能。

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