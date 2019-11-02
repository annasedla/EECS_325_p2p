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

    private static final long socketTimeOut = 300000; // constant for socket time out

    /**
     * Constructor for query socket
     * @param socket socket to create a new Peer
     */
    QuerySocket(Socket socket){
        synchronized (p2p.syncObjectPeer){
            this.socket = socket;

            // create a new peer ID
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

    /**
     * Close connection of this peer
     */
    private void closeConnection(){
        System.out.println("Socket closed, query socket: " + peerID);

        for (int i = 0; i < p2p.connectedPeers.size(); i++){
            if(p2p.connectedPeers.get(i).equals(peerID))
            {
                p2p.connectedPeers.remove(i);
                break;
            }
        }
    }


    /**
     * Method to handle query
     * @param data Input data
     */
    private void handleQuery(String data){

        String queryID = "";
        String message = "";
        boolean uniqueQuery = true;
        boolean hasFile = false;
        int i;

        for(i = 3; data.charAt(i) != ')'; i++) {

            //fill the query ID
            queryID += data.charAt(i);
        }

        i += 3;

        for(;data.charAt(i) != ')'; i++) {

            // fill the message
            message += data.charAt(i);
        }

        // create a new query with said message
        Query query = new Query(queryID, peerID, 'Q', message);

        System.out.println("Query received.");

        synchronized (syncObjectQuery){

            // check if the query is unique and discard if its repeated
            for (int j = 0; j < listOfQueries.size(); j++){
                if (query.equals(listOfQueries.get(j))){
                    uniqueQuery = false;
                    System.out.println("Not a unique query, ignore.");
                }
            }
        }

        if (uniqueQuery){
            synchronized (syncObjectQuery){
                listOfQueries.add(query);
            }

            for(int j = 0; j < listOfFiles.size(); j++) {

                // check if the file we are searching for is in the list of this peer's files
                if(listOfFiles.get(j).equals(message)) {
                    hasFile = true;
                }
            }

            if (hasFile){

                // File found on this peer
                System.out.println("Found the requested file on this peer.");
                String addr = myself.toString();

                // Make a query to retrieve the file
                Query response = new Query(queryID, peerID, 'R', "(" + addr + ");(" + message + ")");
                p2p.sendQuery(response);
            } else {

                // Forward the request to other peers
                System.out.println("File not found on this peer, lets forward the request.");
                p2p.sendQuery(query);
            }
        }
    }

    /**
     * Handle the response
     * @param data Input data
     */
    private void handleResponse(String data){
        String queryID = "";
        int i = 3;

        while (data.charAt(i) != ')'){
            queryID += data.charAt(i);
            i++;
        }
        i += 2;

        String message = data.substring(i);

        System.out.println("Query response received.");

        synchronized (syncObjectQuery){
            for (int j = 0; j< listOfQueries.size(); j++){

                if(listOfQueries.get(j).equals(queryID)) {

                    Query currentQuery = listOfQueries.get(j);

                    if (currentQuery.getSourceSocket() == null) {
                        System.out.println("Query response received from this peer.");

                        // break down the message for information to make a DataSocket thread
                        boolean onPort = false;
                        String ipAddress = "";
                        String portString = "";

                        for(int k = 1; message.charAt(k) != ')'; k++) {
                            if(message.charAt(k) == ':') {
                                onPort = true;
                            } else if(onPort) {
                                portString += message.charAt(k);
                            } else {
                                ipAddress += message.charAt(k);
                            }
                        }

                        try {

                            InetAddress inetAddress = InetAddress.getByName(ipAddress);
                            int port = Integer.parseInt(portString);
                            String fileName = currentQuery.getQueryMessage();

                            try {

                                // make a data socket thread to retrive file
                                new Thread(new DataSocket(new Socket(inetAddress, port), fileName, false)).start();
                            } catch (IOException e){
                                System.out.println("Error creating data socket in QuerySocket.");
                                System.exit(1);
                            }
                        } catch(UnknownHostException e) {
                            System.out.println("Error creating inet address in QuerySocket.");
                            System.exit(1);
                        }

                    } else {
                        Query request = new Query(queryID, currentQuery.getSourceSocket(), 'R', message);
                        System.out.println("Query response forwarded.");
                        p2p.sendQuery(request);
                    }

                    // remove the query from the list of queries since its processed
                    listOfQueries.remove(j);
                    j = listOfQueries.size();
                }
            }
        }
    }

    /**
     * Run method for this thread
     */
    public void run(){
        boolean continueIteration = true;

        // keep iterating until we hit an error or data is null
        while (continueIteration){
            try{
                String data = bf.readLine();

                // if the socket is null, close the connection
                if (data == null){
                    closeConnection();
                    continueIteration = false;

                } else {

                    // Time to live for peer
                    peerID.setTimeToLive(System.currentTimeMillis() + socketTimeOut);

                    if (data.charAt(0)=='H'){
                        System.out.println("Heartbeat received from neighbor: " + peerID);
                    } else if (data.charAt(0)=='Q'){
                        handleQuery(data);
                    } else if (data.charAt(0)=='R'){
                        handleResponse(data);
                    }
                }
            } catch (IOException e){
                //Exit the thread if an error occurs
                continueIteration = false;

                if (!socket.isClosed()){
                    try{
                        socket.close();
                    } catch (IOException io){
                        System.out.println("Error closing query socket.");
                    }
                }

                synchronized (syncObjectPeer){
                    closeConnection();
                }
            }
        }
    }
}
