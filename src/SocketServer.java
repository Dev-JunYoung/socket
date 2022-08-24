import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class SocketServer {
    private static int cnt;
    private static List<Writer> listWriters = new ArrayList<Writer>();
    static HashMap<String,PrintWriter> hash; //<방제,유저닉네임>
    public static void main(String[] args){
        ServerSocket server = null;
        try {
            server=new ServerSocket();
            server.bind(new InetSocketAddress("127.0.0.1", 139));
            hash=new HashMap<String,PrintWriter>();
            while(true){
                System.out.println("===================================================================");
                System.out.println("현재 서버 방갯수 : "+hash.size()+"개");
                System.out.println("접속 기다리는중");
                Socket socket=server.accept();
                ServerThread chatThread=new ServerThread(socket,hash,listWriters);
                chatThread.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(server != null && server.isClosed() == false) {
                    server.close(); //서버소켓 닫기
                }
            } catch (IOException e) {

                e.printStackTrace();

            }
        }
    }
}
class ServerThread extends Thread{
    int cnt=0;
    Socket socket;
    String[] split;
    String code;
    BufferedReader BR;
    HashMap<String,PrintWriter> hash;
    //boolean initFlag=false;
    PrintWriter PW= null;
    String nickname;
    List<Writer> listWriters;
    public ServerThread(Socket socket, HashMap<String, PrintWriter> hash,List<Writer> listWriters) {
        System.out.println("ServerThread 생성자");
        this.socket = socket;
        this.hash = hash;
        this.listWriters=listWriters;
        hash=new HashMap<>();
        hash.put("초기방",null);
    } //생성자

    @Override
    public void run() {
        super.run();
        String line=null;
        try {
            //보낸다 PW
            //1.소켓에 대한 출력 스트림(문자) 된 것을 (OutputStreamWriter 에 의해)바이트스트림으로 변경
            //2.PrintWriter : 텍스트 출력스트림에 인쇄
            //"접속되어있는 클라이언트에게 메세지 전송"
            PW = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            //들어온다 BR
            //1.연결된 소켓으로부터 바이트스트림을 받아와서 (바이트->문자로 변환해주는) InputStreamReader 의 파라미터로 생성한다)
            //2.문자스트림을 읽을 수 있도록 BufferedReader 생성
            //"클라이언트로 부터 받아오는 스트림"  클라이언트가 보낸 메세지스트림을 읽는 객체
            BR=new BufferedReader(new InputStreamReader(socket.getInputStream())); //받아온다

            //PW.println("Please press you 방제목(코드)");
            //PW.flush();//잔류 스트림 내보내기
            while (true){
                String request = BR.readLine();
                System.out.println("request : "+request);
                if(request==null) { //클라이언트가 quit를 보내지 않고 소켓을 닫은 경우
                    System.out.println("request==null");
                    doQuit(PW);
                    break;
                }
                else if("quit".equals(request)) {
                    System.out.println("request==quit");
                    doQuit(PW);
                    break;
                }

                String[] tokens = request.split(":");
                System.out.println("tokens[0] : "+tokens[0]);
                System.out.println("tokens[1] : "+tokens[1]);

                if("join".equals(tokens[0])) {  // JOIN:안대혁\r\n
                    doJoin(tokens[1], PW);
                }
                else if("message".equals(tokens[0])) { //MESSAGE:방가;\r\n
                    if("new".equals(tokens[1])) {
                        System.out.println("cnt : "+ tokens[2]);
                        cnt= Integer.parseInt(tokens[2]);
                        broadcast("cnt:"+cnt);
                    }else {
                        doMessage(tokens[1]);
                    }
                }
                else if("quit".equals(tokens[0])) {  //QUIT\r\n
                    doQuit(PW);
                }
                else {
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void enterRoom(String code) {
        this.code=code;
        String data = code +"방에 입장했습니다..";
        //hash.put(code,nickname);
        hash.size();
        //broadcast(data);
        //addWriter(PW);
        PW.println(data); //방 참여가 성공했다는 것을 클라이언트에게 알려줘야한다.
        PW.flush();
        PW.println("enterRoom:ok");
        PW.flush();
    }

    private void doJoin(String nickName, Writer writer) { //닉네임을 등록하기 위한 요청 메서드
        this.nickname = nickName;
        String data = nickname +"님이 참여하였습니다.";
        broadcast(data);
        //writer pool에 현재 스레드의 writer인 printWriter를 저장해야한다
        addWriter(writer); // writer pool 에 저장
        PW.println(data); //방 참여가 성공했다는 것을 클라이언트에게 알려줘야한다.
        PW.flush();
        PW.println("join:ok");
        PW.flush();
    }


    private void addWriter(Writer writer) {
        synchronized (listWriters) { //synchronized는 여러 스레드가 하나의 공유 객체에 접근할때 동기화를 보장해준다.
            listWriters.add(writer); //list인 writer Pool 에 파라미터로 받은 writer를 추가한다.
        }
    }
    private void broadcast(String data) { // 스레드간 공유 객체인 listWriters에 접근하기때문에 동기화 처리를 해주어야함

        synchronized (listWriters) {
            for(Writer writer : listWriters) {
                PrintWriter printWriter = (PrintWriter)writer;
                printWriter.println(hash);
                printWriter.flush();
            }

        }
    }
    private void doMessage(String message) { //메시지를 전달하기 위한 요청 메서드
        broadcast("message:"+this.nickname + ":" +message+ ":" +cnt);
    }

    private void doQuit(Writer writer) { //방을 나가기 위한 요청 메서드
        removeWriter(writer);
        String data = nickname + "님이 퇴장하였습니다.";
        broadcast(data);
    }
    private void removeWriter(Writer writer) { //현재 스레드의 writer를 Writer Pool에서 제거한다.
        synchronized (listWriters) {
            listWriters.remove(writer);
        }
    }
}
