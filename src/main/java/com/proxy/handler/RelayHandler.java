package com.proxy.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class RelayHandler extends ChannelInboundHandlerAdapter {


    private Channel relayChannel;

    public RelayHandler(Channel relayChannel){
        this.relayChannel=relayChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        relayChannel.writeAndFlush(msg);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        relayChannel.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("异常");
        super.exceptionCaught(ctx, cause);
    }
}
