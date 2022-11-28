package github.rpc.server;


import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;
import java.util.Map;

public class NettyRpcServerHandler extends SimpleChannelInboundHandler {
    private Map<String,Object> serviceProvider;
    NettyRpcServerHandler(Map<String,Object> serviceProvider){
        this.serviceProvider = serviceProvider;
    }
    // 服务端的处理逻辑，此处收到的是解码好了的RpcRequest对象
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcRequest rpcRequest = (RpcRequest) msg;
        System.out.println("服务端收到Request请求" + rpcRequest);
        RpcResponse response = getResponse(rpcRequest);
        ctx.writeAndFlush(response);
        // 发送关闭连接事件
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private RpcResponse getResponse(RpcRequest rpcRequest) throws Exception {
        String interfaceName = rpcRequest.getInterfaceName();
        Object service = serviceProvider.get(interfaceName);
        Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsType());
        Object invoke = method.invoke(service, rpcRequest.getParams());
        RpcResponse response = RpcResponse.success(invoke);
        return response;
    }
}
