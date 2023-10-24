package com.briup.client;

import com.briup.smart.env.Configuration;
import com.briup.smart.env.client.Client;
import com.briup.smart.env.client.Gather;
import com.briup.smart.env.entity.Environment;
import com.briup.smart.env.impl.ConfigurationImpl;

import java.util.Collection;

public class ClientMain {
    public static void main(String[] args) {
        System.out.println("客户端开启...");
        Configuration instance = ConfigurationImpl.getInstance();
        try {
            // 执行客户端发送过来的采集的数据
            Client client = instance.getClient();
            Gather gather = instance.getGather();
            // 采集（采集过程可以不断的执行）
            Collection<Environment> list = gather.gather();
            client.send(list);
        } catch (Exception e) {
            System.out.println("客户端异常，程序终止");
            e.printStackTrace();
        }
        System.out.println("客户端数据发送完毕");
    }
}



//修改备份
