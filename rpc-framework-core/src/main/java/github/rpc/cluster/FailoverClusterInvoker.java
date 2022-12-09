package github.rpc.cluster;

import github.rpc.client.NettyRpcClient;
import github.rpc.client.RpcClient;
import github.rpc.common.RpcResponse;
import github.rpc.extension.ExtensionLoader;

public class FailoverClusterInvoker {
    private RpcClient rpcClient = ExtensionLoader.getExtensionLoader(RpcClient.class).getExtension("netty");

    public RpcResponse doInvoke(){
        return null;
    }
}
