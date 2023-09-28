package github.rpc.protocol;

import github.rpc.Invoker;
import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.common.URL;
import github.rpc.remoting.ChannelHandler;
import github.rpc.remoting.client.AbstractClient;
import github.rpc.remoting.client.NettyClient;
import github.rpc.remoting.exchange.DefaultFuture;
import github.rpc.remoting.exchange.ExchangeChannelHandler;
import github.rpc.remoting.exchange.HeaderExchangeClient;
import github.rpc.remoting.exchange.HeaderExchangeHandler;
import github.rpc.remoting.transport.DecodeHandler;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class MyRpcInvoker implements Invoker {
    public HeaderExchangeClient client;
    public URL url;
    public ExchangeChannelHandler handler;
    public MyRpcInvoker(URL url, ExchangeChannelHandler handler){
        this.url = url;
        this.handler = handler;
        client = new HeaderExchangeClient(new NettyClient(url,new DecodeHandler(new HeaderExchangeHandler(handler))));
    }
    @Override
    public CompletableFuture<Object> doInvoke(RpcRequest rpcRequest,URL url) {// consumer side
        CompletableFuture<Object> future;
        future = client.request(rpcRequest);
        return future;
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
