package com.briup.client;

import com.briup.smart.env.Configuration;
import com.briup.smart.env.client.Gather;
import com.briup.smart.env.entity.Environment;
import com.briup.smart.env.support.ConfigurationAware;
import com.briup.smart.env.support.PropertiesAware;
import com.briup.smart.env.util.BackupImpl;
import com.briup.smart.env.util.Log;
import org.junit.Test;

import java.io.*;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Timer;

public class GatherImpl implements Gather, PropertiesAware, ConfigurationAware {
    // 定义文件路径
    private String filePath;
    // 使用日志
    private Log log;
    @Override
    public void setConfiguration(Configuration configuration) throws Exception {
            log = configuration.getLogger();
    }
    @Override
    public void init(Properties properties) throws Exception {
        filePath = properties.getProperty("gather-file-path");
    }
    @Override
    public Collection<Environment> gather() throws Exception {
        // 定义一个用来返回结果的集合
        Collection<Environment> list = new LinkedList<>();
        // 统计一下各个数据的条数
        // 温湿度
        int count1 = 0;
        // 光照
        int count2 = 0;
        // CO2
        int count3 = 0;
        // 1、读取文件？文件输入字符流、缓冲流
        // plus：希望文件有更新的时候才采集，没有更新就不采集 -> 备份模块的方法
        // 判断备份文件是否存在？
        // 如果备份文件存在，和当前文件做个对比，看是否有更新？ 根据文件大小
        // 如果备份文件小于采集文件，从新添加的位置开始读？RandomAccessFile
        BufferedReader br = null;
        InputStream in = GatherImpl.class.getClassLoader().getResourceAsStream(filePath);
        br = new BufferedReader(new InputStreamReader(in));
        String line = null;
        while((line = br.readLine())!=null){
            //2、对数据进行一些操作
            // 将数据分割出来
            String[] split = line.split("[|]");
            String srcId = split[0];
            String desId = split[1];
            String devId = split[2];
            int count = Integer.parseInt(split[4]);
            String cmd = split[5];
            int status = Integer.parseInt(split[7]);
            long time = Long.parseLong(split[8]);
            // 根据第四部分的数据来区分环境种类
            String sensorAddress = split[3];
            Environment environment = new Environment();
            environment.setSrcId(srcId);
            environment.setDevId(devId);
            environment.setCmd(cmd);
            environment.setCount(count);
            environment.setStatus(status);
            environment.setDesId(desId);
            environment.setSensorAddress(sensorAddress);
            environment.setGatherDate(new Timestamp(time));
            if("16".equals(sensorAddress)){
                count1++;
                // 温湿度：需要拆分温度和湿度两个对象
                // 接收温湿度数据的字符串
                String tempData = split[6];
                environment.setName("温度");
                // 计算一下温度数据
                int v1 = Integer.parseInt(tempData.substring(0,4),16);
                // 使用公式得到data
                float data = (float)(v1 * 0.00268127F-46.85F);
                environment.setData(data);

                // 新创建一个对象（拷贝对象）
                Environment env = copyEnv(environment);
                env.setName("湿度");
                // 计算一下湿度数据
                int v2 = Integer.parseInt(tempData.substring(4,8),16);
                float data2 = (float)(v2*0.00190735F-6F);
                env.setData(data2);
                list.add(env);
            }else if("256".equals(sensorAddress)){
                count2++;
                // 光照强度
                environment.setName("光照强度");
                // 计算一下数据
                float data =  Integer.parseInt(split[6].substring(0,4),16);
                environment.setData(data);
            }else if("1280".equals(sensorAddress)) {
                count3++;
                // CO2浓度
                environment.setName("二氧化碳浓度");
                // 计算一下数据
                float data =  Integer.parseInt(split[6].substring(0,4),16);
                environment.setData(data);
            }
            list.add(environment);
        }
        // 输出
        log.info("本次共采集环境数据【"+list.size()+"】条，其中：");
        log.info("温度数据【"+count1+"】条，");
        log.info("湿度数据【"+count1+"】条，");
        log.info("光照强度数据【"+count2+"】条，");
        log.info("CO2浓度数据【"+count3+"】条。");

        // 采集完毕之后，记得更新备份信息
//        BackupImpl backup = new BackupImpl();
//        backup.store("client/src/main/resources/backup-file",list,true);


        return list;
    }


    private Environment copyEnv(Environment e){
        return new Environment(null,
                                e.getSrcId(),
                                e.getDesId(),
                                e.getDevId(),
                                e.getSensorAddress(),
                                e.getCount(),
                                e.getCmd(),
                                e.getStatus(),
                                0.0f,
                                e.getGatherDate());
    }

    @Test
    public void test() throws Exception {
        new GatherImpl().gather().forEach(System.out::println);
    }


}
