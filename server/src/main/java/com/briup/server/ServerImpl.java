package com.briup.server;

import com.briup.smart.env.Configuration;
import com.briup.smart.env.entity.Environment;
import com.briup.smart.env.server.DBStore;
import com.briup.smart.env.server.Server;
import com.briup.smart.env.support.ConfigurationAware;
import com.briup.smart.env.support.PropertiesAware;
import com.briup.smart.env.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

public class ServerImpl implements Server, ConfigurationAware, PropertiesAware {
    private boolean stop;
    private Log log;
    private int port;
    private DBStore dbStore;
    @Override
    public void receive() throws Exception {
        // 1、准备ServerSocket
        ServerSocket serverSocket = new ServerSocket(port);
        // 2、监听客户端连接
        while(!stop) {
            Socket accept = serverSocket.accept();
            // 手动管理线程  -> 使用线程池来管理
            new Thread(()->{
                // 要做什么事情
                // 接收数据
                // 使用输入流读取数据
                ObjectInputStream ois = null;
                try {
                    ois = new ObjectInputStream(accept.getInputStream());
                    Object object = ois.readObject();
                    // 类型转换（将object转换成 Collection）
                    Collection<Environment> list = new LinkedList<>();
                    if(object instanceof Collection){
                        Collection<?> coll = (Collection<?>) object;
                        for (Object o : coll) {
                            if(o instanceof Environment){
                                Environment e = (Environment) o;
                                list.add(e);
                            }
                        }
                    }

                    // 入库
                    dbStore.saveDB(list);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @Override
    public void shutdown() throws Exception {
        stop = true;
        System.exit(0);
    }

    @Override
    public void setConfiguration(Configuration configuration) throws Exception {
        log = configuration.getLogger();
        dbStore = configuration.getDbStore();
    }

    @Override
    public void init(Properties properties) throws Exception {
        port = Integer.parseInt(properties.getProperty("server-port"));
    }
}
