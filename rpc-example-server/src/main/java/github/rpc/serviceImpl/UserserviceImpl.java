package github.rpc.serviceImpl;


import github.rpc.Userservice;
import github.rpc.annotation.RpcService;
import github.rpc.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RpcService(group = "g1",version = "v1")
public class UserserviceImpl implements Userservice {

    public User getUserById(Integer id) {
        // 构造用户
//        try {
//            Thread.sleep(10*10000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        User user = new User();
        user.setId(id);
        user.setSex(true); // 男
        user.setUsername("hello rpc!");
        return user;
    }

    public int insertUser(int id) {
        System.out.println("成功插入用户"+id);
        return 1;
    }

    public List<User> getAllUser() {
        return null;
    }
    @Override
    public CompletableFuture<String> sayHelloAsync(String name) {
        try {
            Thread.sleep(10 * 10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture("hello" + name);
    }
}
