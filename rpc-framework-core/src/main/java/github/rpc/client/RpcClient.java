package github.rpc.client;


import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;

public interface RpcClient {
    RpcResponse sendRequest(RpcRequest rpcRequest);
}
