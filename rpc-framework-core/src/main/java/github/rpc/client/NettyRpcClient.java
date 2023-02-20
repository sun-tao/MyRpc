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
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class NettyRpcClient implements RpcClient {
    private static final Bootstrap bootstrap;
    private static final EventLoopGroup eventLoopGroup;
    private String host;
    private int port;
    public static HashMap<String,Channel> channelHashMap = new HashMap<>();
    private ZkServiceRegister zkServiceRegister = SingletonFactory.getInstance(ZkServiceRegister.class);
    private LoadBalance loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("Random");
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
    private Channel doConnect(String host,int port) throws Exception{
        String remoting = host + port;
        Channel channel = channelHashMap.get(remoting);
        if (channel == null || !channel.isActive()){
            // 若连接不存在或是连接被异常关闭了，都需要重新连接
            try {
                ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
                log.info("连接Netty服务器成功!");
                channelHashMap.put(remoting,channelFuture.channel()); // 当channle失效时，此时覆盖之前失效的连接
            } catch ( Exception  e) {
                throw new Exception("connection exception");
            }
        }
        channel = channelHashMap.get(remoting);
        return channel;
    }

    public RpcResponse sendRequest(RpcRequest rpcRequest) throws RuntimeException{
            int len = 2;
            List<String> invokers = zkServiceRegister.getInvokers(rpcRequest.getInterfaceName());
            List<String> invoked = new ArrayList<>(invokers.size());
            Exception last_e = null;
            // retry loop
            for (int i = 0 ; i < len ; i++){
                int cnt = 0;
                if (i > 0){
                    // check whether the invokers still not empty
                    invokers = zkServiceRegister.getInvokers(rpcRequest.getInterfaceName());  // 更新一下invokers列表
                }
                InetSocketAddress inetSocketAddress = zkServiceRegister.serviceDiscovery(rpcRequest.getInterfaceName(),loadBalance,rpcRequest,invokers,invoked);
                if (inetSocketAddress == null) throw new RuntimeException("connection failed！");
                invoked.add(inetSocketAddress.getHostName() + ":" + inetSocketAddress.getPort());
                try {
                    host = inetSocketAddress.getHostName();
                    port = inetSocketAddress.getPort();
                    Channel channel = doConnect(host, port);  // 可能连接不上，这时需要触发重试机制
                    if (last_e != null){
                        // 若后面几次重连成功则以warn形式打印先前连接的报错
                        log.warn("Although retry successfully,but there are some error occured before success,the error is {}" , last_e.getLocalizedMessage());
                    }
                    channel.writeAndFlush(rpcRequest);
                    // 阻塞的等待服务端返回RpcResponse
                    RpcResponseHolder rpcResponseHolder = SingletonFactory.getInstance(RpcResponseHolder.class);
                    while(rpcResponseHolder.getRpcResponse(rpcRequest.getRequestId()) == null){
                        // 自旋等待,直到接收到了rpcResponse才能继续执行,不考虑线程安全问题
                        Thread.sleep(1000);
                        cnt++;
                        if (cnt > 10){
                            throw new RuntimeException("服务调用超时!");
                        }
                    }
                    return rpcResponseHolder.getRpcResponse(rpcRequest.getRequestId());
                }catch (Exception e){
                    log.warn("服务调用超时！");
                    last_e = e;
                }
            }
            throw  new RuntimeException("connection failed！");
    }
}
