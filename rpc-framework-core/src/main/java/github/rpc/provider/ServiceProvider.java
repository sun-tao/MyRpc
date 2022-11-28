package github.rpc.provider;



import github.rpc.registry.zk.ZkServiceRegister;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

// provider也可以像其他一样，对外提供一个接口，现在是直接写成类的形式
public class ServiceProvider {
    private Map<String,Object> serviceProvider;
    private String host;
    private int port;
    private ZkServiceRegister zkServiceRegister;
    public ServiceProvider(String host, int port){
        serviceProvider = new HashMap<String, Object>();
        this.host = host;
        this.port = port;
        this.zkServiceRegister = new ZkServiceRegister();
    }
    public  Map<String,Object> getServiceProvider(){
        return serviceProvider;
    }
    public void fillServiceProvider(Object service){
        // 要获得该实现类的所有的接口的名字,而不是实现类的名字
//        String name = service.getClass().getName();
        // ok
        Class<?>[] interfaces = service.getClass().getInterfaces();
        for (Class c : interfaces){
            // 本机注册服务： 将接口名(服务名) 和 本地的服务对应的impl实现类对应起来
            serviceProvider.put(c.getName(),service);
            // 注册到zookeeper上
            zkServiceRegister.register(c.getName(),new InetSocketAddress(host,port));
        }

    }
}
