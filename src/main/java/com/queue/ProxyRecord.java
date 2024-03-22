package com.queue;

import com.queue.common.Record;
import io.netty.channel.Channel;

public class ProxyRecord implements Record {

    private Channel channel;

    private Object msg;

    private String id;

    public ProxyRecord(){}

    public ProxyRecord(Channel channel, Object msg, String id) {
        this.channel = channel;
        this.msg = msg;
        this.id = id;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Object getMsg() {
        return msg;
    }

    public void setMsg(Object msg) {
        this.msg = msg;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String key() {
        return this.id;
    }

    @Override
    public String toString() {
        return this.id;
    }
}
