package com.briup.server.util;

import com.alibaba.druid.pool.DruidDataSourceFactory;


import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class JDBCUtil {
    // 维护一个数据源
    private static DataSource dataSource;
    // 将数据库连接的四要素提取出来变为静态常量
    public static final String CLASS_NAME;
    public static final String URL;
    public static final String USERNAME;
    public static final String PASSWORD;
    // 定义本类中用到的三个对象
    private static Connection conn;
    private static Statement st;
    private static ResultSet rs;

    static {
        // 读取配置文件内容
        InputStream in = JDBCUtil.class.getClassLoader().getResourceAsStream("druid.properties");
        // 加载输入流
        Properties properties = new Properties();
        try {
            properties.load(in);
            // 也可以根据properties创建数据源
            dataSource = DruidDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
        CLASS_NAME = properties.getProperty("className");
        URL = properties.getProperty("url");
        USERNAME = properties.getProperty("username");
        PASSWORD = properties.getProperty("password");

    }

    // 1、不使用连接池子获得连接对象
    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName(CLASS_NAME);
        conn = DriverManager.getConnection(URL, USERNAME,PASSWORD);
        return conn;
    }

    // 2、使用连接池获得连接对象
    public static Connection getDruidConnection() throws SQLException {
        conn = dataSource.getConnection();
        return conn;
    }
    // 3、执行DDL语句的方法（st）
    public static int executeDDL(String sql) throws SQLException {
        // 创建statement对象
        st = getDruidConnection().createStatement();
        return st.executeUpdate(sql);
    }

    // 4、执行DML语句的方法（st）
    public static int executeDML(String sql) throws SQLException {
        return executeDDL(sql);
    }
    // 5、执行DQL语句的方法（st）
    public static ResultSet executeDQL(String sql) throws SQLException {
        // 创建statement对象
        st = getDruidConnection().createStatement();
        return st.executeQuery(sql);
    }

    // 6、关闭的方法
    public static void close(){
        close(conn,st,rs);
    }
    public static void close(Connection conn, Statement st,ResultSet rs){
        if (rs!=null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (st!=null) {
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn!=null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
