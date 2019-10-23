package p2p;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

// close sockets if a timeout occurs
public class TimeOutThread extends p2p implements Runnable {

    //how often to check timers
    private static final int timerFreq = 60;
    private final Object syncObjectPeer;

    TimeOutThread(Object syncObjectPeer){
        this.syncObjectPeer = syncObjectPeer;
    }

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

                            // if connection has timed out
                            System.out.println("Connection to " + p2p.connectedPeers.get(i) + " has timed out");
                            p2p.connectedPeers.get(i).getSocket().close();
                            p2p.connectedPeers.remove(i);
                            i--;
                        } catch (IOException e){
                            System.out.println("Cannot close socket, error");
                        }
                    } else {

                        // send a heartbeat message to peers

                        Query HBquery = new Query("", p2p.connectedPeers.get(i), 'H', "");
                        p2p.sendMessage(HBquery); //TODO check that the output returns True

                    }
                }
            }
        }
    }
}