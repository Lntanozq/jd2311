package com.briup.server;

import com.briup.smart.env.impl.ConfigurationImpl;
import com.briup.smart.env.server.Server;

public class ServerMain {
    public static void main(String[] args) {
        System.out.println("服务器开启...");
        Server server = null;
        try {
            server = ConfigurationImpl.getInstance().getServer();
            server.receive();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
