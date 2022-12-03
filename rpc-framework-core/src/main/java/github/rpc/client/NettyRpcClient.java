package github.rpc.client;


import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.common.RpcResponseHolder;
import github.rpc.common.SingletonFactory;
import github.rpc.extension.ExtensionLoader;
import github.rpc.loadbalance.LoadBalance;
import github.rpc.loadbalance.loadbalancer.RandomLoadBalance;
import github.rpc.provider.ServiceProvider;
import github.rpc.registry.ServiceRegister;
import github.rpc.registry.zk.ZkServiceRegister;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashMap;

@Slf4j
public class NettyRpcClient implements RpcClient {
    private static final Bootstrap bootstrap;
    private static final EventLoopGroup eventLoopGroup;
    private String host;
    private int port;
    private static HashMap<String,Channel> channelHashMap = new HashMap<>();
    private ZkServiceRegister zkServiceRegister = SingletonFactory.getInstance(ZkServiceRegister.class);
    private LoadBalance loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("consistentHash");
//    private ZkServiceRegister zkServiceRegister = (ZkServiceRegister) ExtensionLoader.getExtensionLoader(ServiceRegister.class).getExtension("zkServiceRegister");
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

    // 建立连接
    private Channel doConnect(String host,int port){
        String remoting = host + port;
        Channel channel = channelHashMap.get(remoting);
        if (channel == null || !channel.isActive()){
            // 若连接不存在或是连接被异常关闭了，都需要重新连接
            try {
                ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
                log.info("连接Netty服务器成功!");
                channelHashMap.put(remoting,channelFuture.channel()); // 当channle失效时，此时覆盖之前失效的连接
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        channel = channelHashMap.get(remoting);
        return channel;
    }

    public RpcResponse sendRequest(RpcRequest rpcRequest) {
        try {
            // LoadBalance loadBalance = new RandomLoadBalance(); // 在此选择负载均衡算法
            InetSocketAddress inetSocketAddress = zkServiceRegister.serviceDiscovery(rpcRequest.getInterfaceName(),loadBalance,rpcRequest);
            if (inetSocketAddress == null){
                return null;
            }
            host = inetSocketAddress.getHostName();
            port = inetSocketAddress.getPort();
            // 每一次 发送Rpc请求，都会新建Netty连接，相当于短连接
//            ChannelFuture f = bootstrap.connect(host, port).sync();
            // 连接并获取channel,连接的时候，对于已经建立的连接则不会重新建立连接了
            Channel channel = doConnect(host, port);
            channel.writeAndFlush(rpcRequest);
            // 阻塞的等待服务端返回RpcResponse
            RpcResponseHolder rpcResponseHolder = SingletonFactory.getInstance(RpcResponseHolder.class);
            while(rpcResponseHolder.getRpcResponse(rpcRequest.getRequestId()) == null){
                // 自旋等待,直到接收到了rpcResponse才能继续执行,不考虑线程安全问题
                Thread.sleep(100);
            }
            return rpcResponseHolder.getRpcResponse(rpcRequest.getRequestId());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
