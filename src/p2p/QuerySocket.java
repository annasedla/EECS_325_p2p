package p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

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
            p2p.connectedPeers.add(peerID);
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

    void handleQuery(String data){

        String fileName = "";
        String queryID = "";
        String message = "";
        boolean uniqueQuery = true;
        boolean hasFile = false;

        int i = 3;

        while (data.charAt(i) != ')'){
            fileName += data.charAt(i);
            i++;
        }

        i = i+3;

        while(data.charAt(i) != ')'){
            message += data.charAt(i);
            i++;
        }

        Query query = new Query(queryID, peerID, 'Q', message);

        System.out.println("Received the query");

        synchronized (syncObjectQuery){
            for (int j = 0; j < queriesList.size(); j++){
                if (query.equals(queriesList.get(j))){
                    uniqueQuery = false;
                }
            }
        }

        if (uniqueQuery){
            synchronized (syncObjectQuery){
                queriesList.add(query);
            }

            for(int j = 0; j < listOfFiles.size(); j++) {
                if(listOfFiles.get(j).equals(message)) {
                    hasFile = true;
                }
            }

            if (hasFile){
                System.out.println("Found the requested file on this peer.");
                fileName = message;
                String addr = myself.toString();

                Query r = new Query(queryID, peerID, 'R', "(" + addr + ");(" + fileName + ")");
                p2p.sendQuery(r);
            } else {
                // forward the request to other peers
                System.out.println("File not found on this peer.");
                p2p.sendQuery(query);
            }
        }
    }

    void handleResponse(String data){
        String fileName = "";
        String queryID = "";
        String message = "";
        int i = 3;

        while (data.charAt(i) != ')'){
            fileName += data.charAt(i);
            i++;
        }
        i = i +2;
        message = data.substring(i);

        System.out.println("Response received pt1.");

        synchronized (syncObjectQuery){
            for (int j = 0; j< queriesList.size(); j++){

                if(queriesList.get(j).equals(queryID)) {
                    Query currentQuery = queriesList.get(j);

                    if (currentQuery.getSourceSocket() == null) {
                        System.out.println("Response received pt2.");

                        boolean onPort = false;
                        String ip = "";
                        String portS = "";

                        for (int k = 1; message.charAt(k) != ')'; k++) {
                            if (message.charAt(k) == ':') {
                                onPort = true;
                            } else if (onPort) {
                                portS += message.charAt(k);
                            } else {
                                ip += message.charAt(k);
                            }
                        }

                        try {
                            InetAddress inetAddress = InetAddress.getByName(ip);
                            int port = Integer.parseInt(portS);
                            fileName = currentQuery.getQueryMessage();

                            try {
                                new Thread(new DataSocket(new Socket(inetAddress, port), fileName, false)).start();
                            } catch (IOException e){
                                System.out.println("CANNOT CREATE DATA SOCKET.");
                                System.exit(1);
                            }
                        } catch(UnknownHostException e) {
                            System.out.println("CANNOT FIND INET ADDRESS.");
                            System.exit(1);
                        }

                    } else {
                        Query request = new Query(queryID, currentQuery.getSourceSocket(), 'R', message);
                        System.out.println("Response forwarded.");
                        p2p.sendQuery(request);
                    }

                    queriesList.remove(j);
                    j = queriesList.size();
                }
            }
        }
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
                        handleQuery(data);
                    } else if (data.charAt(0)=='R'){
                        handleResponse(data);
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
