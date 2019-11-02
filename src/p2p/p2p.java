package p2p;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class p2p {

    // global fields to set up sockets
    public static Peer myself; // my IP
    private static ArrayList<Peer> myNeighbors = new ArrayList<Peer>(); // my neighbors IP
    private static int queryPort;
    private static int dataPort;

    // package accessible fields to keep track of the files on this peer as well as the queries
    static ArrayList<String> listOfFiles = new ArrayList<>();
    static ArrayList<Query> listOfQueries = new ArrayList<Query>(); // more on this in the README

    // package accessible objects for thread synchronization
    static final Object syncObjectPeer = new Object();
    static final Object syncObjectQuery = new Object();

    // package accessible fields to keep track of peers connected to this peer and the welcome sockets
    static ArrayList<Peer> connectedPeers = new ArrayList<Peer>();
    static ArrayList<ServerSocket> welcomeSockets = new ArrayList<>(); // one for queries and one for data

    /**
     * Method initially run to store the neighbors
     */
    private static void storeConnections(){

        //This changes based on the peer
        Path pathToNeighbors = Paths.get("p2p/10/config_neighbors.txt");
        Path pathToSharing = Paths.get("p2p/10/config_sharing.txt");

        try {
            BufferedReader bf_neighbors = Files.newBufferedReader(pathToNeighbors, Charset.defaultCharset());
            BufferedReader bf_sharing = Files.newBufferedReader(pathToSharing, Charset.defaultCharset());

            bf_neighbors.readLine(); // skip the first line, it is titles

            for (int i = 0; i < 3; i ++){
                String lineOfNeighbors = bf_neighbors.readLine();
                p2p.readFromInput(lineOfNeighbors);
            }

            String lineOfSharing = bf_sharing.readLine();

            while (lineOfSharing != null){
                listOfFiles.add(lineOfSharing);
                lineOfSharing = bf_sharing.readLine();
            }

            // close both of the buffer readers since we are done with them
            bf_neighbors.close();
            bf_sharing.close();

        } catch (java.io.IOException e){
            System.out.println("Error reading from file input. Buffered reader error.");
            System.exit(1);
        }
    }

    /**
     * Method to store information about peer's neighbors called by storeConnection()
     * @param line line from the BufferReader
     */
    private static void readFromInput(String line){
        List<String> ipList = Arrays.asList(line.split(","));

        if (ipList.size() > 2){
            //set the data port
            dataPort = Integer.parseInt(ipList.get(2));

            //set the query port
            queryPort = Integer.parseInt(ipList.get(1));

            try{
                // set the Peer object of this peer
                myself = new Peer(InetAddress.getByName(ipList.get(0)), dataPort, null, 0);
            } catch (UnknownHostException e){
                System.out.println("Cannot resolve the host for my IP address.");
            }

        } else {
            try{
                //add it to the neighbors
                myNeighbors.add(new Peer(InetAddress.getByName(ipList.get(0)),
                        Integer.parseInt(ipList.get(1)), null, 0));
            } catch (UnknownHostException e) {
                System.out.println("Cannot resolve the host for my neighbors.");
            }
        }
    }

    /**
     * Calls establish connection for all the neighbors
     */
    private static void connect() {
        for (int i = 0; i < myNeighbors.size(); i++) {

            boolean establishConnection = true;

            synchronized (syncObjectPeer) {
                //check if the neighbor is already in connected peers
                for (int j = 0; j < connectedPeers.size(); j++) {
                    if (myNeighbors.get(i).equals(connectedPeers.get(j))) {
                        establishConnection = false;

                    }
                }
            }
            if (establishConnection){
                establishConnection(i);
            }
        }
    }

    /**
     * Determines the result of the attempt to establish connection
     * @param peerIndex index of a peer we are trying to establish connection with
     */
    private static void establishConnection(int peerIndex){
        System.out.println("Attempting to connect to this peer: " + myNeighbors.get(peerIndex).getIpAddress());

        try{
            // make a new thread of query socket
            new Thread(new QuerySocket(new Socket(myNeighbors.get(peerIndex).getIpAddress(), myNeighbors.get(peerIndex).getPort()))).start();
            System.out.println("Successfully connected to: " +  myNeighbors.get(peerIndex).getIpAddress());
        } catch (IOException e){
            System.out.println("Failed connecting to: " + myNeighbors.get(peerIndex).getIpAddress());
        }
    }

    /**
     * When Get is called by the user the query is added to a list of queries
     * @param fileName file we are trying to retrieve from other peers
     */
    private static void getObject(String fileName){
        Query query = new Query(UUID.randomUUID().toString(), null, 'Q', fileName);
        synchronized(syncObjectQuery)
        {
            listOfQueries.add(query);
        }
        sendQuery(query);
    }

    /**
     * Ran when user types in Leave, disconnects peers
     */
    private static void leave(){
        try{
            synchronized (syncObjectPeer){
                for (int i = connectedPeers.size() - 1; i >= 0; i-- ){

                    // close all sockets and remove them from the connected peers list
                    connectedPeers.get(i).getSocket().close();
                    connectedPeers.remove(i);
                }
            }
        } catch (IOException e){
            System.out.println("Cannot close socket while leaving.");
            System.exit(1);
        }
    }

    /**
     * Ran when user types in Exit, disconnects peers, and closes welcome sockets
     */
    private static void exit(){

        try{
            synchronized (syncObjectPeer){
                for (int i = connectedPeers.size() - 1; i >=0; i--){

                    // close all sockets and remove them from the connected peers list
                    connectedPeers.get(i).getSocket().close();
                    connectedPeers.remove(i);
                }
            }

            for (int i = welcomeSockets.size() - 1; i >= 0; i--){

                //closes both welcome sockets
                welcomeSockets.get(i).close();
                welcomeSockets.remove(i);
            }

        } catch (IOException e){
            System.out.println("Cannot close sockets while exiting.");
            System.exit(1); // exit the system
        }
    }

    /**
     * Method that handles query, response, and heartbeat messages by creating the appropriate streams at the right sockets
     * @param query Heartbeat, query or response
     */
    static void sendQuery(Query query){

        String message = query.toString() + "\n";

        // heartbeat message
        if (query.getQueryType() == 'H'){
            try{
                DataOutputStream dataOutputStream = new DataOutputStream((query.getSourceSocket().getSocket().getOutputStream()));
                dataOutputStream.writeBytes(message);
                System.out.println("Heartbeat sent to neighbor: " + query.getSourceSocket());

            } catch (IOException e) {
                System.out.println("Cannot write to socket to send a query.");
                System.exit(1);
            }
        }

        // query message
        else if(query.getQueryType() == 'Q') {
            synchronized (syncObjectPeer){
                for (int i = 0; i < connectedPeers.size(); i++){
                    if (query.getSourceSocket() == null || !query.getSourceSocket().equals(connectedPeers.get(i))){
                        try{
                            DataOutputStream dataOutputStream = new DataOutputStream(connectedPeers.get(i).getSocket().getOutputStream());
                            dataOutputStream.writeBytes(message);
                            System.out.println("Query message sent to: " + connectedPeers.get(i));

                        } catch (IOException e) {
                            System.out.println("Cannot write to socket to send a query message.");
                            System.exit(1);
                        }
                    }
                }
            }
        }

        // response message
        else {
            try{
                DataOutputStream dataOutputStream = new DataOutputStream((query.getSourceSocket().getSocket().getOutputStream()));
                dataOutputStream.writeBytes(message);
                System.out.println("Query response message sent to" + query.getSourceSocket());

            } catch (IOException e) {
                System.out.println("Cannot write to socket to send a response.");
                System.exit(1);
            }
        }
    }

    public static void main(String[] args){

        // SET UP SECTION
        System.out.println("Anna's P2P network starting...");

        p2p.storeConnections(); // store all variables, port, IP

        try
        {
            new Thread(new QueryWelcomeSocket(queryPort)).start(); // welcome socket for query
            new Thread(new DataWelcomeSocket(dataPort)).start(); // welcome socket for file transfer
        }
        catch(IOException e)
        {
            System.out.println("Error happened while trying to start welcome sockets for data and query.");
        }

        new Thread(new TimeOutThread(syncObjectPeer)).start(); //Time handling thread for hearbeat and socket timeout

        // USER INPUT SECTION
        Scanner scanner = new Scanner(System.in);

        while (true){

            System.out.println("Enter a Command - Connect, Get, Leave, Exit");

            // variable to keep track of user input
            String input;
            input = scanner.nextLine();

            if (input.equals("Connect")){

                //establish connection to other peers
                connect();

            } else if (input.substring(0, 3).equals("Get")){
                String fileName = input.substring(4);
                getObject(fileName);

            } else if (input.equals("Leave")){
                leave();

            } else if (input.equals("Exit")){
                exit();
                scanner.close();
                System.exit(0);

            } else {
                System.out.println("Wrong command, retry.");
            }
        }
    }
}

