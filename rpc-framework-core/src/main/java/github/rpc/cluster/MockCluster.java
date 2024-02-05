package github.rpc.cluster;

import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.common.URL;
import github.rpc.extension.ExtensionLoader;
import github.rpc.remoting.exchange.DefaultFuture;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Slf4j
public class MockCluster implements Cluster{
    private Cluster cluster;
    public MockCluster(URL url){
        String clusterType = url.getClusterType();
        cluster = ExtensionLoader.getExtensionLoader(Cluster.class).getExtension(clusterType);
    }
    @Override
    public CompletableFuture<Object> invoke(RpcRequest rpcRequest, URL url) {
        DefaultFuture future = new DefaultFuture(rpcRequest, url);
        RpcResponse response = new RpcResponse();
        response.setRequestId(rpcRequest.getRequestId());
        if (url.parseMockMode().equals("force")){ // 强制返回mock
            response.setData(getMockResult(rpcRequest,url));
            DefaultFuture.received(response);
            return future;
        }else if (url.parseMockMode().equals("fail")){ // 失败再返回mock（仅针对rpc调用异常，而不处理业务异常）
            DefaultFuture cf = (DefaultFuture) cluster.invoke(rpcRequest, url);
            // 同步请求，cf已完成，异步请求，cf肯定是未完成的,对于异步请求，无需判断异常是否存在，因为肯定不存在异常
            // 这边判断异常是否存在仅是针对同步请求
            // fixme:最好搭配整套异常处理体系，采用try-catch抓 cluster.invoke 可能产生的异常，而不是用响应结果来判断是否出了异常，这样对于客户端本地产生的异常就无能为力了
            if (cf.recreate() instanceof Exception){
                // fixme:rpc请求返回了异常，直接做降级处理，但是对业务异常，不应该降级,这里没有区分业务异常和rpc异常
                response.setData(getMockResult(rpcRequest,url));
                response.clearException();
                cf.setResponse(response);
                return cf;
            }
            return cf;
        }else{ // 非法mock参数,默认不执行mock
            log.warn("illegal mock parameter:{}",url.getMock());
            return cluster.invoke(rpcRequest,url);
        }
    }

    @Override
    public void refer(List<URL> urls) {
        cluster.refer(urls);
    }

    @Override
    public void refer(URL url) {
        cluster.refer(url);
    }

    @Override
    public void cancelRefer(URL url) {
        cluster.cancelRefer(url);
    }

    private Object getMockResult(RpcRequest rpcRequest, URL url){
        String v = url.parseMockResult();
        // 判断是否是接口实现类
        try {
            Class<?> clazz = Class.forName(v);
            Object o = clazz.newInstance();
            Method method = clazz.getMethod(rpcRequest.getMethodName(),int.class);
            Object obj = method.invoke(o,rpcRequest.getParams());
            return obj;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            // mock参数不是接口实现类,仅仅只是一个值,目前只支持null值mock和String值mock，其他需要通过实现类去mock
            if (v.equals("null")){
                return null;
            }else{
                return v;
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
