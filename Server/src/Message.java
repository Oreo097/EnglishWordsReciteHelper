public class Message {
    /*
     * 这是信息对象 由User对象生成
     */
    /******************** 变量表 *********************/
    /******************** 对象定义变量表 *********************/
    public String message;
    /******************** 全局变量表 *********************/
    /******************** public *********************/
    public String target_name;
    public String sender_name;
    public int target_id;
    public int sender_id;
    /******************** private *********************/
    private boolean debugmode=true;

    /*
     * 构造函数
     */
    public Message(String m_message) {
        message = m_message;
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
     * 处理Message的函数 主要是处理信息 获取target_name、sender_name 信息格式为 target_name-message-sender_name
     */
    public void dealMessage() {
        String can = message;
        int target1, target2;// 索引两个“-”的位置
        target1 = can.indexOf("-");
        target2 = can.lastIndexOf("-");
        String target_id_String = can.substring(0, target1);
        String sender_id_String = can.substring(target2 + 1);
        if(check(target_id_String)) {
            this.target_id = Integer.parseInt(target_id_String);// 转换成整数
        }else {
            cout("dealMessage():target_id miss");
        }
        if(check(sender_id_String)) {
            this.sender_id = Integer.parseInt(sender_id_String);
        }else {
            cout("dealMessage():sebder_id miss");
        }
        cout("message is send to: " + target_id);
        cout("message is send from: " + sender_id);
    }
    public boolean check(String m_message) {
        if(m_message.equals("")) {
            return false;
        }
        return true;
    }
    /*
     * 取出信息的函数
     */
    public String getMessage() {
        String Message=null;
        String can = message;
        int target1, target2;// 索引两个“-”的位置
        target1 = can.indexOf("-");
        target2 = can.lastIndexOf("-");
        Message=can.substring(target1+1,target2);
        cout("Message.getmessage():"+Message);
        return Message;
    }
    /*
     * 检测信息是否正确以及属于什么样的信息第一次传输的信息必须是登录信息，登陆后传输的信息必须是会话信息，如果信息错误将会被拒绝
     */
    public static int checkMessage(String m_message) {
        int target1 = m_message.indexOf("-");
        int target2 = m_message.lastIndexOf("-");
        int target3= m_message.indexOf(":");
        if(target1==target2) {
            return -1;
        }
        String target_id_String = m_message.substring(0, target1);
        if(target_id_String.equals("")) {
            return 1;
        }
        String sender_id_String = m_message.substring(target2 + 1);
        if(sender_id_String.equals("")) {
            return -1;
        }
        String sender_name_String=m_message.substring(target1+1,target3);
        if(sender_name_String.equals("")) {
            return -1;
        }
        return 0;
    }
}
