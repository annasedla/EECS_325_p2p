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

    public void run(){

    }


    // 	public QuerySocket(Socket socket)
    // 	{
    // 		synchronized(syncObjP)
    // 		{
    // 			this.socket = socket;
    // 			InetAddress address = socket.getInetAddress();
    // 			int port = socket.getPort();
    // 			id = new PeerId(address, port, socket, System.currentTimeMillis() + TO);
    // 			try
    // 			{
    // 				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    // 			}
    // 			catch(IOException e)
    // 			{
    // 				System.out.println("Error with datastreams");
    // 				System.exit(1);
    // 			}

    // 			peerConns.add(id);
    // 		}
    // 	}
}
