package com.proxy;

import com.proxy.handler.SocksRequestHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.socks.SocksInitRequestDecoder;
import io.netty.handler.codec.socks.SocksMessageEncoder;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProxyServer {

    private static final Logger LOGGER = Logger.getLogger(ProxyServer.class.getName());

    private final String lanPrefix = "192.168";

    private String host;

    private int port;

    private final int defaultPort = 11080;

    private boolean auth = false;

    public ProxyServer(){
        this(null);
    }

    public ProxyServer(String[] args){
        this.host = LANIPv4();
        this.port = getPort(args);
    }

    public static void main(String[] args) {
        ProxyServer proxyServer = new ProxyServer(args);
        proxyServer.start();
    }

    public void start(){
        final EventLoopGroup bossEventLoopGroup = new NioEventLoopGroup(2);
        final EventLoopGroup workEventLoopGroup = new NioEventLoopGroup(8);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossEventLoopGroup, workEventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new SocksInitRequestDecoder());
                            pipeline.addLast(new SocksMessageEncoder());
                            pipeline.addLast(new SocksRequestHandler(bossEventLoopGroup, isAuth()));
                        }
                    });
            if (getHost() == null) {
                return;
            }
            LOGGER.info("当前主机局域网ip:" + getHost() + "; 代理端口:" + getPort() + "; 认证:" + (isAuth() ? "需要认证" : "无需认证"));
            ChannelFuture channelFuture = serverBootstrap.bind(getHost(), getPort()).sync();
            LOGGER.info("socks5代理已启动!!!");
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,"代理服务器启动失败",e);
        } finally {
            bossEventLoopGroup.shutdownGracefully();
            workEventLoopGroup.shutdownGracefully();
        }
    }

    private int getPort(String[] args){
        if (args != null && ((args.length & 1) == 0)) {
            for (int i = 0, j = i + 1; i < args.length - 1; i++) {
                if (args[i].equals("-p")){
                    return Integer.parseInt(args[j]);
                }
            }
        }
        return defaultPort;
    }

    private String LANIPv4() {
        Enumeration<NetworkInterface> enumeration = null;
        try {
            enumeration = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            LOGGER.log(Level.SEVERE,"获取本地网卡接口失败",e);
        }
        if (enumeration == null) {
            return null;
        }
        //ipv4 地址集
        List<String> ipv4Result = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            final NetworkInterface networkInterface = enumeration.nextElement();
            final Enumeration<InetAddress> en = networkInterface.getInetAddresses();
            while (en.hasMoreElements()) {
                final InetAddress address = en.nextElement();
                if (!address.isLoopbackAddress()) {
                    if (address instanceof Inet4Address) {
                        ipv4Result.add(address.getHostAddress());
                    }
                }
            }
        }

        if (!ipv4Result.isEmpty()) {
            for (String ip : ipv4Result) {
                if (ip.startsWith(lanPrefix)) {
                    return ip;
                }
            }
        }

        return null;
    }


    public String getHost() {
        return host;
    }

    public boolean isAuth() {
        return auth;
    }

    public int getPort() {
        return port;
    }
}
