package github.rpc;

import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.common.URL;

public interface Invoker {
    RpcResponse doInvoke(RpcRequest rpcRequest, URL url); // consumer side

    Object doInvoke(RpcRequest rpcRequest); // provider side

    URL getURL();
}
