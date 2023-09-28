package github.rpc.remoting.exchange;

import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.remoting.Channel;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
// 一个类即包含holder管理职责
public class DefaultFuture extends CompletableFuture<Object> {
    private RpcRequest request;
    private static Map<Integer,DefaultFuture> FUTURES = new ConcurrentHashMap<>();
    public DefaultFuture(RpcRequest request){
        this.request = request;
        FUTURES.put(request.getRequestId(),this);
    }

    public static void received(Object message){
        RpcResponse response;
        if (message instanceof RpcResponse){
            response = (RpcResponse) message;
            int id = response.getRequestId();
            DefaultFuture future = FUTURES.get(id);
            future.doReceived(message);
            FUTURES.remove(id);
        }
    }

    private void doReceived(Object message){
        if (message instanceof RpcResponse){
            this.complete(message);
        }
    }
    // todo：配合超时机制
    public void sent(){

    }


}
