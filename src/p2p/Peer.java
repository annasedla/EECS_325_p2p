package p2p;
import java.net.*;

public class Peer {

    //private global fields
    private Socket socket;
    private int port;
    private long timeToLive;
    private InetAddress ipAddress;

    /**
     * Constructor for the class
     * @param socket socket of peer
     * @param port port used by peer
     * @param timeToLive time to live of the cache
     * @param ipAddress IP address of the process
     */
    public Peer(InetAddress ipAddress, int port, Socket socket, long timeToLive){
        this.socket = socket;
        this.port = port;
        this.timeToLive = timeToLive;
        this.ipAddress = ipAddress;
    }

    /**
     * Socket getter
     * @return the socket object
     */
    Socket getSocket() {
        return socket;
    }

    /**
     * Socket setter
     * @param socket sets the socket
     */
    void setSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * Port id getter
     * @return port id of this peer
     */
    int getPort() {
        return port;
    }

    /**
     * Port id setter
     * @param port sets the port id
     */
    void setPort(int port) {
        this.port = port;
    }

    /**
     * TTL getter
     * @return TTL of this Peer
     */
    long getTimeToLive() {
        return timeToLive;
    }

    /**
     * TTL setter
     * @param timeToLive TTL before socket timeout
     */
    void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    /**
     * IP address getter
     * @return IP address
     */
    InetAddress getIpAddress() {
        return ipAddress;
    }

    /**
     *IP address setter
     * @param ipAddress IP address of this peer
     */
    void setIpAddress(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Converts the Peer to string with the port and IP
     * @return port and IP seperated by a : for queries
     */
    public String toString() {
        return ipAddress.getHostAddress() + ":" + port;
    }
}
