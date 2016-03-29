package org.projectodd.stilts.stomp.protocol.websocket;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.projectodd.stilts.stomp.protocol.StompFrame;
import org.projectodd.stilts.stomp.protocol.StompFrameCodec;
import org.projectodd.stilts.stomp.protocol.websocket.WebSocketFrame.FrameType;

public class WebSocketStompFrameEncoder extends OneToOneEncoder {

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if ( msg instanceof StompFrame ) {
            ChannelBuffer buffer = StompFrameCodec.INSTANCE.encode( (StompFrame) msg );
            WebSocketFrame webSocketFrame = new DefaultWebSocketFrame(FrameType.TEXT, buffer);
            return webSocketFrame;
        }
        return msg;
    }

}
