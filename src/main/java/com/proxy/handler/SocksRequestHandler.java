package com.proxy.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.*;

import java.util.logging.Logger;

//该Handler主要用于初始化，连接，认证等
public class SocksRequestHandler extends SimpleChannelInboundHandler<SocksRequest> {

    private static final Logger LOGGER = Logger.getLogger(SocksRequestHandler.class.getName());


    private Boolean auth;

    private final String userName="ys";

    private final String password="123456";

    private EventLoopGroup bossEventLoopGroup;

    public SocksRequestHandler(EventLoopGroup bossEventLoopGroup,Boolean auth){
        this.bossEventLoopGroup=bossEventLoopGroup;
        this.auth=auth;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SocksRequest socksRequest) {
        switch (socksRequest.requestType()){
            case INIT:
                if (auth){//需要认证时，添加密码解码器到管道首部，并返回需要认证密码报文
                    channelHandlerContext.pipeline().addFirst(new SocksAuthRequestDecoder());
                    channelHandlerContext.writeAndFlush(new SocksInitResponse(SocksAuthScheme.AUTH_PASSWORD));
                }else {//不需要认证，添加命令解码器到管道首部
                    channelHandlerContext.pipeline().addFirst(new SocksCmdRequestDecoder());
                    channelHandlerContext.writeAndFlush(new SocksInitResponse(SocksAuthScheme.NO_AUTH));
                }
                break;
            case AUTH:
                if(auth){
                    SocksAuthRequest socksAuthRequest=(SocksAuthRequest)socksRequest;
                    //认证成功，添加命令解码器到管道首部，并移除认证解码器
                    if (socksAuthRequest.username().equals(userName)&&socksAuthRequest.password().equals(password)){
                        LOGGER.info("用户认证成功");
                        channelHandlerContext.pipeline().addFirst(new SocksCmdRequestDecoder());
                        channelHandlerContext.writeAndFlush(new SocksAuthResponse(SocksAuthStatus.SUCCESS));
                    }else {
                        LOGGER.warning("账户或密码不正确");
                        channelHandlerContext.writeAndFlush(new SocksAuthResponse(SocksAuthStatus.FAILURE));
                    }
                }else {//无需认证直接返回认证成功
                    channelHandlerContext.writeAndFlush(new SocksAuthResponse(SocksAuthStatus.SUCCESS));
                }
                break;
            case CMD:
                SocksCmdRequest socksCmdRequest=(SocksCmdRequest)socksRequest;
                SocksCmdType socksCmdType = socksCmdRequest.cmdType();
                switch (socksCmdType){
                    //如果是tcp代理,添加命令处理器并移除当前处理器
                    case CONNECT :
                        channelHandlerContext.pipeline().addLast(new SocksCommandRequestHandler(bossEventLoopGroup)).remove(this);
                        //将数据传递到给命令处理器处理
                        channelHandlerContext.fireChannelRead(socksCmdRequest);
                        break;
                    //TODO udp转发
                    case UDP:

                        break;
                    default:
                        LOGGER.warning("不支持的代理方式: " + socksCmdType.name());
                        channelHandlerContext.writeAndFlush(new SocksCmdResponse(SocksCmdStatus.COMMAND_NOT_SUPPORTED,socksCmdRequest.addressType()));
                        channelHandlerContext.close();
                }
                break;
            case UNKNOWN://未知类型关闭连接
                channelHandlerContext.close();
        }
    }


}
