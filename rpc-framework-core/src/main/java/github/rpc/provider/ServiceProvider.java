package github.rpc.provider;



import github.rpc.common.SingletonFactory;
import github.rpc.registry.zk.ZkServiceRegister;
import github.rpc.util.IpUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

// provider也可以像其他一样，对外提供一个接口，现在是直接写成类的形式
public class ServiceProvider {
    private Map<String,Object> serviceProvider;
    private String host;
    private int port;
    private ZkServiceRegister zkServiceRegister;
    public ServiceProvider(){
        serviceProvider = new HashMap<String, Object>();
        this.zkServiceRegister = SingletonFactory.getInstance(ZkServiceRegister.class);
        host = IpUtils.getRealIp();
        port = 8100;
    }

    public  Map<String,Object> getServiceProvider(){
        return serviceProvider;
    }
    public void fillServiceProvider(Object service,String group,String version){
        // 要获得该实现类的所有的接口的名字,而不是实现类的名字
//        String name = service.getClass().getName();
        // ok
        Class<?>[] interfaces = service.getClass().getInterfaces();
        for (Class c : interfaces){
            // 本机注册服务： 将接口名(服务名) 和 本地的服务对应的impl实现类对应起来
            serviceProvider.put(c.getName() + group + version ,service);
            // 注册到zookeeper上,服务结点上线
            zkServiceRegister.register(c.getName()  + group  +  version,host,String.valueOf(port));
            // 注册服务的路由策略  "/xxxService/route/ip1=>ip2"
            zkServiceRegister.registerRoute(c.getName() + group  +  version);
        }

    }
}
