package github.rpc.client;


import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.common.RpcResponseHolder;
import github.rpc.common.SingletonFactory;
import github.rpc.common.URL;
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
    public static ConcurrentHashMap<String,Channel> channelHashMap = new ConcurrentHashMap<>();

    public NettyRpcClient(){}
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
    private Channel doConnect(String host,String port) throws IOException{
        String remoting = host + port;
        Channel channel = channelHashMap.get(remoting);
        if (channel == null || !channel.isActive()){
            // 若连接不存在或是连接被异常关闭了，都需要重新连接
            try {
                ChannelFuture channelFuture = bootstrap.connect(host, Integer.parseInt(port) ).sync();
                log.info("连接Netty服务器成功!");
                channelHashMap.put(remoting,channelFuture.channel()); // 当channle失效时，此时覆盖之前失效的连接
            } catch ( Exception  e) {
                throw new IOException("connection exception");
            }
        }
        channel = channelHashMap.get(remoting);
        return channel;
    }

    public Channel connect(URL url){
        try {
            return doConnect(url.getIp(),url.getPort());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public RpcResponse sendRequest(RpcRequest rpcRequest,URL url) throws IOException{
        Channel channel = channelHashMap.get(url.parseInstance());
        if (channel == null){
            channel = connect(url);
        }
        RpcResponseHolder rpcResponseHolder = SingletonFactory.getInstance(RpcResponseHolder.class);
        CompletableFuture<RpcResponse> future = new CompletableFuture<>();
        rpcResponseHolder.put(rpcRequest.getRequestId(),future);  // 注册CompletableFuture
        channel.writeAndFlush(rpcRequest);
        // 阻塞的获取结果，直到接收到rpcResponse
        RpcResponse rpcResponse = null;
        try {
            rpcResponse = future.get(5000, TimeUnit.MILLISECONDS);  // 同步超时机制,待优化为异步
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        return rpcResponse;
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
