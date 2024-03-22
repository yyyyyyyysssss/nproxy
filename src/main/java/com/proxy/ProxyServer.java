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

public class ProxyServer {

    private static final String host = LANIPv4();

    private static int port = 11080;

    private static final boolean auth = false;

    public static void main(String[] args) {
        if (args != null && ((args.length & 1) == 0)) {
            for (int i = 0, j = i + 1; i < args.length - 1; i++) {
                if (args[i].equals("-p")){
                    port=Integer.parseInt(args[j]);
                }
            }
        }
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
                            pipeline.addLast(new SocksRequestHandler(bossEventLoopGroup, auth));
                        }
                    });
            if (host == null) {
                return;
            }
            System.out.println("当前主机局域网ip:" + host + "; 代理端口:" + port + "; 认证:" + (auth ? "需要认证" : "无需认证"));
            ChannelFuture channelFuture = serverBootstrap.bind("127.0.0.1", port).sync();
            System.out.println("socks5代理已启动!!!");
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossEventLoopGroup.shutdownGracefully();
            workEventLoopGroup.shutdownGracefully();
        }

    }

    public static String LANIPv4() {
        Enumeration<NetworkInterface> enumeration = null;
        try {
            enumeration = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (enumeration == null) {
            return null;
        }
        //ipv4 地址集
        List<String> ipv4Result = new ArrayList<String>();

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
                if (ip.startsWith("192.168")) {
                    return ip;
                }
            }
        }

        return null;
    }
}
