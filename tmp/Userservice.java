package github.rpc;

import java.util.List;

public interface Userservice {
    User getUserById(Integer id);

    int insertUser(int id);

    List<User> getAllUser();
}
