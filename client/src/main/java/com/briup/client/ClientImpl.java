package com.briup.client;

import com.briup.smart.env.Configuration;
import com.briup.smart.env.client.Client;
import com.briup.smart.env.entity.Environment;
import com.briup.smart.env.support.ConfigurationAware;
import com.briup.smart.env.support.PropertiesAware;
import com.briup.smart.env.util.Log;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.Properties;

public class ClientImpl implements Client, PropertiesAware, ConfigurationAware {
    // 定义一个端口号
    private int port;
    // 定义一个ip地址
    private String ip;
    // 定义日志对象
    private Log log;

    @Override
    public void send(Collection<Environment> collection) throws Exception {
        // 1、定义Socket
        Socket socket = new Socket(ip,port);
        // 2、使用对象流
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(collection);
        oos.flush();
        oos.close();
        socket.close();
    }

    @Override
    public void setConfiguration(Configuration configuration) throws Exception {
        log = configuration.getLogger();
    }

    @Override
    public void init(Properties properties) throws Exception {
        port = Integer.parseInt(properties.getProperty("client-port"));
        ip = properties.getProperty("client-ip");
    }
}
