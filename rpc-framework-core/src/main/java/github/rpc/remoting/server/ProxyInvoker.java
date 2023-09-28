package github.rpc.remoting.server;

import github.rpc.Invoker;
import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.common.URL;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public class ProxyInvoker implements Invoker {
    public String serviceName; // 服务名
    public Object targetService; // 服务本地实现类

    public URL url;

    public ProxyInvoker(Object targetService){
        this.targetService = targetService;
    }

    public ProxyInvoker(Object targetService,URL url){
        this.targetService = targetService;
        this.url = url;
    }
    @Override
    public CompletableFuture<Object> doInvoke(RpcRequest rpcRequest, URL url) {
        return null;
    }

    @Override
    public Object doInvoke(RpcRequest rpcRequest) {
        try {
            Method method =  targetService.getClass().getMethod(rpcRequest.getMethodName(),rpcRequest.getParamsType());
            return method.invoke(targetService,rpcRequest.getParams());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public URL getURL() {
        return url;
    }
}
