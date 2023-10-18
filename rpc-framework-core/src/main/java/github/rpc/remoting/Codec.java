package github.rpc.remoting;

import github.rpc.annotation.Spi;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

@Spi
public interface Codec {
    MessageToByteEncoder getEncoder();  // 为了适配http的编码和myrpc的编码
    ByteToMessageDecoder getDecoder();
}
