package com.tedu.bbs.controller;

import com.tedu.bbs.util.DBUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Controller
public class ArticleController {

    @RequestMapping("/lookArticle")//传入链接的抽象路径处理传递的信息,执行查看文章的业务,显示动态页面
    public void lookArticle(HttpServletRequest request,HttpServletResponse response){
        System.out.println("开始处理查看文章功能！！！");//该功能所需要的信息已经具备,故不需要执行SQL语句来获取其他信息
        String content = request.getParameter("content");//接收通过request获得的表单信息
        System.out.println("文章内容为："+content);
        try {
            //通过PrintWriter和指定响应的类型及字符集来写HTML页面
            response.setContentType("text/html;charset=utf-8");
            PrintWriter pw = response.getWriter();
            pw.println("<!DOCTYPE html>");
            pw.println("<html lang=\"en\">");
            pw.println("<head>");
            pw.println("    <meta charset=\"UTF-8\">");
            pw.println("    <title>文章内容</title>");
            pw.println("</head>");
            pw.println("<body>");
            pw.println("<center>");
            pw.println("    <h1>文章内容</h1>");
            pw.println("    <table border=\"1\">");
            pw.println("        <tr>");
            pw.println("            <td><center>文章内容</center></td>");
            pw.println("        </tr>");
            pw.println("        <tr>");
            pw.println("            <td>"+content+"</td>");
            pw.println("        </tr>");
            pw.println("    </table>");
            pw.println("</center>");
            pw.println("</body>");
            pw.println("</html>");

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @RequestMapping("/articleList")//传入链接的抽象路径处理传递的信息,执行处理文章列表的业务,显示动态页面
    public void articleList(HttpServletRequest request,HttpServletResponse response){
        System.out.println("开始处理文章列表！！！");
        String author = request.getParameter("userAuthor");
        String uid = request.getParameter("userId");
        System.out.println("用户ID为："+uid);
        System.out.println(author);
        try(
                Connection connection = DBUtil.getConnection()//与SQL建立联系
        ){
//              如果没有提前通过get请求获取
//            String sql = "SELECT u.username,a.id,a.title " +
//                    "FROM userinfo u,article a " +
//                    "WHERE u.id=a.u_id " +
//                    "AND a.u_id=?";
            String sql = "SELECT title,content " +
                         "FROM article " +
                         "WHERE u_id=?";
            PreparedStatement ps = connection.prepareStatement(sql);//传入SQL语句并生成执行计划
            ps.setInt(1,Integer.parseInt(uid));//将第一个？设置成传入转化后的int型用户ID
            ResultSet rs = ps.executeQuery();//查询结果集
            //在response.getWriter()之前设置响应头，告知浏览器正文类型和字符集，避免页面乱码
            response.setContentType("text/html;charset=utf-8");
            //通过response.getWriter()获取的缓冲字符流写出的内容会作为正文发送给浏览器
            PrintWriter pw = response.getWriter();
            pw.println("<!DOCTYPE html>");
            pw.println("<html lang=\"en\">");
            pw.println("<head>");
            pw.println("    <meta charset=\"UTF-8\">");
            pw.println("    <title>帖子</title>");
            pw.println("</head>");
            pw.println("<body>");
            pw.println("<center>");
            pw.println("    <h1>帖子信息</h1>");
            pw.println("    <table border=\"1\">");
            pw.println("        <tr>");
            pw.println("            <td>标题</td>");
            pw.println("            <td>作者</td>");
            pw.println("        </tr>");
            while (rs.next()){
                String title1 = rs.getString(1);
                String content = rs.getString(2);
                pw.println("        <tr>");
                pw.println("            <td><a href='/lookArticle?content="+content+"' >"+title1+"</a></td>");
                pw.println("            <td>"+author+"</td>");
                pw.println("        </tr>");
            }
            pw.println("    </table>");
            pw.println("</center>");
            pw.println("</body>");
            pw.println("</html>");

            rs.close();//结果集释放
            ps.close();//执行计划释放

        }catch (SQLException | IOException e){
            e.printStackTrace();
        }
    }

    @RequestMapping("/writeArticle")//传入HTML页面表单的action获取信息处理发表文章的业务
    public void write(HttpServletRequest request, HttpServletResponse response){
        System.out.println("开始处理发表文章功能！！！");
        //1 获取表单信息
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String content = request.getParameter("content");
        if (author==null || author.isEmpty() ||
            content==null || content.isEmpty()){
            try {
                response.sendRedirect("/article_info_error.html");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        try(
                Connection connection = DBUtil.getConnection()
        ){
            String sql = "SELECT username,id " +
                         "FROM userinfo " +
                         "WHERE username=?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1,author);
            ResultSet rs = ps.executeQuery();//查找,反馈结果
            if (rs.next()){
                int id = rs.getInt("id");
                String sql1 = "INSERT INTO article(title,content,u_id) VALUES(?,?,?)";
                PreparedStatement ps1 = connection.prepareStatement(sql1);
                ps1.setString(1,title);
                ps1.setString(2,content);
                ps1.setInt(3,id);
                int num = ps1.executeUpdate();//插入,影响内容
                if (num>0){//检测影响的条数>0,发表成功
                    response.sendRedirect("/write_article_success.html");
                }else {
                    response.sendRedirect("/write_article_fail.html");
                }
            }else {//没有此作者
                response.sendRedirect("/have_not_user.html");
            }
        }catch (SQLException | IOException throwable){
            throwable.printStackTrace();
        }
    }
}
