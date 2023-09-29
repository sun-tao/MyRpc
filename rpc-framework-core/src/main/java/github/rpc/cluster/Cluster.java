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

}
