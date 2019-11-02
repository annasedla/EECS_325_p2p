package p2p;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TimeOutThread extends p2p implements Runnable {

    //global fields
    private static final int timerFreq = 90; // heartbeat frequency
    private final Object syncObjectPeer;

    /**
     * Constructor for the time out thread
     * @param syncObjectPeer synchronization object
     */
    TimeOutThread(Object syncObjectPeer){
        this.syncObjectPeer = syncObjectPeer;
    }

    /**
     * Run method for this thread
     */
    public void run(){
        while(true){
            try{
                TimeUnit.SECONDS.sleep(timerFreq);
            } catch (Exception e){
                System.out.println("Cannot sleep. :(");
            } synchronized (syncObjectPeer){
                for (int i = 0; i < p2p.connectedPeers.size(); i++){
                    if (connectedPeers.get(i).getTimeToLive() < System.currentTimeMillis()){
                        try{

                            //If connection has timed out disconnect and remove from the list of connected peers
                            System.out.println("Closing the connection to " + p2p.connectedPeers.get(i) +
                                    " because the heartbeat did not arrive for a timeout value of " + connectedPeers.get(i).getTimeToLive());
                            p2p.connectedPeers.get(i).getSocket().close();
                            p2p.connectedPeers.remove(i);
                            i--;
                        } catch (IOException e){
                            System.out.println("Cannot close socket in TimeOutThread, error.");
                        }
                    } else {

                        //Send a heartbeat message to peers
                        Query HBquery = new Query("", p2p.connectedPeers.get(i), 'H', "");
                        p2p.sendQuery(HBquery);

                    }
                }
            }
        }
    }
}
