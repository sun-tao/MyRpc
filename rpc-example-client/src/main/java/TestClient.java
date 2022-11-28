import github.rpc.client.NettyRpcClient;
import github.rpc.client.RpcClientProxy;
import github.rpc.common.Blog;
import github.rpc.common.User;
import github.rpc.registry.zk.ZkServiceRegister;
import github.rpc.service.BlogService;
import github.rpc.service.Userservice;

import java.util.List;

public class TestClient {

    public static void main(String[] args) {
        // 确定客户端的通信方式，采用SimpleRpcClient来进行通信
        // -------------增加zookeeper后的改动---------
        NettyRpcClient simpleRpcClient = new NettyRpcClient(new ZkServiceRegister());   //--- netty版本改动1 ---
        // 创建客户端的请求接口的代理对象生成工厂，用此工厂的getProxy方法可以生产对应的接口的代理对象
        // 该工厂同时肩负指定客户端通信方式的责任，在创建工厂对象的时候，指定所要用的通信方式为SimpleRpcClient
        // 这样后面用此工厂生产的代理对象，所增强的方法，就会采用此通信方式
        RpcClientProxy rpcClientProxy = new RpcClientProxy(simpleRpcClient);
        // 通过上面指定的rpcClientProxy对象来代理 BlogService接口,返回BlogService接口的代理对象
        BlogService blogServiceProxy = (BlogService) rpcClientProxy.getProxy(BlogService.class);
        Blog blogByid = blogServiceProxy.getBlogByid(100);
        System.out.println(blogByid);
        // 同理，代理UserService接口，返回UserService接口的代理对象
        Userservice proxy = (Userservice) rpcClientProxy.getProxy(Userservice.class);
        User userByid = proxy.getUserById(100);
        System.out.println(userByid);

        List<User> allUser = proxy.getAllUser();
        System.out.println(allUser);
    }
}
