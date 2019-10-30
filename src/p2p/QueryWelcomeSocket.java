package p2p;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

public class QueryWelcomeSocket extends p2p implements Runnable{

    private ServerSocket socket;
    QueryWelcomeSocket(int port) throws IOException
        {
            socket = new ServerSocket(port);
            p2p.welcomeSockets.add(socket);
        }

    public void run()
    {
        System.out.println("Run method executed by child thread of query welcome socket.");
        while(!socket.isClosed()){
            try{
                Thread thread = new Thread(new QuerySocket(socket.accept()));
                thread.start();
            } catch(SocketException e) {
                    System.out.println("Exiting query welcome socket error.");
            } catch(IOException e) {
                    System.out.println("Issue setting up query welcome socket.");
            }
        }
    }
}
