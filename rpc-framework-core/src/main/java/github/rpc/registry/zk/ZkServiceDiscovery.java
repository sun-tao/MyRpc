package github.rpc.registry.zk;

import github.rpc.common.RpcRequest;
import github.rpc.loadbalance.LoadBalance;
import github.rpc.registry.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ZkServiceDiscovery implements ServiceDiscovery {
    private CuratorFramework client;
    private ConcurrentHashMap<String,List<String>> serversCache;  // 本地服务列表缓存,单服务 -> 多服务 , 这边对本地缓存的更新可能有线程冲突
    private ConcurrentHashMap<String,List<String>> serversRoutesCache; // 本地服务路由策略缓存
    private static PathChildrenCache nodeCache;
    // zookeeper根节点路径
    private static final String ROOT_PATH = "MyRpc";
    public ZkServiceDiscovery(){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        // 连接zookeeper服务器
        this.client  =
                CuratorFrameworkFactory.builder().connectString("localhost:2181").retryPolicy(retryPolicy).sessionTimeoutMs(40000).namespace(ROOT_PATH).build();
        this.client.start();
        log.info("zookeeper连接成功！");
        serversCache = new ConcurrentHashMap<>();
        serversRoutesCache = new ConcurrentHashMap<>();
    }

    @Override
    public InetSocketAddress serviceDiscovery(String serviceName, LoadBalance loadBalance, RpcRequest rpcRequest, List<String> invokers, List<String> invoked) {
        // RPC客户端使用
        try {
            log.info("客户端请求调用{}服务",serviceName);
            if (invokers == null){
                invokers = client.getChildren().forPath("/"+serviceName);
            }
            if (invokers == null || invokers.size() == 0){
                log.info("无可用的服务!");
                return null;
            }
            log.info("本地缓存的{}服务提供者列表有{}" ,serviceName, invokers);
            // 排除invoked中已经包含了的
            List<String> result = new ArrayList<>();
            for (int i = 0 ; i < invokers.size() ; i++){
                if (invoked != null && invoked.contains(invokers.get(i))){
                    continue;
                }
                result.add(invokers.get(i));
            }
            if (result == null || result.size() == 0){
                log.info("无可用的服务!");
                return null;
            }
            String address = loadBalance.loadBalance(result,rpcRequest);
            return parseAddress(address);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private InetSocketAddress parseAddress(String address){
        String[] strs = address.split(":");
        return new InetSocketAddress(strs[0],Integer.parseInt(strs[1]));
    }

    public List<String> getRoutes(String serviceName){
        try {
            final String serverRoutesName = serviceName + "/route";
            List<String> serverRoutes = serversRoutesCache.get(serviceName);
            if (serverRoutes == null){
                serverRoutes = client.getChildren().forPath("/" + serverRoutesName);
                if (serverRoutes == null){
                    // 该服务无路由策略
                    return null;
                }
                serversRoutesCache.put(serviceName,serverRoutes);
                nodeCache = new PathChildrenCache(client, "/"+serverRoutesName,true);
                nodeCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
                nodeCache.getListenable().addListener(new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                        if (pathChildrenCacheEvent.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED){
                            // 服务提供者增加
                            // spilt by "/" , get ip + port
                            String ipPort = spiltIpPort(pathChildrenCacheEvent.getData().getPath());
                            serversRoutesCache.get(serviceName).add(ipPort);
                            log.info("本地服务{}路由缓存中上线{}",serviceName,ipPort);
                        }else if (pathChildrenCacheEvent.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED){
                            // 服务提供者减少
                            String ipPort = spiltIpPort(pathChildrenCacheEvent.getData().getPath());
                            serversRoutesCache.get(serviceName).remove(ipPort);
                            log.info("本地服务{}路由缓存中下线{}",serviceName,ipPort);
                        }
                    }
                });
            }
            return serverRoutes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<String> getInvokers(String serviceName) {
        try {
            final String serviceProviderName = serviceName + "/provider";
            List<String> serverProviders = serversCache.get(serviceName);
            if (serverProviders == null){
                // 第一次调用 全量拉取zookeeper数据，初始化本地缓存
                serverProviders = client.getChildren().forPath("/" + serviceProviderName);
                serversCache.put(serviceName,serverProviders);
                // 注册自身信息到zookeeper
                register(serviceName,InetAddress.getLocalHost());
                // watch机制,异步更新本地服务缓存
                nodeCache = new PathChildrenCache(client, "/"+serviceProviderName,true);
                //调用start方法开始监听 ，设置启动模式为同步加载节点数据
                //添加监听
                nodeCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
                nodeCache.getListenable().addListener(new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                        if (pathChildrenCacheEvent.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED){
                            // 服务提供者增加
                            // spilt by "/" , get ip + port
                            String ipPort = spiltIpPort(pathChildrenCacheEvent.getData().getPath());
                            serversCache.get(serviceName).add(ipPort);
                            log.info("本地服务{}缓存中上线{}",serviceName,ipPort);
                        }else if (pathChildrenCacheEvent.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED){
                            // 服务提供者减少
                            String ipPort = spiltIpPort(pathChildrenCacheEvent.getData().getPath());
                            serversCache.get(serviceName).remove(ipPort);
                            log.info("本地服务{}缓存中下线{}",serviceName,ipPort);
                        }
                    }
                });
            }
            return serverProviders;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
        根据全量路径得到后缀路径 -- todo
     */
    private String spiltIpPort(String path){
        String[] splited = path.split("/");
        return splited[splited.length-1];
    }

    /*
        客户端调用的时候注册自身信息到 consumer中
     */
    private void register(String serviceName, InetAddress clientIp) {
        try {
            if (client.checkExists().forPath("/" + serviceName + "/consumer") == null){
                // 永久注册该服务名,因为可能有其他的rpc服务器也在zookeeper上注册了该服务名，因此服务名不能下线
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/" + serviceName + "/consumer");
            }
            // 临时性注册 本rpc服务器的IP+host
            String path = "/" + serviceName + "/consumer" + "/" + getClientAddress(clientIp);
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getClientAddress(InetAddress clientIp){
        return clientIp.getHostName();
    }
}
