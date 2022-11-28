package github.rpc;

import github.rpc.annotation.RpcReference;
import github.rpc.annotation.RpcService;
import github.rpc.common.Blog;
import github.rpc.service.BlogService;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BlogController {
    @RpcReference(group = "g1",version = "v1")
    private BlogService blogService;

    public void start(){
        Blog blogByid = blogService.getBlogByid(6);
        log.info("rpc执行结果为{}",blogByid);
    }
}
