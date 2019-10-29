package p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

public class QuerySocket extends p2p implements Runnable {

    //global variables
    private Socket socket;
    private BufferedReader bf;
    private Peer peerID;
    private static final long socketTimeOut = 300000;

    QuerySocket(Socket socket){
        synchronized (p2p.syncObjectPeer){
            this.socket = socket;

            // to create a new ID
            InetAddress inetAddress = socket.getInetAddress();
            int port = socket.getPort();

            peerID = new Peer(inetAddress, port, socket, System.currentTimeMillis() + socketTimeOut);

            try{
                bf = new BufferedReader((new InputStreamReader(socket.getInputStream())));
            } catch (IOException e){
                System.out.println("Error setting up input stream reader for query socket.");
                System.exit(1);
            }
        }
    }

    void closeConnection(){
        System.out.println("Socket closed, query socket: " + peerID);

        for (int i = 0; i < p2p.connectedPeers.size(); i++){
            if(p2p.connectedPeers.get(i).equals(peerID))
            {
                p2p.connectedPeers.remove(i);
                break;
            }
        }
    }

    void handleQuery(){

    }

    void handleResponse(){

    }

    public void run(){
        boolean continueIteration = true;

        while (continueIteration){
            try{

                String data = bf.readLine();
                if (data == null){

                    //handle close socket scenario
                    closeConnection();
                    continueIteration = false;

                } else {

                    // set time to live on the message
                    peerID.setTimeToLive(System.currentTimeMillis() + socketTimeOut);

                    if (data.charAt(0)=='H'){
                        System.out.println("Heartbeat received from:" + peerID);
                    } else if (data.charAt(0)=='Q'){
                        handleQuery();
                    } else if (data.charAt(0)=='R'){
                        handleResponse();
                    }
                }
            } catch (IOException e){
                // need to exit the thread
                continueIteration = false;

                if (!socket.isClosed()){
                    try{
                        socket.close();
                    } catch (IOException io){
                        System.out.println("Error closing query socket");
                    }
                }

                synchronized (syncObjectPeer){
                    closeConnection();
                }
            }
        }
    }
}
