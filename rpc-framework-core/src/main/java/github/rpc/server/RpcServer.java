package github.rpc.server;

import github.rpc.annotation.Spi;
import github.rpc.provider.ServiceProvider;

@Spi
public interface RpcServer {
    void start(int port);
    void stop();
    void setServiceProvider(ServiceProvider serviceProvider);
}
