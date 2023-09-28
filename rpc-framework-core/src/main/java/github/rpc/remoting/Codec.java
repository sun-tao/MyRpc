package github.rpc.remoting;

import github.rpc.annotation.Spi;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

@Spi
public interface Codec {
    MessageToByteEncoder getEncoder();
    ByteToMessageDecoder getDecoder();
}
