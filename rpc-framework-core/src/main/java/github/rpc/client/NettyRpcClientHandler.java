package github.rpc.client;


import github.rpc.common.RpcResponse;
import github.rpc.common.RpcResponseHolder;
import github.rpc.common.SingletonFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
