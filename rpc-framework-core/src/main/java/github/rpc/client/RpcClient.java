package github.rpc.client;


import github.rpc.annotation.Spi;
import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;

import java.io.IOException;

@Spi
public interface RpcClient {
    RpcResponse sendRequest(RpcRequest rpcRequest) throws IOException;
}
