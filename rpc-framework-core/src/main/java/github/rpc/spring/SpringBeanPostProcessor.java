package github.rpc.spring;

import github.rpc.annotation.RpcReference;
import github.rpc.annotation.RpcService;
import github.rpc.client.NettyRpcClient;
import github.rpc.client.RpcClient;
import github.rpc.client.RpcClientProxy;
import github.rpc.common.SingletonFactory;
import github.rpc.extension.ExtensionLoader;
import github.rpc.provider.ServiceProvider;
import github.rpc.registry.ServiceRegister;
import github.rpc.registry.zk.ZkServiceRegister;
import github.rpc.server.NettyRpcServer;
import github.rpc.server.RpcServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {
    // 在扫描到RpcService的时候，不仅要创建服务类，还要将其进行注册
    private ServiceProvider serviceProvider;
    private RpcServer rpcServer;
    private ZkServiceRegister zkServiceRegister;
    private RpcClient rpcClient;
    public SpringBeanPostProcessor(){
        // 获取单例对象,单例模式
        this.serviceProvider = SingletonFactory.getInstance(ServiceProvider.class);
        this.zkServiceRegister = SingletonFactory.getInstance(ZkServiceRegister.class);
        // NettyRpcServer
        this.rpcServer = ExtensionLoader.getExtensionLoader(RpcServer.class).getExtension("nettyServer");
        // 采用SPI机制，动态可插拔的替换RpcClient接口的实现类
        this.rpcClient = ExtensionLoader.getExtensionLoader(RpcClient.class).getExtension("netty");
    }
    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)){
            // Spring实例化服务实现类对象的同时，注册该服务，得到serviceProvider，同时得到nettyRpcServer
            // 都工作在单例模式下
            // 添加了RpcService的服务 实现类对象
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            this.serviceProvider.fillServiceProvider(bean,rpcService.group(),rpcService.version()); // 发布服务
            this.rpcServer.setServiceProvider(serviceProvider); // 本机注册服务
        }
        return bean;
    }

    @SneakyThrows
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 搜索所有bean，找Controller这个bean的加了@RpcReference的字段，对该字段进行代理
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            if (rpcReference != null){
//                NettyRpcClient nettyRpcClient = new NettyRpcClient(zkServiceRegister);
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient,rpcReference.group(),rpcReference.version());
                Object proxy = rpcClientProxy.getProxy(declaredField.getType());
                declaredField.setAccessible(true);
                declaredField.set(bean,proxy); // 注入该UserService
            }
        }
        return bean;
    }
}
