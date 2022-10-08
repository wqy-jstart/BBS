package com.tedu.bbs.controller;

import com.tedu.bbs.entity.User;
import com.tedu.bbs.util.DBUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@Controller
public class UserController {
    //该方法用来生成动态页面(随着数据的更新不断变换页面)
    @RequestMapping("/userList")//定义一个表单action作为页面的抽象路径,显示动态页面
    public void userList(HttpServletRequest request,HttpServletResponse response){
        System.out.println("开始生成用户列表！！！");
        /*
            1:从数据库将所有信息查询出来
            2：将每个用户信息体现到html页面的表中的一行中
         */
        try (
                Connection connection = DBUtil.getConnection()
        ){
            //获取userinfo表中的信息
            String sql ="SELECT id,username,password,nickname,age FROM userinfo";
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();//★该方法会返回一个ResultSet对象，这个对象封装了查询出来的结果集。
            //在response.getWriter()之前设置响应头，告知浏览器正文类型和字符集，避免页面乱码
            response.setContentType("text/html;charset=utf-8");
            //通过response.getWriter()获取的缓冲字符流写出的内容会作为正文发送给浏览器
            PrintWriter pw = response.getWriter();
            pw.println("<!DOCTYPE html>");
            pw.println("<html lang=\"en\">");
            pw.println("<head>");
            pw.println("    <meta charset=\"UTF-8\">");
            pw.println("    <title>用户列表</title>");
            pw.println("</head>");
            pw.println("<body>");
            pw.println("    <center>");
            pw.println("        <h1>用户列表</h1>");
            pw.println("        <table border=\"1\">");
            pw.println("            <tr>");
            pw.println("                <td>ID</td>");
            pw.println("                <td>用户名</td>");
            pw.println("                <td>密码</td>");
            pw.println("                <td>昵称</td>");
            pw.println("                <td>年龄</td>");
            pw.println("            </tr>");
            while(rs.next()) {//-------此处体现了动态页面的特点,随着用户的注册不断改变！
                int id = rs.getInt("id");//将查询到的"id"获取并赋值给id
                String userAuthor = rs.getString("username");//将查询到的"username"获取并赋值给userAuthor
                pw.println("            <tr>");
                pw.println("                <td>"+id+"</td>");//利用get请求来获取抽象路径后表单的信息,这里作者就是username
                                                  //加入一个新的超链接,定义抽象路径articleList,传递信息userId,userAuthor
                pw.println("                <td><a href='/articleList?userId="+id+"&userAuthor="+userAuthor+"' >"+userAuthor+"</a></td>");
                pw.println("                <td>"+rs.getString("password")+"</td>");
                pw.println("                <td>"+rs.getString("nickname")+"</td>");
                pw.println("                <td>"+rs.getInt("age")+"</td>");
                pw.println("            </tr>");
            }
            pw.println("        </table>");
            pw.println("    </center>");
            pw.println("</body>");
            pw.println("</html>");

            rs.close();//结果集释放
            ps.close();//执行计划释放

        }catch (SQLException | IOException throwables){
            throwables.printStackTrace();
        }
    }

    @RequestMapping("/regUser")
//(1)原始:public void reg(HttpServletRequest request,HttpServletResponse response){
//(2)MVC:public void reg(String username,String password,String nickname,int age, HttpServletResponse response) {
    public void reg(User user, HttpServletResponse response){ //(3)直接传递一个对象
        //SpringMVC提供的框架：传递过来的要是表单里面的名字不得有误,顺序可以改变,age如果指定了int型,那便只可传递int型变量——>(2)
        System.out.println("开始处理注册！");
        System.out.println("User:"+user);
//        String username = request.getParameter("username");
//        String password = request.getParameter("password");
//        String nickname = request.getParameter("nickname");
//        String ageStr = request.getParameter("age");

        //利用get获取对象中的属性
        String username = user.getUsername();
        String password = user.getPassword();
        String nickname = user.getNickname();
        int age = user.getAge();
        //必要验证
        if (username==null||username.isEmpty()||
            password==null||password.isEmpty()||
            nickname==null||nickname.isEmpty()
//            ageStr==null||ageStr.isEmpty()||
//                !ageStr.matches("[0-9]+") --------传递对象时age必须符合要求
        ){
            //信息输入有误提示错误页面
            try {
                response.sendRedirect("/reg_info_error.html");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        System.out.println("姓名："+username+",密码"+password+",昵称"+nickname+",年龄"+age);

//        int age = Integer.parseInt(ageStr);
        //2 将用户信息插入到数据库的userinfo表中
        try(
                Connection connection = DBUtil.getConnection()//与数据库建立连接
        ) {
            //判断用户是否存在
            String sql = "SELECT 1 FROM userinfo WHERE username=?";
            //查1代表如果WHERE条件成立就能显示1,反之不显示,可以用来判断WHERE条件是否成立
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1,username);
            ResultSet rs = ps.executeQuery();//★该方法会返回一个ResultSet对象，这个对象封装了查询出来的结果集。
            //如果WHERE条件成立说明此次用户名和上一次相同,可查到数字1,next()方法为true
            if (rs.next()) { //判断结果集是否有一条记录
                response.sendRedirect("/have_user.html");
                return;
            }
            String sql1 = "INSERT INTO userinfo (username,password,nickname,age) " +
                    "VALUES(?,?,?,?)";
            ps = connection.prepareStatement(sql1);
            ps.setString(1,username);
            ps.setString(2,password);
            ps.setString(3,nickname);
            ps.setInt(4,age);
            int num = ps.executeUpdate();//★该方法返回影响了多少条记录
            if (num > 0) { //若影响的条数大于0则说明插入成功
                response.sendRedirect("/reg_success.html");
            }
        }catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    //处理登录
    @RequestMapping("/loginUser")
    public void login(HttpServletRequest request,HttpServletResponse response) {
        System.out.println("开始处理登录！");
        //获取用户信息
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        System.out.println("用户名为："+username+",密码为："+password);
        //必要验证工作
        if (username==null|| username.isEmpty()||
            password==null|| password.isEmpty()){
            try {
                response.sendRedirect("/login_info_error.html");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try(
                Connection connection = DBUtil.getConnection()
        ){
            String sql = "SELECT id,username,password,nickname,age "+
                         "FROM userinfo "+
                         "WHERE username=? "+
                         "AND password=?";
            PreparedStatement ps = connection.prepareStatement(sql);
            //拿到登录需要的用户名和密码,查询信息,若能查到说明账户密码正确
            ps.setString(1,username);
            ps.setString(2,password);
            ResultSet rs = ps.executeQuery();//★该方法会返回一个ResultSet对象，这个对象封装了查询出来的结果集。
            if (rs.next()){ //该方法判断是否有一条记录
                response.sendRedirect("/login_success.html");//若能查到记录,则登录成功！
            }else {
                response.sendRedirect("/login_fail.html");//若查询不到记录,登录失败！
            }
        }catch (SQLException | IOException throwables){
            throwables.printStackTrace();
        }
    }
}
