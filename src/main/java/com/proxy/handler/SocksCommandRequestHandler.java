package com.proxy.handler;

import com.proxy.handler.request.ProxyRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.SocksCmdRequest;

public class SocksCommandRequestHandler extends SimpleChannelInboundHandler<SocksCmdRequest> {

    private EventLoopGroup bossEventLoopGroup;

    public SocksCommandRequestHandler(EventLoopGroup bossEventLoopGroup){
        this.bossEventLoopGroup=bossEventLoopGroup;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SocksCmdRequest socksCmdRequest) {
        String targetHost=socksCmdRequest.host();
        int targetPort=socksCmdRequest.port();
        ProxyRequest proxyRequest=new ProxyRequest(targetHost,targetPort,channelHandlerContext.channel());
        channelHandlerContext.pipeline().addLast(new ConnectTargetServerHandler(bossEventLoopGroup,proxyRequest)).remove(this);
        channelHandlerContext.fireChannelRead(socksCmdRequest);
    }

}
