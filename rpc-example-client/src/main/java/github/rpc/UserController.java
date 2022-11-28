package github.rpc;

import github.rpc.annotation.RpcReference;
import github.rpc.common.User;
import github.rpc.service.Userservice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class UserController {
    @RpcReference(group = "g1",version = "v1")
    private Userservice userservice;  // 被代理

    public void start(){
        User userById = userservice.getUserById(10);
        log.info("rpc执行结果为{}",userById);
        List<User> allUser = userservice.getAllUser();
        log.info("rpc执行结果为{}",allUser);
    }
}
