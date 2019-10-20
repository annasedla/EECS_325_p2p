package p2p;

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

//TODO organize 10 - 15 and fill up correctly

public class p2p {

    // global variables
    public static Peer myself; // IP address of me
    public static ArrayList<Peer> myNeighbors = new ArrayList<Peer>(); // my neighbors IP

    public static void readFromInput(String line){
        myself =

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
                System.out.println(l_neighbors);

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

