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
import github.rpc.util.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
@Slf4j
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
        future = client.request(rpcRequest,url);
        waitFutureIfSync(future,url);
        return future;
    }

    private void waitFutureIfSync(CompletableFuture<Object> future,URL url){
        if (url.getConsumerAsync().equals("true")){
            return;
        }else if (url.getConsumerAsync().equals("false")){
            try {
                // wait,考虑超时的情况，这边可能会抛出自定义的timeoutexception，要向上传递
                // 向上传递最标准，但是代码改动太大，因此考虑直接将异常封装在defaultFuture中，这样也可以实现回传给业务
                future.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e); //应对超时
            }
        }
    }

    @Override
    public CompletableFuture<Object> doInvoke(RpcRequest rpcRequest) { // provider side
        return null;
    }

    @Override
    public URL getURL() {
        return url;
    }
}
