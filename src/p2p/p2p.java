package p2p;

import java.io.BufferedReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

//TODO organize 10 - 15 and fill up correctly

public class p2p {

    // global variables
    public static Peer myself; // IP address of me
    private static ArrayList<Peer> myNeighbors = new ArrayList<Peer>(); // my neighbors IP

    private static int queryPort;
    private static int dataPort;

    private static void readFromInput(String line){
        List<String> ipList = Arrays.asList(line.split(","));
        System.out.println(ipList);

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

    public static void main(String[] args){

//        System.out.println(System.getProperty("user.dir"));
        System.out.println("Running");

        Path pathToNeighbors = Paths.get("src/p2p/10/config_neighbors.txt");
        Path pathToSharing = Paths.get("src/p2p/10/config_sharing.txt");


        try {
            BufferedReader bf_neighbors = Files.newBufferedReader(pathToNeighbors, Charset.defaultCharset());
            BufferedReader bf_sharing = Files.newBufferedReader(pathToSharing, Charset.defaultCharset());

            bf_neighbors.readLine(); // skip the first line
            bf_sharing.readLine(); // skit the first line

            for (int i = 0; i < 3; i ++){
                String l_neighbors = bf_neighbors.readLine();
//                System.out.println(l_neighbors);
                p2p.readFromInput(l_neighbors);

            }

            bf_neighbors.close(); // close the buffered reader since were done reading from the input


            String l_sharing = bf_neighbors.readLine();


            System.out.println(l_sharing);

        } catch (java.io.IOException e){
            System.out.println("Error reading from file input. Buffered reader error.");
            System.exit(1);
        }
    }
}

