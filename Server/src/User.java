
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class User {
    /*
     * 这个类是用来建立用户对象的 一个用户类中应该包含这样一些功能 接收/发送数据 所以使用另一个线程发送数据 主线程接收数据
     */
    /******************** 变量表 *********************/
    /******************** 对象定义变量表 *********************/
    public int user_id;
    private Socket user_Socket;
    private DataBay core_databay;
    /******************** 全局变量表 *********************/
    /******************** public *********************/
    public ArrayList<Message> message_list;
    public boolean message_checkpoint = false;
    public boolean checkpoint = true;
    public String user_name;
    /******************** private *********************/
    private boolean debugmode = true;
    private OutputStream ostream;
    private PrintWriter pwriter;
    private InputStream istream;
    private BufferedReader breader;
    // private Thread thread_send;

    /*
     * 构造函数
     */
    public User(Socket m_user_Socket, DataBay m_databay) {
        user_Socket = m_user_Socket;
        core_databay = m_databay;
        message_list = new ArrayList<Message>();
        user_id=0;
    }

    /*
     * 我觉得有必要建立测试用的推送机制 所以写了这个函数用于debug
     */
    public void cout(String m_output) {// 实在不知道起什么名字所以就用了cout，我就不信cout在java里面能重名
        if (debugmode == true) {
            System.out.println(m_output);
        }
    }

    /*
     * 数据上传函数 把接收到的数据上传到ServerCore其他User的信息队列中
     */
    public void pushMessage(Message m_message_push) {
        User user_target = null;
        if ((user_target = core_databay.getUser(m_message_push.target_id)) != null) {
            cout("pushMessage(Message m_message_push):not null start push to " + m_message_push.target_id);
            cout("pushMessage(Message m_message_push):user_target.userid " + user_target.user_id);
            user_target.message_list.add(m_message_push);// 向目标列表添加数据
            cout("pushMessage(Message m_message_push):" + user_name + "message pushed");
            user_target.message_checkpoint = true;
        } else {// 发送反馈数据
            cout("pushMessage(Message m_message_push):user is null");
            // pushMessage(m_message_push);
        }
    }

    /*
     * 初始化发送数据函数 只需要初始化一次
     */
    public void initSend() {
        try {
            ostream = user_Socket.getOutputStream();
            pwriter = new PrintWriter(ostream);
            cout("function Send inited");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * 初始化数据接收函数 只需要初始化一次
     */
    public void initReceive() {
        try {
            istream = user_Socket.getInputStream();
            breader = new BufferedReader(new InputStreamReader(istream));
            cout("function Receive inited");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * 初始化函数
     */
    public void init() {
        initSend();
        initReceive();
    }

    /*
     * 数据发送函数 将要发送的数据发送至ServerCore类的二维数组里 传输协议是 user_id_message_send_time
     */
    public void sendMessage_A() {// 向物理上的用户发送数据
        // cout("sendMessage_A():start");
        if (message_checkpoint == true) {
            try {
                Message message_send = message_list.get(0);
                pwriter.write(message_send.message + "\n");
                cout(user_name + "sendMessage():sended");
                pwriter.flush();// 清除缓冲区
                cout(user_name + "sendMessage():flushed");
                message_list.remove(0);
            } catch (Exception e) {
                cout("no message");
                message_checkpoint = false;
                // e.printStackTrace();//去除了报错提示
            }
        }
    }

    /*
     * 重载函数，用来发送最初的握手信息和服务器的提示信息
     */
    public void sendMessage_M(String message_send) {// 向物理上的用户发送数据
        pwriter.write(message_send + "\n");
        cout(user_name + "sendMessage(String message_send):sended");
        pwriter.flush();// 清除缓冲区
        cout(user_name + " sendMessage(String message_send):flushed");
    }

    /*
     * 接受数据处理函数 接受的数据是一个数组 后期可以考虑加入校验程序
     */
    public Message receiveMessage() {// 从物理上的用户接受的信息
        try {
            String message;
            message = breader.readLine();
            if (Message.checkMessage(message) == 0) {// 增加验证消息准确性
                Message message_receive = new Message(message);
                message_receive.dealMessage();// 这样就可以使用message对象的处理信息函数找出user_id
                return message_receive;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * 手动接收消息
     */
    public String receiveMessage_M() {
        try {
            String message;
            message = breader.readLine();
            return message;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /*
     * 验证密码
     */
    public boolean checkPasswd(Message m_message) {
        String passwd=core_databay.getPasswd(m_message.sender_id);
        if(m_message.getMessage().equals(passwd)) {
            cout("checkPasswd(Message m_message):pass");
            return true;
        }else {
            cout("checkPasswd(Message m_message):passwd not ture");
            sendMessage_M("password error");
            return false;
        }
    }

    /*
     * 登陆验证
     */
    public boolean checkTicket() {
        for(int i=0;i<5;i++) {
            String message=receiveMessage_M();
            cout("checkTicket():received message is:"+message);
            if(Message.checkMessage(message)!=1) {
                sendMessage_M("message error");
                cout("checkTicket():message error");
                continue;
            }
            Message check_message=new Message(message);
            check_message.dealMessage();
            if(checkPasswd(check_message)==true) {
                user_id=check_message.sender_id;
                user_name=core_databay.getName(user_id);
                cout("checkTicket():User: "+user_name+" access");
                sendMessage_M("Access");
                sendMessage_M(user_name);
                return true;
            }
            cout("checkTicket():password error and this is the :"+i+" try");
        }
        sendMessage_M("error too more");
        cout("checkTicket():error too more");
        return false;
    }

    /*
     * 下面是用户类的主程序 包含创建新的线程 交换数据
     */
    public Runnable send_Runnable = new Runnable() {// 定义一个线程runnable

        @Override
        public void run() {
            cout("send thread start");
            while (checkpoint == true) {
                sendMessage_A();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    cout("sleep failed");
                    e.printStackTrace();
                }
            }
        }
    };

    /*
     * 对象启动函数 暂不使用
     */
    public void start() {
        Thread thread_send = new Thread(send_Runnable);// 启动发送信息的线程
        thread_send.start();
        while (checkpoint) {
            Message message_receive = receiveMessage();
            cout("start():message_receive.message:" + message_receive.message);
            pushMessage(message_receive);
        }
    }
}
