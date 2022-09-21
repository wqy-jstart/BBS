package com.tedu.bbs.util;

import com.alibaba.druid.pool.DruidDataSource;

import java.sql.Connection;
import java.sql.SQLException;
/**
 * 数据库工具类
 */
public class DBUtil {
    //Druid时阿里提供的数据库连接池,连接池的主要作用：1.控制连接数量 2.重用连接
    private static DruidDataSource dds;
    static {//调用该类时静态块里SQL加载路径只被加载一次
        dds = new DruidDataSource();//实例化连接池对象
        //设置连接数据库的基本信息：URL,username,password,初始连接数,最大连接数等
        dds.setUsername("root");//数据库用户名
        dds.setPassword("root");//数据库密码
        dds.setUrl("jdbc:mysql://localhost:3306/bbsdb?characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true");
        dds.setInitialSize(5);//设置初始线程数
        dds.setMaxActive(10);//设置最大连接数
    }

    /**
     * 获取一个数据库连接
     * @return
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        /*
           连接池返回的连接是在实际数据库驱动提供的连接对象上包装了一层连接池自己提供的连接对象,这个
            连接对象的close方法并不是真的断开连接，而是将当前连接归还到连接池中。
         */
        return dds.getConnection();//直接调用该方法获取连接
    }
}
