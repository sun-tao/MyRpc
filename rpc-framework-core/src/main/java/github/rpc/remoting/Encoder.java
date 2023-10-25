package github.rpc.remoting;

import io.netty.buffer.ByteBuf;

public interface Encoder {
    public void encode(Object msg, ByteBuf out) throws Exception;
}
