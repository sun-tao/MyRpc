package github.rpc.client;


import github.rpc.annotation.Spi;
import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.common.URL;

import java.io.IOException;

@Spi
public interface RpcClient {
    RpcResponse sendRequest(RpcRequest rpcRequest, URL url) throws IOException;
}
