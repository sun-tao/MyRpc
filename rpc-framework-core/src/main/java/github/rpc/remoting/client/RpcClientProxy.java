package github.rpc.remoting.client;


import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.common.URL;
import github.rpc.extension.ExtensionLoader;
import github.rpc.registry.Registry;
import github.rpc.remoting.exchange.DefaultFuture;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class RpcClientProxy {
    public Registry registry; //todo:后续进行注册中心的扩展，目前只支持订阅单注册中心
    public Map<String,URL> consumerUrls = new HashMap<>(); // for 多服务引用扩展
    class ClientInvocationHandler implements InvocationHandler {
        public Object invoke(Object proxy, Method method, Object[] args) {
            // 封装rpcrequest
            RpcRequest rpcRequest = new RpcRequest();
            rpcRequest.setInterfaceName(method.getDeclaringClass().getName());
            rpcRequest.setMethodName(method.getName());
            rpcRequest.setParamsType(method.getParameterTypes());
            rpcRequest.setParams(args);
            // 调用指定的rpcClient去发送该rpcRequest
            URL url = consumerUrls.get(rpcRequest.getInterfaceName());
            CompletableFuture<Object> future = registry.invoke(rpcRequest, url);
            DefaultFuture defaultFuture = (DefaultFuture) future;
            Object result = defaultFuture.recreate();
            return result;
        }
    }

    public RpcClientProxy(){

    }
    public void setUrl(String serviceName, URL url){
        consumerUrls.put(serviceName,url);
    }

    public void refer(){
        for (Map.Entry<String,URL> entry: consumerUrls.entrySet()){
            URL url = entry.getValue();
            activateRegistryIfNeed(url);
            registry.refer(url);
        }
    }
    private void activateRegistryIfNeed(URL url){
        String registryType = url.getRegistryType();
        registry = ExtensionLoader.getExtensionLoader(Registry.class).getExtension(registryType,url);
        log.info("activate registry: registryType ({}) , clusterType({})",registryType,url.getClusterType());
    }

    public Object getProxy(Class<?> clazz){
        // 返回被代理接口的代理对象，该对象全权代理需要代理的接口，将内部方法增强为:封装rpcRequest+调用rpcClient,返回调用的结果
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                new ClientInvocationHandler());
        return o;
    }
}