package com.briup.smart.env.util;

import com.briup.smart.env.Configuration;
import com.briup.smart.env.support.ConfigurationAware;

import java.io.*;

public class BackupImpl implements Backup, ConfigurationAware {
    private Log log;
    // 维护一个file的成员变量
    private File file;
    @Override
    public Object load(String filePath, boolean del) throws Exception {
        checkFile(filePath);
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object object = ois.readObject();
        ois.close();
        fis.close();
        if(del){
            if(file.delete()) { // 这里可以能会删不掉？ 放到关闭资源的方法后面
                log.info("备份文件已被删除");
            }else {
                log.warn("备份文件未被删除，请手动删除");
            }
        }
        return object;
    }

    @Override
    public void store(String filePath, Object obj, boolean append) throws Exception {
        // 输出流
        // 对象流、文件流
        checkFile(filePath);
        FileOutputStream fos = new FileOutputStream(file,append);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(obj);
        oos.flush();
        oos.close();
        fos.close();
        log.info(file.getName()+" 文件备份成功，位置为："+file.getAbsolutePath());
    }

    @Override
    public void setConfiguration(Configuration configuration) throws Exception {
        log = configuration.getLogger();
    }

    /**
     * 验证一下filePath的有效性
     * @param filePath
     * @throws Exception
     */
    private void checkFile(String filePath) throws Exception {
        // 里面写不符合要求的逻辑，抛异常
        // 判断filePath的有效性
        if(filePath == null || filePath.length()<=0 ){
            log.error("文件路径不能为空");
            throw new Exception("文件路径不能为空");
        }
        // 输入流
        // 对象流、文件流
        file = new File(filePath);
        // 判断file是否存在
        if(!file.exists()){
            log.error("备份文件不存在");
            throw new Exception("备份文件不存在");
        }
    }
}
