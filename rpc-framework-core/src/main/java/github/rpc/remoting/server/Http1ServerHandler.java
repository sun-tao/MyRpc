package github.rpc.remoting.server;

import github.rpc.common.RpcRequest;
import github.rpc.common.RpcResponse;
import github.rpc.remoting.Channel;
import github.rpc.remoting.ChannelHandler;
import github.rpc.remoting.NettyChannel;
import github.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class Http1ServerHandler extends ChannelDuplexHandler {
    private ChannelHandler handler;
    private HttpRequest httpRequest;
    private String serializerType;
    private String messageType;
    private String path;
    private ByteBuf totalContent = Unpooled.buffer(200); //完整的httpcontent

    Http1ServerHandler(ChannelHandler handler){
        this.handler = handler;
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object request = null;
        if (msg instanceof HttpRequest){
            httpRequest = (HttpRequest) msg;
            if (!httpRequest.headers().isEmpty()){
                path = httpRequest.headers().get("path");
                serializerType = httpRequest.headers().get("serializer");
                messageType = httpRequest.headers().get("messageType");
            }
        }else if (msg instanceof HttpContent){
            // todo:这里body里存了全量的request，header里已经存了元数据，这里只要存调用参数即可
            HttpContent httpContent = (HttpContent) msg;
            ByteBuf content = httpContent.content(); //rpcrequest请求的二进制字节数组
            totalContent.writeBytes(content);
//            log.info("channel:{}",ctx.channel());
            if (msg instanceof LastHttpContent){
                byte[] bytes = new byte[totalContent.readableBytes()];
                totalContent.readBytes(bytes);
                Serializer serializer = Serializer.getSerializerByType(Integer.parseInt(serializerType));
                request = serializer.deserialize(bytes,Integer.parseInt(messageType));
                Channel channel = NettyChannel.getOrAddNettyChannel(ctx.channel());
                handler.received(channel,request);
            }
        }

    }
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise){
        try {
            super.write(ctx,msg,promise);
            Channel channel = NettyChannel.getOrAddNettyChannel(ctx.channel());
            handler.sent(channel,msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private RpcResponse getResponse(RpcRequest rpcRequest, boolean heartBeat){
        if (heartBeat == true){
            RpcResponse pong = RpcResponse.wrapperResult("PONG", rpcRequest.getRequestId());
            pong.setMessageType(1);
            return pong;
        }
        return null;
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
