package github.rpc.route;

import java.util.List;

public interface Route {
    List<String> route(List<String> invokers,String url);
}
