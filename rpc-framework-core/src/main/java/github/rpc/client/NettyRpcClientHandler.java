package github.rpc.client;


import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.common.RpcResponseHolder;
import github.rpc.common.SingletonFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import static github.rpc.client.NettyRpcClient.channelHashMap;

@Slf4j
public class NettyRpcClientHandler extends SimpleChannelInboundHandler {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcResponse response = (RpcResponse) msg;
//        System.out.println("客户端收到响应"  + response);
        log.info("客户端收到Rpc响应{}" , response);
        RpcResponseHolder rpcResponseHolder = SingletonFactory.getInstance(RpcResponseHolder.class);
        rpcResponseHolder.inject(response);
    }

    // 如果5s没有用户消息请求发送，则向服务端发送心跳包
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                RpcRequest rpcRequest = new RpcRequest();
                // 组建心跳包
                rpcRequest.setRequestId(UUID.randomUUID().toString());
                rpcRequest.setMessageType(1);
                log.info("客户端发送心跳包{}",rpcRequest);
                if (ctx.channel().isActive()){
                    ctx.writeAndFlush(rpcRequest);
                }else{
                    // todo
                }

            }
        }
        super.userEventTriggered(ctx,evt);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception{
        // 服务端主动下线,经过TCP四次挥手后下线
//        channelHashMap.remove("kubernetes.docker.internal:8100");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
