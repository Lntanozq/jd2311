package com.briup.smart.env.impl;

import com.briup.smart.env.Configuration;
import com.briup.smart.env.client.Client;
import com.briup.smart.env.client.Gather;
import com.briup.smart.env.server.DBStore;
import com.briup.smart.env.server.Server;
import com.briup.smart.env.support.ConfigurationAware;
import com.briup.smart.env.support.PropertiesAware;
import com.briup.smart.env.util.Backup;
import com.briup.smart.env.util.Log;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConfigurationImpl implements Configuration {
    // 准备一个map集合用来返回对象
    Map<String,Object> map = new HashMap<>();
    // 准备一个单例
    private static final ConfigurationImpl configuration = new ConfigurationImpl();
    // 准备一个properties对象（里面可以存放多个键值对）
    private final Properties properties = new Properties();

    public ConfigurationImpl() {
        // 所有的主逻辑，都在构造器里写
        // 使用dom4j，读取xml文件内容，读到全限类名，输出
        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            InputStream in = ConfigurationImpl.class.getClassLoader().getResourceAsStream("conf.xml");
            document = saxReader.read(in);

            // 获取根节点 -> 获取子节点
            Element rootElement = document.getRootElement();
            List<Element> elements = rootElement.elements();
            for (Element element : elements) {
                // 获取element中叫做class的属性
                Attribute aClass = element.attribute("class");
                // 获取对应的反射对象
                Class<?> clazz = Class.forName(aClass.getValue());
                // 反射获得空参构造器
                Constructor<?> constructor = clazz.getConstructor();
                // 反射创建对象 -> (Object)
                Object o = constructor.newInstance();
                // 放入到集合中 标签名字作为key，对象作为value
                map.put(element.getName(),o);

                // 遍历二级子节点，给properties做赋值
                List<Element> elements1 = element.elements();
                for (Element element1 : elements1) {
                    // 遍历第二级子节点的标签名字作为properties的key，标签值就作为value
                    properties.setProperty(element1.getName(),element1.getText());
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // 遍历集合进行判断
        map.forEach((k,v) ->{
            // init 和 setConfiguration
            // 判断 v instanceOf ，如果属于就强转，调用方法
            if(v instanceof ConfigurationAware){
                try {
                    // 把自己（this）set进去即可
                    ((ConfigurationAware) v).setConfiguration(this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(v instanceof PropertiesAware){
                // 准备properties对象
                try {
                    ((PropertiesAware) v).init(properties);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
    public static Configuration getInstance(){
        if(configuration != null) {
            return configuration;
        }else{
            return new ConfigurationImpl();
        }
    }

    @Override
    public Log getLogger() throws Exception {
        return (Log) map.get("logger");
    }

    @Override
    public Server getServer() throws Exception {
        return (Server) map.get("server");
    }

    @Override
    public Client getClient() throws Exception {
        return (Client) map.get("client");
    }

    @Override
    public DBStore getDbStore() throws Exception {
        return (DBStore) map.get("dbStore");
    }

    @Override
    public Gather getGather() throws Exception {
        return (Gather) map.get("gather");
    }

    @Override
    public Backup getBackup() throws Exception {
        return (Backup) map.get("backup");
    }
}
