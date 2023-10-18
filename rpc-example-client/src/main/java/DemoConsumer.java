import github.rpc.*;
import github.rpc.remoting.client.RpcClientProxy;
import github.rpc.common.URL;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

@Slf4j
public class DemoConsumer {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        RpcClientProxy proxy = new RpcClientProxy();
        URL url1 = new URL();
        url1.setServiceName(BlogService.class.getName());
        url1.setLoadbalacne("consistentHash");
        url1.setSide("consumer");
        url1.setProtocol("http1");
        proxy.setUrl(BlogService.class.getName(),url1);
        URL url2 = new URL();
        url2.setServiceName(Userservice.class.getName());
        url2.setConsumerAsync("false");
        url2.setLoadbalacne("consistentHash");
        url2.setTimeout("0");
        url2.setSide("consumer");
        url2.setProtocol("http1");
        proxy.setUrl(Userservice.class.getName(),url2);
        proxy.refer();

        BlogService blogService = (BlogService) proxy.getProxy(BlogService.class);
        Userservice userservice = (Userservice) proxy.getProxy(Userservice.class);

        Blog blogByid = blogService.getBlogByid(10);
        System.out.println(blogByid);
        User userById = userservice.getUserById(10);
        System.out.println(userById);
    }
}
