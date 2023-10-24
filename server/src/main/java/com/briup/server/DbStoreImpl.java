package com.briup.server;

import com.briup.server.util.JDBCUtil;
import com.briup.smart.env.Configuration;
import com.briup.smart.env.entity.Environment;
import com.briup.smart.env.server.DBStore;
import com.briup.smart.env.support.ConfigurationAware;
import com.briup.smart.env.support.PropertiesAware;
import com.briup.smart.env.util.Log;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Properties;

public class DbStoreImpl implements DBStore, ConfigurationAware, PropertiesAware {

    // 维护日志对象
    private Log log;
    // 定义一个批处理的量
    private int batchSize;
    @Override
    public void saveDB(Collection<Environment> collection) throws Exception {
        // 获取jdbc连接对象
        Connection conn = JDBCUtil.getDruidConnection();
        // 定义一个语句对象
        PreparedStatement pst = null;
        // 定义一个前一天的变量
        int preDay = 0;
        // 定义一个统计个数的变量
        int count = 0;
        // 1、集合遍历，取出每一个对象
        for (Environment environment : collection) {
            count++;
            // 2、根据时间中的天数，插入到不同的表里
            int day = getDay(environment.getGatherDate());
            // System.out.println(day); // 测试的时候，可以临时打包client引入测试
            // 根据天数来构建插入语句
            String sql = "insert into env_detail_"+day+" values(?,?,?,?,?,?,?,?,?)";
            // 控制pst的数量，最多只有31个
            if(pst == null) {
                pst = conn.prepareStatement(sql);
            }else{
                if (preDay != day) {
                    pst.executeBatch();
                    pst.clearBatch();
                    pst = conn.prepareStatement(sql);
                    preDay = day;
                }
            }
            // 加入数据
            pst.setString(1,environment.getName());
            pst.setString(2,environment.getSrcId());
            pst.setString(3,environment.getDesId());
            pst.setString(4,environment.getSensorAddress());
            pst.setInt(5,environment.getCount());
            pst.setString(6,environment.getCmd());
            pst.setFloat(8,environment.getData());
            pst.setInt(7,environment.getStatus());
            pst.setTimestamp(9,environment.getGatherDate());
            // 4、批处理
            pst.addBatch();
            if(count % batchSize == 0){
                pst.executeBatch();
                pst.clearBatch();
            }
        }
        if(pst != null && count % batchSize !=0) {
            pst.executeBatch();
            pst.clearBatch();
        }

        System.out.println("入库成功");
        // 资源关闭
        JDBCUtil.close(conn,pst,null);
    }

    private int getDay(Timestamp timestamp){
        // 获取当天23点的时间戳
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
        calendar.set(Calendar.MILLISECOND,999);
        long timeInMillis = calendar.getTimeInMillis();
        Calendar c1 = Calendar.getInstance();
        // System.out.println(calendar == c1);
        c1.setTime(timestamp);
        int day = c1.get(Calendar.DAY_OF_MONTH);
//        if(timestamp.getTime() > timeInMillis){
//            return day-1;
//        }
        return day;
    }

    @Test
    public void test(){
        Timestamp timestamp = new Timestamp(2018,1,26,23,59,59,999);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        int i = calendar.get(Calendar.DAY_OF_MONTH);
        System.out.println(i);

        System.out.println(getDay(timestamp));
    }

    @Override
    public void setConfiguration(Configuration configuration) throws Exception {
        log = configuration.getLogger();
    }

    @Override
    public void init(Properties properties) throws Exception {
        batchSize = Integer.parseInt(properties.getProperty("batch-size"));
    }
}
