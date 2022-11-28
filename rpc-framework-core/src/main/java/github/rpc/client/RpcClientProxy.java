package github.rpc.client;


import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

// 动态代理service接口，在接口的对应方法中封装Rpcrequest对象，并持有一个种类的rpcClient以实现网络通信
public class RpcClientProxy {
    private RpcClient rpcClient;
    public RpcClientProxy(RpcClient rpcClient){
        this.rpcClient = rpcClient;
    }


    public Object getProxy(Class<?> clazz){
        // 返回被代理接口的代理对象，该对象全权代理需要代理的接口，将内部方法增强为:封装rpcRequest+调用rpcClient,返回调用的结果
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new ClientInvocationHandler(rpcClient));
        return o;
    }
}

class ClientInvocationHandler implements InvocationHandler {
    private RpcClient rpcClient;

    ClientInvocationHandler(RpcClient rpcClient){
        this.rpcClient = rpcClient;
    }
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 封装rpcrequest
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setInterfaceName(method.getDeclaringClass().getName());
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setParamsType(method.getParameterTypes());
        rpcRequest.setParams(args);
        // 调用指定的rpcClient去发送该rpcRequest
        RpcResponse rpcResponse = rpcClient.sendRequest(rpcRequest);
        if (rpcResponse == null){
            return null;
        }
        Object data = rpcResponse.getData();
        return data;
    }
}
