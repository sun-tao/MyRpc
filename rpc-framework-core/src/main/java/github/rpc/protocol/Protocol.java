package github.rpc.protocol;

import github.rpc.Invoker;
import github.rpc.annotation.Spi;
import github.rpc.common.URL;
import github.rpc.exporter.Exporter;
@Spi
public interface Protocol {
    Exporter export(Invoker invoker); // provider side
    Invoker refer(URL url);  // consumer side
}
