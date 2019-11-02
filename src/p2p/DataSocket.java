package p2p;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataSocket extends p2p implements Runnable {

    //global variables
    private Socket socket;
    private String message;
    private String fileName;
    private boolean isServer;
    //buffer size for files
    public static final int bufferSize = 100000;

    DataSocket(Socket socket, String fileName, boolean isServer){
        this.socket = socket;
        this.message = "T:(" + fileName + ")\n";
        this.fileName = fileName;
        this.isServer = isServer;
    }

    private void transferFilePeer(){

        try{
            BufferedReader bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message = bf.readLine();
            String fileName = "";
            int i = 3;

            while (message.charAt(i) != ')'){
                fileName += message.charAt(i);
                i++;
            }

            //find the correct file path based on file name
            Path path = Paths.get("p2p/shared/" + fileName);
            System.out.println("THIS IS THE PATH: " + path);

            InputStream fileInput = Files.newInputStream(path);
            OutputStream outputStream = socket.getOutputStream();

            byte[] buffer = new byte[bufferSize];
            boolean readingFromBuffer = true;

            while (readingFromBuffer){
                int streamSize = fileInput.read(buffer);
                if(streamSize == - 1) {
                    //should send all data to buffer, iterate again, then do this.
                    readingFromBuffer = false;
                } else {
                    outputStream.write(buffer, 0, streamSize);
                }
            }

            socket.close();
            fileInput.close();
            System.out.println("Finished transferring file to peer");

        } catch (IOException e){
            System.out.println("Error receiving file");
            System.exit(1);
        }


        System.out.println("Beginning to transfer file.");
    }

    private void requestFilePeer(){
        Path path = Paths.get("p2p/obtained/" + fileName);

        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            InputStream inputStream = socket.getInputStream();
            OutputStream outputFile = Files.newOutputStream(path);
            byte[] buffer = new byte[bufferSize];
            boolean readingFromBuffer = true;

            System.out.println("Requesting file.");

            dataOutputStream.writeBytes(message);

            while(readingFromBuffer) {
                int readSize = inputStream.read(buffer);
                if(readSize == -1) {
                    readingFromBuffer = false;
                } else {
                    outputFile.write(buffer, 0, readSize);
                }
            }

            //close everything
            socket.close();
            outputFile.close();

            System.out.println("Finished receiving.");
        } catch(IOException e) {
            System.out.println("Error requesting file.");
            System.exit(1);
        }
    }

    public void run(){
        if (isServer){
            transferFilePeer();
        } else {
            requestFilePeer();
        }
    }
}
