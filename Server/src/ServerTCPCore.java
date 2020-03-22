import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.*;


public class ServerTCPCore {
    /*
    这是服务器端的TCP传输核心，所有的指令都会经过TCP进行传输
     */
    /*
   变量表
     */
    /*
    定义对象
     */
    public static boolean debugmode;
    private int threadpool_type;
    private int blockedthread_max;
    private int portnumber;
    private int threadpool_thread_number;
    /*
    全局变量
     */
    //public
    public DataBay myDataBay;
    //private
    private ServerSocket myServerSocket;
    private ExecutorService myThreadPool;
    private int blockedThread_number;

    /*
    构造函数
     */
    public ServerTCPCore(int m_threadpool_type, int m_blockedthread_max, int m_portmnuber, boolean m_debugmode){
        threadpool_type = m_threadpool_type;
        blockedthread_max = m_blockedthread_max;
        portnumber = m_portmnuber;
        debugmode = m_debugmode;
        myDataBay = new DataBay();
        blockedThread_number=0;
    }
    public ServerTCPCore(int m_threadpool_type, int m_blockedthread_max, int m_portmnuber) {// 重载构造函数，默认非debug模式
        threadpool_type = m_threadpool_type;
        blockedthread_max = m_blockedthread_max;
        portnumber = m_portmnuber;
        debugmode = false;
        myDataBay = new DataBay();
        blockedThread_number=0;
    }
    /*
    建立Socket
     */
    public void setupServerSocket() {
        try {
            myServerSocket = new ServerSocket(portnumber);
            System.out.println("ServerSocket set up successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("setupServerSocket():finished");
    }
    /*
    建立线程池
     */
    public void setupCacheThreadPool() {// 建立一个拥有无数量限制的线程池
        myThreadPool = Executors.newCachedThreadPool();
        System.out.println("setupCacheThreadPool():finished");
    }

    public void setupFixedThreadPool(int m_threadpool_thread) {// 建立一个有数量限制的线程池
        myThreadPool = Executors.newFixedThreadPool(m_threadpool_thread);
        System.out.println("setupFixedThreadPool(int m_threadpool_thread):finished");
    }public void setupThreadPool(int m_threadpool_type) {// 自动检测建立线程池类型
        switch (m_threadpool_type) {
            case 0:
                setupCacheThreadPool();
                break;
            case 1:
                setupFixedThreadPool(threadpool_thread_number);
                break;
        }
        System.out.println("setupThreadPool(int m_threadpool_type):finished");
    }

    /*
     * 建立Socket链接的函数 后面要把它加入线程池的一个线程里面
     */
    public Socket setupSocket() {// 这是建立Socket链接的函数
        try {
            System.out.println("setupSocket()：waiting for connection");
            Socket linkedSocket = myServerSocket.accept();
            blockedThread_number -= 1;// 每链接成功一个就减一个，一直保持等待线程数量一定
            System.out.println("setupSocket():blockedThread_number: "+(blockedThread_number+1));
            System.out.println("setupSocket()：connected client start to communicate!");
            return linkedSocket;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /*
     * 接下来就是建立用户对象了（user） 建立对象并提供相应功能
     */
    public void service() {// 添加其他功能请重写这个
        Socket mySocket = setupSocket();
        User myUser = new User(mySocket, myDataBay);// id? 还不知道怎么获取
        myUser.init();
        if(myUser.checkTicket()==true) {
            myDataBay.user_list.add(myUser);// 把user加入到列表
            myUser.start();
        }
        myUser.checkpoint=false;
        int id=myUser.user_id;
        if(id!=0) {
            myDataBay.user_id_list.add(id);
        }
        myUser=null;
    }
    /*
     * 接下来就是任务分配部分 建立blockedThread_max个线程依次加入线程池 当有链接确认后就再新加入一个线程
     * 之所以还规定blocked线程的数量就是为了防止开局大量blocked线程加入线程池导致服务器直接崩溃
     */
    public void distrubuteThread() {// 这个函数是用来分配线程和使线程加入线程池
        while (true) {
            if(blockedThread_number < blockedthread_max) {
                Thread service_thread = new Thread(service_runnable);
                myThreadPool.submit(service_thread);
                blockedThread_number++;
                System.out.println("distrubuteThread():thread submit");
            }
            try {
                Thread.sleep(50);
            }catch(Exception e) {
                e.printStackTrace();
            }

            //cout("dist is still on");
        }
    }

    public Runnable service_runnable = new Runnable() {

        @Override
        public void run() {
            service();
        }
    };
    /*
     * 析构函数
     */
    protected void closeCore() throws Throwable {
        myServerSocket.close();//关闭端口
        myThreadPool.shutdownNow();//关闭线程池
        myDataBay.main_checkpoint=false;//指令关闭DataBay的线程
        myDataBay=null;//清除DataBay对象
        System.out.println("closeCore():ShutDown");
    }
    /*
     * 启动函数 传输核心的启动函数
     */
    public void start() {
        System.out.println("ready to go");
        setupServerSocket();
        setupThreadPool(threadpool_type);
        myDataBay.initDriver();
        myDataBay.connectDataBase();
        Thread dist_thread=new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                distrubuteThread();
            }
        });
        dist_thread.start();

    }
}
