package github.rpc.exporter;

import github.rpc.Invoker;

public interface Exporter {
    Invoker getInvoker();
    void setInvoker(Invoker invoker);
}
