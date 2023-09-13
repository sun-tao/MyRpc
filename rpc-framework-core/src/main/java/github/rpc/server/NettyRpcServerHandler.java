package github.rpc.server;


import github.rpc.Invoker;
import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
public class NettyRpcServerHandler extends SimpleChannelInboundHandler {
    private Map<String, Invoker> exportedMap;
    NettyRpcServerHandler(Map<String,Invoker> exportedMap){
        this.exportedMap = exportedMap;
    }
    // 服务端的处理逻辑，此处收到的是解码好了的RpcRequest对象
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcRequest rpcRequest = (RpcRequest) msg;
        if (rpcRequest.getMessageType() == 0){
            // 应用层真正的Rpc请求
            log.info("服务端收到Request请求" + rpcRequest);
            RpcResponse response = getResponse(rpcRequest);
            ctx.writeAndFlush(response);
            // 测试客户端的重试机制
        }else if(rpcRequest.getMessageType() == 1){
            // 心跳包
            log.info("服务端收到心跳包{}",rpcRequest);
            RpcResponse response = getResponse(rpcRequest, true);
            ctx.writeAndFlush(response);
        }
        // 发送关闭连接事件
        // ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private RpcResponse getResponse(RpcRequest rpcRequest,boolean heartBeat){
        if (heartBeat == true){
            RpcResponse pong = RpcResponse.wrapperResult("PONG", rpcRequest.getRequestId());
            pong.setMessageType(1);
            return pong;
        }
        return null;
    }
    // 解析rpc请求的
    private RpcResponse getResponse(RpcRequest rpcRequest) throws Exception {
        String interfaceName = rpcRequest.getInterfaceName();
        Invoker invoker = exportedMap.get(interfaceName);
        Object result = invoker.doInvoke(rpcRequest);
        RpcResponse response = RpcResponse.wrapperResult(result,rpcRequest.getRequestId());
        return response;
    }

    // 如果30s没有请求，则断开当前连接
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        }
        super.userEventTriggered(ctx,evt);
    }
}
