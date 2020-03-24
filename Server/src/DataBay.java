import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Random;

public class DataBay {
    /*
     * 数据中间存储的对象 用来存放一些数据 后期用来实现与SQL数据库对接
     */
    /*
     * 程序逻辑打算这么设计，登陆时候进行验证，根据登录id匹配密码
     */
    /******************** 全局变量表 *********************/
    /******************** public *********************/
    public ArrayList<User> user_list;// 存储user对象的列表【index_user】
    public ArrayList<Integer> user_id_list;
    public boolean main_checkpoint;
    public boolean cleaner_checkpoint;

    /******************** private *********************/
    /******************** 数据库登录相关 *********************/
    private String database_url = "jdbc:mysql://localhost:3306/userlist"
            + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";
    private String database_username = "root";
    private String database_passwd = "zhao1996";
    private Connection connect;// 数据库的链接
    private Statement stmt;// 数据库状态

    public DataBay() {
        user_list = new ArrayList<User>();
        main_checkpoint = true;
        cleaner_checkpoint = false;
        Cleaner();
    }

    /*
     * 获取User的函数 返回User类型 用来找到相应的User并发送数据
     */
    public User getUser(int m_id) {
        for (User the_user : user_list) {
            if (the_user.user_id == m_id) {
                return the_user;
            }
        }
        return null;
    }

    /*
     * 生成user_id的函数   已废弃
     */
    public synchronized int creatId() {
        Random user_id_random = new Random();
        int id = user_id_random.nextInt();
        int i = 0;
        while (i < user_id_list.size()) {
            if (id == user_id_list.get(i)) {
                id = user_id_random.nextInt();
                i = 0;
            }
        }
        user_id_list.add(id);
        return id;
    }

    /*
     * 清理user_id列表的函数
     */
    public synchronized void cleanNullId() {
        for (int i : user_id_list) {
            if (getUser(i) == null) {
                user_id_list.remove(i);
            }
        }
    }

    /*
     * 清理user列表的函数
     */
    public synchronized void cleanNullUser() {
        for(int id=0;id<user_id_list.size();id++) {
            for(int user=0;user<user_list.size();user++) {
                if(user_list.get(user).user_id==id) {
                    user_list.remove(user);
                    user_id_list.remove(id);
                }
            }
        }
    }

    /*
     * 定时清理的程序
     */
    public void cleanList() {
        String time = ShowInfo.getTime_day();
        if (time == "00:00:00") {
            if (cleaner_checkpoint = false) {
                cleanNullUser();
                cleaner_checkpoint = true;
            }
        }
    }

    /*
     * 定时反转清洁标记的程序
     */
    public void turnCheckPoint() {
        String time = ShowInfo.getTime_day();
        if (time == "00:00:30") {
            if (cleaner_checkpoint = true) {
                cleaner_checkpoint = false;
            } else {
            }
        }
    }

    /*
     * 开辟一个新的线程监视时间
     */
    public void Cleaner() {
        Runnable cleaner_Runnable = new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (main_checkpoint) {
                    cleanList();
                    turnCheckPoint();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread cleanerThread = new Thread(cleaner_Runnable);
        cleanerThread.start();
    }

    /*
     * 数据库相关操作函数
     */
    /*
     * 更改数据库登录信息的函数
     */
    public void changeLoginInfo(String m_url, String m_username, String m_passwd) {
        database_url = m_url + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";
        database_username = m_username;
        database_passwd = m_passwd;
    }

    /*
     * 加载驱动程序函数
     */
    public void initDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // 加载MYSQL JDBC驱动程序
            // Class.forName("org.gjt.mm.mysql.Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * 链接数据库函数
     */
    public void connectDataBase() {
        try {
            connect = DriverManager.getConnection(database_url, database_username, database_passwd);
            stmt = connect.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * 从数据库获取密码
     */
    public String getPasswd(int m_id) {
        try {
            ResultSet rs = stmt.executeQuery("select * from userinfo where id=" + m_id+";");
            rs.next();// 光标指向下一行
            String password = rs.getString("password");
            return password;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * 从数据库中获取名字
     */
    public String getName(int m_id) {
        try {
            ResultSet rs = stmt.executeQuery("select * from userinfo where id=" + m_id+";");
            rs.next();// 光标指向下一行
            String user_name = rs.getString("name");
            return user_name;
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * 添加数据库验证登录功能
     */
    public boolean logIn(int m_id,String m_passwd) {
        String passwd=getPasswd(m_id);
        if(m_passwd.equals(passwd)) {
            return true;
        }
        else {
            return false;
        }
    }
    /*
     * 找到User位置功能
     */
    //public int getUserLocation(int m_user_id) {

    //}
}
