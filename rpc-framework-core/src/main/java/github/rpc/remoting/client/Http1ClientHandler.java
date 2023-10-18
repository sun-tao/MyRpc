package github.rpc.remoting.client;

import github.rpc.remoting.Channel;
import github.rpc.remoting.ChannelHandler;
import github.rpc.remoting.NettyChannel;
import github.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;

import java.io.EOFException;
import java.util.HashMap;
import java.util.Map;

public class Http1ClientHandler extends ChannelDuplexHandler {
    private ChannelHandler handler;
    private HttpResponse httpResponse;
    private String serializerType;
    private String messageType;
    private Map<String,Integer> serializerMapping = new HashMap<>();
    {
        serializerMapping.put("jdk",0);
        serializerMapping.put("hessian",1);
    }
    Http1ClientHandler(ChannelHandler handler){
        this.handler = handler;
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object response = null;
        if (msg instanceof HttpResponse){
            httpResponse = (HttpResponse) msg;
            if (!httpResponse.headers().isEmpty()){
                serializerType = httpResponse.headers().get("serializer");
                messageType = httpResponse.headers().get("messageType");
            }
        }else if (msg instanceof HttpContent){
            HttpContent httpContent = (HttpContent) msg;
            ByteBuf content = httpContent.content(); //rpcrequest请求的二进制字节数组
            byte[] bytes = new byte[content.readableBytes()];
            content.readBytes(bytes);
            Serializer serializer = Serializer.getSerializerByType(Integer.parseInt(serializerType));
            try {
                response = serializer.deserialize(bytes,Integer.parseInt(messageType));
            }catch (EOFException eofException){
                // do nothing
            }

        }
        Channel channel = NettyChannel.getOrAddNettyChannel(ctx.channel());
        handler.received(channel,response);
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
}
