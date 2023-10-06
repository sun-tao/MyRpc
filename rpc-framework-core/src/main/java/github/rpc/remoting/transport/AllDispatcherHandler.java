package github.rpc.remoting.transport;

import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.remoting.Channel;
import github.rpc.remoting.ChannelHandler;
import github.rpc.remoting.ExecutorRepository;
import github.rpc.remoting.HandlerDelegate;

import java.util.concurrent.ExecutorService;

// 除了编解码+(反)序列化的所有处理都扔到myrpc线程池中
public class AllDispatcherHandler implements HandlerDelegate {
    // 最底层的handler，持有上层的DecodeHandler
    ChannelHandler handler;
    public AllDispatcherHandler(ChannelHandler handler){
        this.handler = handler;
    }
    @Override
    public void received(Channel channel, Object message) {
        ExecutorService executor = null;
        if (message instanceof RpcRequest){// server端
            executor = ExecutorRepository.createIfAbsent("provider");
        }else if (message instanceof RpcResponse){
            executor = ExecutorRepository.createIfAbsent("consumer");
        }
        if (executor == null){ //work in io thread
            handler.received(channel,message);
            return;
        }
        // 线程切换 io线程->myrpc框架线程
        executor.submit(new Runnable() {
            @Override
            public void run() {
                handler.received(channel,message);
            }
        });
    }

    @Override
    public void sent(Channel channel, Object message) {

    }

    @Override
    public ChannelHandler getChannelHandler() {
        return null;
    }
}
