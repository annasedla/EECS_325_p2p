package p2p;
import java.net.*;

public class Peer {

    // Private variables
    private Socket socket;
    private int port;
    private long timeToLive;
    private InetAddress ipAddress;

    /**
     * Constructor for the class
     * @param socket socket of peer
     * @param port port used by peer
     * @param timeToLive time to live of the cache
     * @param ipAddress IP address of the porcess
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
    public Socket getSocket() {
        return socket;
    }

    /**
     * Socket setter
     * @param socket sets the socket
     */
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    /**
     * Port id getter
     * @return port id of this peer
     */
    public int getPort() {
        return port;
    }

    /**
     * Port id setter
     * @param port sets the port id
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     *
     * @return
     */
    public long getTimeToLive() {
        return timeToLive;
    }

    /**
     *
     * @param timeToLive
     */
    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    /**
     *
     * @return
     */
    public InetAddress getIpAddress() {
        return ipAddress;
    }

    /**
     *
     * @param ipAddress
     */
    public void setIpAddress(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String toString() {
        return ipAddress.getHostAddress() + ":" + port;
    }
}
