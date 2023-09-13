package github.rpc.exporter;

import github.rpc.Invoker;

public class MyRpcExporter implements Exporter{
    public Invoker invoker;
    public String key; // 暴露的服务全限定名

    public MyRpcExporter(String key, Invoker invoker){
        this.invoker = invoker;
        this.key = key;
    }
    @Override
    public Invoker getInvoker() {
        return invoker;
    }

    @Override
    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }
}
