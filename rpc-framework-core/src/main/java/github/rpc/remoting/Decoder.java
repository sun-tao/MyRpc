package github.rpc.remoting;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface Decoder {
    public Object decode(ChannelHandlerContext ctx, ByteBuf in, Object obj) throws Exception;
}
