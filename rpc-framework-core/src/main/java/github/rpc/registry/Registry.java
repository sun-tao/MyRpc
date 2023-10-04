package github.rpc.registry;

import github.rpc.Invoker;
import github.rpc.annotation.Spi;
import github.rpc.cluster.Cluster;
import github.rpc.cluster.FailoverCluster;
import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.common.URL;
import github.rpc.exporter.Exporter;

import java.util.concurrent.CompletableFuture;

@Spi
public interface Registry {
    void register(URL url); // 服务端服务注册
    Exporter export(Invoker invoker); // 服务端
    void subscribe(URL url); // 消费端订阅远端服务
    Cluster refer(URL url); // 消费端
    CompletableFuture<Object> invoke (RpcRequest rpcRequest, URL url);
}
