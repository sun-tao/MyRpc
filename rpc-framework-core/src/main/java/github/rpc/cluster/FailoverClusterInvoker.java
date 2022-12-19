package github.rpc.cluster;

import github.rpc.client.NettyRpcClient;
import github.rpc.client.RpcClient;
import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.common.RpcResponseHolder;
import github.rpc.common.SingletonFactory;
import github.rpc.extension.ExtensionLoader;
import github.rpc.registry.zk.ZkServiceRegister;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class FailoverClusterInvoker {
    private RpcClient rpcClient = ExtensionLoader.getExtensionLoader(RpcClient.class).getExtension("netty");
    private ZkServiceRegister zkServiceRegister = SingletonFactory.getInstance(ZkServiceRegister.class);
    private String host;
    private int port;
    // 负载均衡算法选择并调用
}
