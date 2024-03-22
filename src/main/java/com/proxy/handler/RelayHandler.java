package com.proxy.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RelayHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = Logger.getLogger(RelayHandler.class.getName());


    private Channel relayChannel;

    private InetSocketAddress inetSocketAddress;

    public RelayHandler(Channel relayChannel){
        this.relayChannel=relayChannel;
        inetSocketAddress = (InetSocketAddress) relayChannel.remoteAddress();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        InetSocketAddress ctxInetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        LOGGER.info("Forward message source: "+getAddress(ctxInetSocketAddress) + " target:" + getAddress(inetSocketAddress));
        relayChannel.writeAndFlush(msg);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        relayChannel.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if(cause instanceof SocketException){
            LOGGER.log(Level.WARNING, "客户端地址: "+inetSocketAddress.toString()+" 连接异常: "+cause.getMessage());
        }else {
            LOGGER.log(Level.SEVERE,"客户端地址: "+inetSocketAddress.toString()+" 转发异常: "+cause.getMessage());
        }
    }


    private String getAddress(InetSocketAddress inetSocketAddress){
        return inetSocketAddress.toString().replace("/","");
    }
}
