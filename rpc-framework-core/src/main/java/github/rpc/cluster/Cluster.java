package github.rpc.cluster;

import github.rpc.annotation.Spi;
import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.common.URL;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Spi
public interface Cluster {
    CompletableFuture<Object> invoke(RpcRequest rpcRequest, URL url);

    void refer(List<URL> urls);

    void refer(URL url); // 增量订阅的时候使用

    void cancelRefer(URL url); // 增量取消订阅-即删除invokers这个map中服务结点

}
