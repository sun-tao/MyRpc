package github.rpc.registry;

import github.rpc.common.RpcRequest;
import github.rpc.loadbalance.LoadBalance;

import java.net.InetSocketAddress;
import java.util.List;

public interface ServiceDiscovery {
    // 服务调用方
    InetSocketAddress serviceDiscovery(String serviceName, LoadBalance loadBalance, RpcRequest rpcRequest, List<String> invokers, List<String> invoked);
}
