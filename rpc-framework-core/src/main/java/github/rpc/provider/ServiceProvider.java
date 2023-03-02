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
    private int weight;
    private ZkServiceRegister zkServiceRegister;
    public ServiceProvider(){
        serviceProvider = new HashMap<String, Object>();
        this.zkServiceRegister = SingletonFactory.getInstance(ZkServiceRegister.class);
        host = IpUtils.getRealIp();
        // 端口和权重均先固定在此，后续可以将其动态配置
        port = 8100;
        weight = 10;
    }

    public  Map<String,Object> getServiceProvider(){
        return serviceProvider;
    }
    public void fillServiceProvider(Object service,String group,String version){
        Map<String,String> parameters = new HashMap<>();
        parameters.put("weight",String.valueOf(weight));
        Class<?>[] interfaces = service.getClass().getInterfaces();
        for (Class c : interfaces){
            // 本机注册服务： 将接口名(服务名) 和 本地的服务对应的impl实现类对应起来
            serviceProvider.put(c.getName() + group + version ,service);
            // 注册到zookeeper上,服务结点上线
            zkServiceRegister.register(c.getName()  + group  +  version,host,String.valueOf(port),parameters);
            // 注册服务的路由策略  "/xxxService/route/ip1=>ip2"
            zkServiceRegister.registerRoute(c.getName() + group  +  version);
        }

    }
}
