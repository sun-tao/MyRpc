package github.rpc;

import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.common.URL;

import java.util.concurrent.CompletableFuture;

public interface Invoker {
    CompletableFuture<Object> doInvoke(RpcRequest rpcRequest,URL url); // consumer side

    Object doInvoke(RpcRequest rpcRequest); // provider side

    URL getURL();
}
