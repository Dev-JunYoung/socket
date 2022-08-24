import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client3 {
    public static void main(String[] args) {
        Socket socket = null;
        BufferedReader BR = null;
        PrintWriter PW = null;
        boolean endFlag = false;
        String nick = null; //닉네임
        String code = null; //방제목

        BufferedReader keyboard=new BufferedReader(new InputStreamReader(System.in));
        int cnt=0;
        try {
            //1. 키보드 연결
            System.out.print("닉네임>> ");
            String nickname = keyboard.readLine(); //닉네임
            socket=new Socket();
            socket.connect(new InetSocketAddress("127.0.0.1", 139));
            //PW=보낸다
            //1.소켓에 대한 출력 스트림(문자) 된 것을 (OutputStreamWriter 에 의해)바이트스트림으로 변경
            //2.PrintWriter : 텍스트 출력스트림에 인쇄
            //"접속되어있는 클라이언트에게 메세지 전송"
            PW=new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            PW.println("join: "+ nickname);
            PW.flush(); //닉네임보냄
            //BR=들어온다
            //1.연결된 소켓으로부터 바이트스트림을 받아와서 (바이트->문자로 변환해주는) InputStreamReader 의 파라미터로 생성한다)
            //2.문자스트림을 읽을 수 있도록 BufferedReader 생성
            //"클라이언트로 부터 받아오는 스트림"  클라이언트가 보낸 메세지스트림을 읽는 객체
            BR=new BufferedReader((new InputStreamReader(socket.getInputStream())));
            //String str=BR.readLine(); // 소켓서버에서 보낸 스트림을 읽어와서 str에 저장

            //System.out.println(str); // Please press you 방제목(코드)  // In SocketServer Code
            //code=keyboard.readLine();
            //PW.println("code: "+ code);
            //PW.flush(); //닉네임보냄

            new ClientThread(socket,cnt).start();
            while(true) {
                String input = keyboard.readLine();
                if("quit".equals(input.toLowerCase())) { //입력값을 소문자로 치환한 후에 quit과 동일한지 검사
                    //Quit프로토콜 처리
                    PW.println("quit");
                    PW.flush();
                    break;
                }
                else { //입력하는 메시지들 처리
                    if("new".equals(input)){
                        cnt++;
                    }
                    PW.println("message:" + input+":"+cnt); //보낸다.
                    PW.flush();
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
