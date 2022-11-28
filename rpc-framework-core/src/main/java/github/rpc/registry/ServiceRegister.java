package github.rpc.registry;

import github.rpc.loadbalance.LoadBalance;

import java.net.InetSocketAddress;

public interface ServiceRegister {
    void register(String serviceName, InetSocketAddress serverAddress);
    InetSocketAddress serviceDiscovery(String serviceName, LoadBalance loadBalance);
}
