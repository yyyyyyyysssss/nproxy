package com.proxy.handler;

import com.proxy.handler.request.ProxyRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socks.*;

public class ConnectTargetServerHandler extends SimpleChannelInboundHandler<SocksCmdRequest> {

    private EventLoopGroup bossEventLoopGroup;

    private ProxyRequest proxyRequest;

    public ConnectTargetServerHandler(EventLoopGroup bossEventLoopGroup, ProxyRequest proxyRequest){
        this.bossEventLoopGroup=bossEventLoopGroup;
        this.proxyRequest=proxyRequest;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, SocksCmdRequest socksCmdRequest) throws Exception {
        Bootstrap bootstrap=new Bootstrap();
        bootstrap.group(bossEventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY,true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new RelayHandler(channelHandlerContext.channel()));
                    }
                });
        ChannelFuture channelFuture = bootstrap.connect(proxyRequest.getHost(), proxyRequest.getPort());
        channelFuture.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()){
                    System.out.println("目标主机："+proxyRequest.getHost()+";目标端口："+proxyRequest.getPort()+"连接成功!!!");
                    System.out.println(channelHandlerContext.channel().hashCode()+channelFuture.channel().hashCode());
                    channelHandlerContext.pipeline().remove(ConnectTargetServerHandler.class);
                    //向目标服务器转发消息
                    channelHandlerContext.pipeline().addLast(new RelayHandler(channelFuture.channel()));
                    channelHandlerContext.writeAndFlush(new SocksCmdResponse(SocksCmdStatus.SUCCESS,SocksAddressType.IPv4));
                }else {
                    channelHandlerContext.writeAndFlush(new SocksCmdResponse(SocksCmdStatus.FAILURE,SocksAddressType.IPv4));
                    channelHandlerContext.close();
                }
            }
        });
    }

}
