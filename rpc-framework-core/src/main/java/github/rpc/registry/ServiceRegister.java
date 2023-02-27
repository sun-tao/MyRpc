package github.rpc.registry;

import github.rpc.annotation.Spi;
import github.rpc.common.RpcRequest;
import github.rpc.loadbalance.LoadBalance;

import java.net.InetSocketAddress;
import java.util.List;

@Spi
public interface ServiceRegister {
    // 服务提供方
    void register(String serviceName, String host,String port);
    // 服务结点指定的路由策略
    void registerRoute(String serviceName);
}
