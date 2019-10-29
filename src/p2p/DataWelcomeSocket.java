package p2p;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

class DataWelcomeSocket extends p2p implements Runnable {
    private ServerSocket socket;

    DataWelcomeSocket(int port) throws IOException{
        socket = new ServerSocket(port);
        p2p.welcomeSockets.add(socket);
    }

    public void run(){
        while(!socket.isClosed()){
            try{
                Thread thread = new Thread(new DataSocket(socket.accept(), null, true));
                thread.start();
            } catch(SocketException e) {
                System.out.println("Exiting socket error");
            } catch(IOException e) {
                System.out.println("Issue setting up socket");
            }
        }

    }
}