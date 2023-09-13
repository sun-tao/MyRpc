package github.rpc.cluster;

import github.rpc.Invoker;
import github.rpc.common.*;
import github.rpc.extension.ExtensionLoader;
import github.rpc.loadbalance.LoadBalance;
import github.rpc.protocol.MyRpcProtocol;
import github.rpc.protocol.Protocol;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class FailoverCluster implements Cluster {
    // 提供方url-远程的实现类
//    public ConcurrentHashMap<URL, Invoker> invokers = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String,List<Invoker>> invokers = new ConcurrentHashMap<>();
//    private List<Invoker> convertInvokersMap2List(){
//        List<Invoker> result = new ArrayList<>();
//        for (Map.Entry<URL,Invoker> entry : invokers.entrySet()){
//            result.add(entry.getValue());
//        }
//        return result;
//    }
    private RpcResponse doInvoke(RpcRequest rpcRequest, URL url) { // 这里的url为消费端url
        // 负载均衡算法选择并调用
        int retryTimes = Integer.parseInt(url.getRetryTimes());
        String loadBalanceType = url.getLoadBalacne();
        LoadBalance loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(loadBalanceType);
        for (int i = 0; i < retryTimes; i++) {
            // todo:运用路由策略,异常重试机制
            // 运用负载均衡
            String serviceName = rpcRequest.getRpcServiceName();
            List<Invoker> invokerList = invokers.get(serviceName);
            if (invokerList == null){
                log.error("FailoverCluster.doInvoke Err:No invokers for service:{}",serviceName);
                return null;
            }
            String instance = loadBalance.loadBalance(invokerList,rpcRequest);
            // 选中一个节点
            if (instance == null){
                log.error("FailoverClusterInvoker:loadbalance error");
                continue;
            }
            // todo：剔除已经选中过的节点
            Invoker invoker = findInvokerByInstance(serviceName,instance);
            RpcResponse rpcResponse = invoker.doInvoke(rpcRequest, invoker.getURL());
            return rpcResponse;
        }
        return null;
    }
    @Override
    public RpcResponse invoke(RpcRequest rpcRequest, URL url) {
        return doInvoke(rpcRequest, url);
    }

    private void doRefer(URL url) {  // 调用protocol层的refer并将refer得到的远端服务实现类加入内存
        String pro = url.getProtocol();  // 默认是自带的myrpc协议
        String serviceName = url.getServiceName();
        Protocol protocol = ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(pro);
        Invoker invoker = protocol.refer(url);
        List<Invoker> localUrls = invokers.computeIfAbsent(serviceName,k->{
            return new ArrayList<Invoker>();
        });
        localUrls.add(invoker);
    }
    @Override
    public void refer(List<URL> urls){
        for (int i = 0 ; i < urls.size() ; i++){
            doRefer(urls.get(i));
        }
    }
    // 根据负载均衡选中的实例信息获取对应的Invoker,第一个参数用于缩小搜索范围为当前服务
    private Invoker findInvokerByInstance(String serviceName,String instance){
        List<Invoker> invokerList = invokers.get(serviceName);
        for (int i = 0 ; i < invokerList.size() ; i++){
            URL url = invokerList.get(i).getURL();
            if (url.parseInstance().equals(instance)){
                return invokerList.get(i);
            }
        }
        return null;
    }
}
