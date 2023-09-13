package github.rpc.protocol;

import github.rpc.Invoker;
import github.rpc.client.NettyRpcClient;
import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.common.URL;

import java.io.IOException;

public class MyRpcInvoker implements Invoker {
    public NettyRpcClient client;
    public URL url;
    public void initInvoker(URL url){
        this.url = url;
        client = new NettyRpcClient();
        client.connect(url);
    }
    @Override
    public RpcResponse doInvoke(RpcRequest rpcRequest, URL url) {// consumer side
        RpcResponse rpcResponse;
        try {
            rpcResponse = client.sendRequest(rpcRequest, url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return rpcResponse;
    }

    @Override
    public Object doInvoke(RpcRequest rpcRequest) { // provider side
        return null;
    }

    @Override
    public URL getURL() {
        return url;
    }
}
