package com.proxy.handler.request;

import io.netty.channel.Channel;

public class ProxyRequest {

    public ProxyRequest(){

    }

    public ProxyRequest(String host,int port,Channel clientChannel){
        this.host=host;
        this.port=port;
        this.clientChannel=clientChannel;
    }

    private String host;

    private int port;

    private Channel clientChannel;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Channel getClientChannel() {
        return clientChannel;
    }

    public void setClientChannel(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }
}
