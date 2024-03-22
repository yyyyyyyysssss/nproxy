package com.proxy.handler;

import com.proxy.handler.request.ProxyRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socks.SocksAddressType;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdStatus;

import java.util.logging.Logger;

public class ConnectTargetServerHandler extends SimpleChannelInboundHandler<SocksCmdRequest> {

    private static final Logger LOGGER = Logger.getLogger(ConnectTargetServerHandler.class.getName());

    private EventLoopGroup bossEventLoopGroup;

    private ProxyRequest proxyRequest;

    public ConnectTargetServerHandler(EventLoopGroup bossEventLoopGroup, ProxyRequest proxyRequest){
        this.bossEventLoopGroup=bossEventLoopGroup;
        this.proxyRequest=proxyRequest;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, SocksCmdRequest socksCmdRequest) {
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
                    LOGGER.info("目标主机："+proxyRequest.getHost()+";目标端口："+proxyRequest.getPort()+" 连接建立成功,开始转发消息");
                    channelHandlerContext.pipeline().remove(ConnectTargetServerHandler.class);
                    //向目标服务器转发消息
                    channelHandlerContext.pipeline().addLast(new RelayHandler(channelFuture.channel()));
                    channelHandlerContext.writeAndFlush(new SocksCmdResponse(SocksCmdStatus.SUCCESS,SocksAddressType.IPv4));
                }else {
                    LOGGER.info("目标主机："+proxyRequest.getHost()+";目标端口："+proxyRequest.getPort()+"连接失败!!!");
                    channelHandlerContext.writeAndFlush(new SocksCmdResponse(SocksCmdStatus.FAILURE,SocksAddressType.IPv4));
                    channelHandlerContext.close();
                }
            }
        });
    }

}
