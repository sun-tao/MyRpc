package github.rpc.remoting.server;

import github.rpc.common.URL;
import github.rpc.exporter.Exporter;
import github.rpc.extension.ExtensionLoader;
import github.rpc.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class RpcServerProxy {
    public Registry registry; //根据url中的registryType spi获取注册中心实例,目前只支持单注册中心配置
    public Map<String,URL> serviceUrl = new HashMap<>();
    public Map<String,Object> serviceRef = new HashMap<>();
    public List<Exporter> exporters = new ArrayList<>();
    public CountDownLatch latch = new CountDownLatch(1);
    public void setRef(String serviceName,Object ref){
        serviceRef.put(serviceName,ref);
    }
    public void setUrl(String serviceName,URL url){
        serviceUrl.put(serviceName,url);
    }
    public void exportAndRegister(){
        for (Map.Entry<String,URL> entry : serviceUrl.entrySet()){
            String serviceName = entry.getKey();
            URL url = entry.getValue();
            Object ref = serviceRef.get(serviceName);
            activateRegistryIfNeed(url);
            ProxyInvoker invoker = new ProxyInvoker(ref,url);
            Exporter export = registry.export(invoker);
            exporters.add(export);
            registry.register(url);
        }
    }

    private void activateRegistryIfNeed(URL url){
        String registryType = url.getRegistryType();
        registry = ExtensionLoader.getExtensionLoader(Registry.class).getExtension(registryType,url);
    }
    public void await(){
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
