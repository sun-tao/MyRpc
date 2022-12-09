package github.rpc.client;


import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

// 动态代理service接口，在接口的对应方法中封装Rpcrequest对象，并持有一个种类的rpcClient以实现网络通信
public class RpcClientProxy {
    private RpcClient rpcClient;
    private String interfacename_group_version;
    public RpcClientProxy(RpcClient rpcClient,String group ,String version ){
        this.rpcClient = rpcClient;
        this.interfacename_group_version = group + version;
    }


    public Object getProxy(Class<?> clazz){
        // 返回被代理接口的代理对象，该对象全权代理需要代理的接口，将内部方法增强为:封装rpcRequest+调用rpcClient,返回调用的结果
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                new ClientInvocationHandler(rpcClient,interfacename_group_version));
        return o;
    }
}

class ClientInvocationHandler implements InvocationHandler {
    private RpcClient rpcClient;
    private String interfacename_group_version;
    ClientInvocationHandler(RpcClient rpcClient , String interfacename_group_version){
        this.rpcClient = rpcClient;
        this.interfacename_group_version = interfacename_group_version;
    }
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 封装rpcrequest
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setInterfaceName(method.getDeclaringClass().getName() + interfacename_group_version);
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setParamsType(method.getParameterTypes());
        rpcRequest.setParams(args);
        rpcRequest.setRequestId(UUID.randomUUID().toString());
        // 调用指定的rpcClient去发送该rpcRequest

        RpcResponse rpcResponse = rpcClient.sendRequest(rpcRequest);

        if (rpcResponse == null){
            return null;
        }
        Object data = rpcResponse.getData();
        return data;
    }
}
