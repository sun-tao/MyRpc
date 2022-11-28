package github.rpc.service;


import github.rpc.common.User;

import java.util.List;

public interface Userservice {
    User getUserById(Integer id);

    int insertUser(int id);

    List<User> getAllUser();
}
