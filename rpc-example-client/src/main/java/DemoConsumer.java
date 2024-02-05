import com.alibaba.csp.sentinel.slots.block.BlockException;
import github.rpc.*;
import github.rpc.remoting.client.RpcClientProxy;
import github.rpc.common.URL;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;

@Slf4j
public class DemoConsumer {
    public static void main(String[] args) throws Exception {
        RpcClientProxy proxy = new RpcClientProxy();
        URL url1 = new URL();
        url1.setServiceName(BlogService.class.getName());
        url1.setLoadbalacne("random");
        url1.setSide("consumer");
        url1.setProtocol("myrpc");
        url1.setMock("fail:github.rpc.BlogServiceMock");
        url1.setTimeout("5000");
        proxy.setUrl(BlogService.class.getName(),url1);
        URL url2 = new URL();
        url2.setServiceName(Userservice.class.getName());
        url2.setConsumerAsync("false");
        url2.setLoadbalacne("consistentHash");
        url2.setTimeout("0"); //10s超时时间
        url2.setSide("consumer");
        url2.setProtocol("myrpc");
        proxy.setUrl(Userservice.class.getName(),url2);
        proxy.refer();

        BlogService blogService = (BlogService) proxy.getProxy(BlogService.class);

        while (true){
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();
            Blog blog = blogService.getBlogByid(5);
            System.out.println(blog);
        }
    }
}
