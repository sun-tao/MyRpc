package github.rpc.client;


import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.common.RpcResponseHolder;
import github.rpc.common.SingletonFactory;
import github.rpc.enums.LoadBalanceEnum;
import github.rpc.extension.ExtensionLoader;
import github.rpc.loadbalance.LoadBalance;
import github.rpc.loadbalance.loadbalancer.RandomLoadBalance;
import github.rpc.provider.ServiceProvider;
import github.rpc.registry.ServiceRegister;
import github.rpc.registry.zk.ZkServiceDiscovery;
import github.rpc.registry.zk.ZkServiceRegister;
import github.rpc.route.ConditionRoute;
import github.rpc.util.IpUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class NettyRpcClient implements RpcClient {
    private static final Bootstrap bootstrap;
    private static final EventLoopGroup eventLoopGroup;
    private String host;
    private int port;
    public static ConcurrentHashMap<String,Channel> channelHashMap = new ConcurrentHashMap<>();
    private ZkServiceRegister zkServiceRegister = SingletonFactory.getInstance(ZkServiceRegister.class);
    private ZkServiceDiscovery zkServiceDiscovery = SingletonFactory.getInstance(ZkServiceDiscovery.class);
    private LoadBalance loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(LoadBalanceEnum.RANDOM.getName());
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
                .handler(new NettyClientInitializer())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000);
    }

    // 建立连接
    // 连接并获取channel,连接的时候，对于已经建立的连接则不会重新建立连接了
    private Channel doConnect(String host,int port) throws IOException{
        String remoting = host + port;
        Channel channel = channelHashMap.get(remoting);
        if (channel == null || !channel.isActive()){
            // 若连接不存在或是连接被异常关闭了，都需要重新连接
            try {
                ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
                log.info("连接Netty服务器成功!");
                channelHashMap.put(remoting,channelFuture.channel()); // 当channle失效时，此时覆盖之前失效的连接
            } catch ( Exception  e) {
                throw new IOException("connection exception");
            }
        }
        channel = channelHashMap.get(remoting);
        return channel;
    }

    public RpcResponse sendRequest(RpcRequest rpcRequest) throws IOException{
            int len = 5;
            // 先读本地服务列表缓存,当前的服务提供者列表
            List<String> invokers = zkServiceDiscovery.getInvokers(rpcRequest.getInterfaceName());
            RpcResponseHolder rpcResponseHolder = SingletonFactory.getInstance(RpcResponseHolder.class);
            List<String> invoked = new ArrayList<>(invokers.size());
            // retry loop
            for (int i = 0 ; i < len ; i++){
                if (i > 0){
                    // check whether the invokers still not empty
                    invokers = zkServiceDiscovery.getInvokers(rpcRequest.getInterfaceName());  // 更新一下invokers列表
                }
                // 运用路由策略
                List<String> routes = zkServiceDiscovery.getRoutes(rpcRequest.getInterfaceName());
                for (int j = 0 ; j < routes.size() ; j++){
                    ConditionRoute conditionRoute = new ConditionRoute(routes.get(j));
                    invokers = conditionRoute.route(invokers, IpUtils.getRealIp());
                }
                // 运用负载均衡
                String address = zkServiceDiscovery.serviceDiscovery(rpcRequest.getInterfaceName(),loadBalance,rpcRequest,invokers,invoked);
                // 选中一个节点
                if (address == null) throw new IOException("connection failed！");
                invoked.add(address);
                try {
                    host = parseIP(address);
                    port = parsePort(address);
                    Channel channel = doConnect(host, port);
                    CompletableFuture<RpcResponse> future = new CompletableFuture<>();
                    rpcResponseHolder.put(rpcRequest.getRequestId(),future);  // 注册CompletableFuture
                    channel.writeAndFlush(rpcRequest);
                    // 阻塞的获取结果，直到接收到rpcResponse
                    RpcResponse rpcResponse = null;
                    rpcResponse = future.get(5000, TimeUnit.MILLISECONDS);  // 同步超时机制,待优化为异步
                    return rpcResponse;
                }catch (IOException  | TimeoutException e){ // 处理一下网络异常，比如服务调用的时候某台服务结点突然下线
                    log.info("retry starts...");
                }catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            throw new IOException("after retry 5 times , still connection fail");
    }

    /*
        input : address = 192.168.1.1:8080?weight=10
        output: ip + port
     */
    private String parseIP(String address){
        int index = address.indexOf("?");
        String[] strs;
        if (index < 0){  // 无参数
            strs = address.split(":");
            return strs[0];
        }else{ // 有参数
            address = address.substring(0,index);
            strs = address.split(":");
            return strs[0];
        }
    }

    private int parsePort(String address){
        int index = address.indexOf("?");
        String[] strs;
        if (index < 0){  // 无参数
            strs = address.split(":");
            return Integer.parseInt(strs[1]);
        }else{ // 有参数
            address = address.substring(0,index);
            strs = address.split(":");
            return Integer.parseInt(strs[1]);
        }
    }
}
