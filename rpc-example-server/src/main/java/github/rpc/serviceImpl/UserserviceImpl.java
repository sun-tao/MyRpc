package github.rpc.serviceImpl;


import github.rpc.common.User;
import github.rpc.service.Userservice;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserserviceImpl implements Userservice {

    public User getUserById(Integer id) {
        // 模拟根据id查找用户的过程
        // 后续可以使用mybatis来实现这一过程

//        System.out.println("客户端查询了" + id + "用户");
        // 构造用户
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
}
