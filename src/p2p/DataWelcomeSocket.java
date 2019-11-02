package p2p;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

class DataWelcomeSocket extends p2p implements Runnable {

    //global fields
    private ServerSocket socket;

    /**
     * Constructor for the welcome socket
     * @param port Port number for the socket
     * @throws IOException In case port number is invalid
     */
    DataWelcomeSocket(int port) throws IOException{
        socket = new ServerSocket(port);
        p2p.welcomeSockets.add(socket);
    }

    /**
     * Run method for this thread
     */
    public void run(){
        while(!socket.isClosed()){
            try{
                Thread thread = new Thread(new DataSocket(socket.accept(), null, true));
                thread.start();
            } catch(SocketException e) {
                System.out.println("Exiting data welcome socket.");
            } catch(IOException e) {
                System.out.println("Issue setting up data welcome socket.");
            }
        }

    }
}