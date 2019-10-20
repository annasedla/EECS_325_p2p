package p2p;

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

//TODO organize 10 - 15 and fill up correctly

public class p2p {

    public static void main(String[] args){
    System.out.println("Running");

    Path pathToData = Paths.get("10/config_neighbors.txt");

    try {
        BufferedReader bf = Files.newBufferedReader(pathToData, Charset.defaultCharset());
        String configurationLine = bf.readLine();
    } catch (java.io.IOException e){
        System.out.println("error reading from file");
    }


    System.out.println(pathToData);

    }
}

