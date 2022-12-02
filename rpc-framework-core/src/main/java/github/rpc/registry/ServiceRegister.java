package github.rpc.registry;

import github.rpc.annotation.Spi;
import github.rpc.common.RpcRequest;
import github.rpc.loadbalance.LoadBalance;

import java.net.InetSocketAddress;

@Spi
public interface ServiceRegister {
    void register(String serviceName, InetSocketAddress serverAddress);
    InetSocketAddress serviceDiscovery(String serviceName, LoadBalance loadBalance, RpcRequest rpcRequest);
}
