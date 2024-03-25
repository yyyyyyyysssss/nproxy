package com.proxy.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Description
 * @Author ys
 * @Date 2024/3/25 10:41
 */
public class ProxyConfig {

    private static final Logger LOGGER = Logger.getLogger(ProxyConfig.class.getName());

    private static Properties properties;

    private static final ReentrantLock lock = new ReentrantLock();

    public static Properties createProperties(){
        if(properties == null){
            lock.lock();
            if(properties == null){
                properties = new Properties();
                InputStream in = ProxyConfig.class.getClassLoader().getResourceAsStream("config.properties");
                try {
                    properties.load(in);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE,"配置加载失败",e);
                    throw new RuntimeException(e);
                }
                lock.unlock();
            }
        }
        return properties;
    }

}
