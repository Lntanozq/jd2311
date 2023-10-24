package com.briup.env.ui;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

// button textField pane label table

public class MainFrame extends JFrame {
    private JScrollPane scpDemo;
    private JTableHeader jth;
    private JTable tabDemo;
    private JButton btnShow;
    private JButton bt1;
    private JButton bt2;

    {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public MainFrame() {
        super("生成环境数据表");        //JFrame的标题名称
        this.setSize(1000, 600);        //控制窗体大小
        this.setLayout(null);        //自定义布局
        this.setLocation(400, 100);    //点击运行以后，窗体在屏幕的位置
        this.scpDemo = new JScrollPane();
        this.bt1 = new JButton("确定");
        this.bt2 = new JButton("取消");
        this.btnShow = new JButton("显示数据");
        this.bt1.setBounds(100, 480, 100, 30);
        this.bt2.setBounds(380, 480, 100, 30);
        this.scpDemo.setBounds(10, 50, 980, 400);    //设置滚动框大小
        this.btnShow.setBounds(10, 10, 120, 30);    //设置按钮
        this.btnShow.addActionListener(new ActionListener()    //给“显示数据”按钮添加事件响应。
        {
            public void actionPerformed(ActionEvent ae) {
                btnShow_ActionPerformed(ae);
            }
        });
        /********按钮“确定”的响应*******/
        this.bt1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        /******按钮 “取消”的响应*****/
        this.bt2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        /******* 将组件加入到窗体中******/
        add(this.scpDemo);
        add(this.btnShow);
        add(this.bt1);
        add(this.bt2);
        this.setVisible(true);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        new MainFrame();
    }

    /***连接数据库并显示到表格中***/
    public void btnShow_ActionPerformed(ActionEvent ae) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://127.0.0.1:3306/env?serverTimezone=Asia/Shanghai&useServerPrepStmts=true";//注意设置时区
            String username = "root";
            String passwords = "123456";
            Connection conn = DriverManager.getConnection(url, username, passwords);
            String sql = "select * from env_detail_20";
            PreparedStatement pstm = conn.prepareStatement(sql);
            ResultSet rs = pstm.executeQuery();
            int count = 0;
            while (rs.next()) {
                count++;
            }
            rs = pstm.executeQuery();
            // 将查询获得的记录数据，转换成适合生成JTable的数据形式
            Object[][] info = new Object[count][10];
            String[] title = {"环境名称", "发送端id", "树莓派系统id", "模块上传感器地址", "传感器个数","指令标号","环境数据","状态标识","采集时间"};
            count = 0;
            while (rs.next()) {
                info[count][0] = rs.getString("name");
                info[count][1] = rs.getString("srcId");
                info[count][2] = rs.getString("dstId");
                info[count][3] = rs.getString("sersorAddress");
                info[count][4] = rs.getInt("count");
                info[count][5] = rs.getString("cmd");
                info[count][6] = rs.getFloat("data");
                info[count][7] = rs.getInt("status");
                info[count][8] = rs.getTimestamp("gather_date");
                count++;
            }
            // 创建JTable
            this.tabDemo = new JTable(info, title);
            // 显示表头
            this.jth = this.tabDemo.getTableHeader();
            // 将JTable加入到带滚动条的面板中
            this.scpDemo.getViewport().add(tabDemo);
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            JOptionPane.showMessageDialog(null, "数据源错误", "错误", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            JOptionPane.showMessageDialog(null, "数据操作错误", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}


