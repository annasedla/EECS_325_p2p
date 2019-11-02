package p2p;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataSocket extends p2p implements Runnable {

    //global fields
    private Socket socket;
    private String message;
    private String fileName;
    private boolean isServer;

    private static final int bufferSize = 100000; // constant buffer file size for file transfer

    /**
     * Constructor for DataSocket
     * @param socket Socket object
     * @param fileName Name of the file to be embedded within the message
     * @param isServer boolean to determine if its server to complete file transfer or file request
     */
    DataSocket(Socket socket, String fileName, boolean isServer){
        this.socket = socket;
        this.message = "T:(" + fileName + ")\n";
        this.fileName = fileName;
        this.isServer = isServer;
    }

    /**
     * Method to transfer files between peers
     */
    private void transferFilePeer(){

        try{

            BufferedReader bf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String message = bf.readLine();

            int i = 3;

            //get the fileName from the input stream
            while (message.charAt(i) != ')'){
                sb.append(message.charAt(i));
//                fileName = fileName + message.charAt(i); TODO delete and check
                i++;
            }

            String fileName = sb.toString();

            Path path = Paths.get("p2p/shared/" + fileName);

            InputStream inputStream = Files.newInputStream(path);
            OutputStream outputStream = socket.getOutputStream();

            byte[] buffer = new byte[bufferSize];
            boolean readingFromBuffer = true;

            // read from the buffer and then write to output stream
            while (readingFromBuffer){
                int streamSize = inputStream.read(buffer);
                if(streamSize == - 1) {
                    readingFromBuffer = false;
                } else {
                    outputStream.write(buffer, 0, streamSize);
                }
            }

            //close the Socket and InputStream
            socket.close();
            inputStream.close();
            System.out.println("Completed file transmission to the requesting peer.");

        } catch (IOException e){
            System.out.println("Error receiving file in data socket.");
            System.exit(1);
        }
    }

    /**
     * Method for file requests
     */
    private void requestFilePeer(){
        Path path = Paths.get("p2p/obtained/" + fileName);

        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = Files.newOutputStream(path);
            byte[] buffer = new byte[bufferSize];
            boolean readingFromBuffer = true;

            System.out.println("Requesting a file transfer from another peer.");

            dataOutputStream.writeBytes(message);

            // read from the buffer and then write to the directory
            while(readingFromBuffer) {
                int readSize = inputStream.read(buffer);
                if(readSize == -1) {
                    readingFromBuffer = false;
                } else {
                    outputStream.write(buffer, 0, readSize);
                }
            }

            //close the Socket and OutputStream
            socket.close();
            outputStream.close();
            System.out.println("Finished receiving file from another peer.");

        } catch(IOException e) {
            System.out.println("Error requesting file in data socket.");
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
