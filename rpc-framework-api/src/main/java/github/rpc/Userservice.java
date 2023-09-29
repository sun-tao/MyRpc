package github.rpc;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Userservice {
    User getUserById(Integer id);

    int insertUser(int id);

    List<User> getAllUser();

    CompletableFuture<String> sayHelloAsync(String name);
}
