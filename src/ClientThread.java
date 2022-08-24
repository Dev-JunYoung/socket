import java.io.*;
import java.net.Socket;

public class ClientThread extends Thread{
    private BufferedReader bufferedReader;
    private PrintWriter printwriter;
    private Socket socket;
    int cnt;
    int clientCnt;
    public ClientThread(Socket socket, int cnt) { // 생성자 파라미터로 소켓받음
        this.clientCnt=cnt;
        this.socket = socket;
    }

    @Override
    public void run() {
        super.run();
        //reader를 통해 읽은 데이터 콘솔에 출력하기
        try {
            //클라이언트 쓰레드에서는 메세지를 받기만하면된다,
            //보내는것은 쓰레드안에서가 아닌
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
            //printwriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"), true);
            while(true) {
                //소켓으로부터 받아오는 데이터
                String receive = bufferedReader.readLine();
                //System.out.println("receive : "+receive);
                String[] split=receive.split(":");
                //System.out.println("split : "+split);
                if(receive == null) {
                    break;
                }else if ("message".equals(split[0])){

                    System.out.println(split[1]+":"+split[2]);
                }else if ("cnt".equals(split[0])){
                    cnt= Integer.parseInt(split[1]);
                    System.out.println("clientCnt : "+clientCnt);
                    System.out.println("cnt : "+cnt);
                }else {
                    //받아온 데이터
                    System.out.println("receive : "+receive); //대화
                }
            }

        } catch (UnsupportedEncodingException e) {
            System.out.println("error: " + e);
        } catch (IOException e) {
            System.out.println("채팅이 종료되었습니다.");
        }finally {
            try {
                if(socket != null && socket.isClosed() ==false)
                {
                    socket.close();
                }
            } catch (IOException e) {
                System.out.println("error: " + e);
            }
        }
    }
}
