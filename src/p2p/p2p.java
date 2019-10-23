package p2p;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

//TODO organize 10 - 15 and fill up correctly
//TODO put proper comments everywhere

public class p2p {

    // global variables to set up connections
    public static Peer myself; // IP address of me
    private static ArrayList<Peer> myNeighbors = new ArrayList<Peer>(); // my neighbors IP
    private static int queryPort;
    private static int dataPort;

    private static ArrayList<String> listOfFiles = new ArrayList<>(); //TODO rename this so it matches
    private static ArrayList<Query> queriesList = new ArrayList<Query>();

    //thread synchronization objects
    private static final Object syncObjectPeer = new Object();
    private static final Object syncObjectQuery = new Object();

    static ArrayList<Peer> connectedPeers = new ArrayList<Peer>(); //oops public static but need to keep track of peers
    static ArrayList<ServerSocket> welcomeSockets = new ArrayList<ServerSocket>(); //oops again

    private static void storeConnections(){
        Path pathToNeighbors = Paths.get("src/p2p/10/config_neighbors.txt");
        Path pathToSharing = Paths.get("src/p2p/10/config_sharing.txt");

        try {
            BufferedReader bf_neighbors = Files.newBufferedReader(pathToNeighbors, Charset.defaultCharset());
            BufferedReader bf_sharing = Files.newBufferedReader(pathToSharing, Charset.defaultCharset());

            bf_neighbors.readLine(); // skip the first line

            for (int i = 0; i < 3; i ++){
                String l_neighbors = bf_neighbors.readLine();
//                System.out.println(l_neighbors);
                p2p.readFromInput(l_neighbors);
            }

            String l_sharing = bf_sharing.readLine();

            while (l_sharing != null){
                listOfFiles.add(l_sharing);
                l_sharing = bf_sharing.readLine();
            }

            bf_neighbors.close(); // close the buffered reader since were done reading from the input
            bf_sharing.close(); // close the buffered reader since were done reading from the input

        } catch (java.io.IOException e){
            System.out.println("Error reading from file input. Buffered reader error.");
            System.exit(1);
        }
    }

    private static void readFromInput(String line){
        List<String> ipList = Arrays.asList(line.split(","));
        //System.out.println(ipList);

        if (ipList.size() > 2){
            //set the data port
            dataPort = Integer.parseInt(ipList.get(2));

            //set the query port
            queryPort = Integer.parseInt(ipList.get(1));

            //set the me peer

            try{
                myself = new Peer(InetAddress.getByName(ipList.get(0)), dataPort, null, 0);
            } catch (UnknownHostException e){
                System.out.println("Cannot resolve the host for myself.");
            }

        } else {
            try{
                myNeighbors.add(new Peer(InetAddress.getByName(ipList.get(0)),
                        Integer.parseInt(ipList.get(1)), null, 0));
            } catch (UnknownHostException e) {
                System.out.println("Cannot resolve the host for neighbors.");
            }
        }
    }

    private static void getObject(String fileName){
        Query query = new Query(UUID.randomUUID().toString(), null, 'Q', fileName);
        synchronized(syncObjectQuery)
        {
            queriesList.add(query);
        }
        sendMessage(query); //TODO check the output
    }

    private static void leave(){
        try{
            synchronized (syncObjectPeer){
                for (int i = connectedPeers.size() - 1; i >= 0; i-- ){
                    connectedPeers.get(i).getSocket().close(); //close all the sockets in connected peers
                    connectedPeers.remove(i); // remove them from the list
                }
            }
        } catch (IOException e){
            System.out.println("Cannot close socket.");
            System.exit(1);
        }
    }

    private static void exit(){

        try{
            synchronized (syncObjectPeer){
                for (int i = connectedPeers.size() - 1; i >=0; i--){
                    connectedPeers.get(i).getSocket().close();
                    connectedPeers.remove(i);
                }
            }

            for (int i = welcomeSockets.size() - 1; i >= 0; i--){
                welcomeSockets.get(i).close();
                welcomeSockets.remove(i);
            }

        } catch (IOException e){
            System.out.println("Cannot close sockets");
            System.exit(1); // exit the system
        }
    }

    static boolean sendMessage(Query query){
        //TODO implement
        return false;
    }

    public static void main(String[] args){

//        System.out.println(System.getProperty("user.dir"));
        System.out.println("Running");

        p2p.storeConnections(); // store all variables

        try
        {
            new Thread(new QueryWelcomeSocket(queryPort)).start(); // welcome socket for query
            new Thread(new DataWelcomeSocket(dataPort)).start(); // welcome socket for file transfer
        }
        catch(IOException e)
        {
            System.out.println("Error happened while trying to start welcome sockets for data and query.");
            System.out.println("run lsof -i :" + queryPort + "and kill that process.");
        }

        new Thread(new TimeOutThread(syncObjectPeer)).start(); //Time handling thread

        /*
         * USER INPUT SECTION
         */

        Scanner scanner = new Scanner(System.in);

        while (true){

            System.out.println("Enter a Command - Connect, Get");

            // variable to keep track of user input
            String input;
            input = scanner.nextLine();

            if (input.equals("Connect")){
                //establish connection to other servers
                //TODO fill here

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

