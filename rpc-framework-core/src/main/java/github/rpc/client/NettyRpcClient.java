package github.rpc.client;


import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.common.SingletonFactory;
import github.rpc.loadbalance.LoadBalance;
import github.rpc.loadbalance.loadbalancer.RandomLoadBalance;
import github.rpc.registry.zk.ZkServiceRegister;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;

public class NettyRpcClient implements RpcClient {
    private static final Bootstrap bootstrap;
    private static final EventLoopGroup eventLoopGroup;
    private String host;
    private int port;
    private ZkServiceRegister zkServiceRegister = SingletonFactory.getInstance(ZkServiceRegister.class);
    public NettyRpcClient(ZkServiceRegister zkServiceRegister){
        this.zkServiceRegister = zkServiceRegister;
    }
    public NettyRpcClient(){

    }
    static {
        // 客户端的基本配置，直接设置在静态变量中，重复使用
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new NettyClientInitializer());
    }

    public RpcResponse sendRequest(RpcRequest rpcRequest) {
        try {
            LoadBalance loadBalance = new RandomLoadBalance(); // 在此选择负载均衡算法
            InetSocketAddress inetSocketAddress = zkServiceRegister.serviceDiscovery(rpcRequest.getInterfaceName(),loadBalance);
            host = inetSocketAddress.getHostName();
            port = inetSocketAddress.getPort();
            ChannelFuture f = bootstrap.connect(host, port).sync();
            // 获取channel
            Channel channel = f.channel();
            // 传输请求，非阻塞,等不到服务端发回响应这变就会继续往下执行，因此没办法原地得到response，只能等通过网络收到response后在channelRead0中得到response
            // 后续要对此进行优化，必须要在这边得到Rpcresponse才能向上返回的
            channel.writeAndFlush(rpcRequest);
            // 监听关闭事件，如果没有关闭事件则会一直阻塞在此
            // 通过此函数实现阻塞，一旦收到服务端的关闭事件，那么也一定收到了服务端的response，那么一定在channelRead0中设置好了AttributeKey，因此可以实现response原地获取
            channel.closeFuture().sync();
            // 优化:阻塞的获取response，从channelRead0中读取,通过起别名的方式
            AttributeKey<Object> key = AttributeKey.valueOf("RpcResponse");
            RpcResponse response = (RpcResponse) channel.attr(key).get();
            return response;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}
