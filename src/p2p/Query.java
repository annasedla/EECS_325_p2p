package p2p;

public class Query {

    // Private variables
    private String queryID;
    private char queryType;
    private String queryMessage;

    /**
     *
     * @param queryID
     * @param queryType
     * @param queryMessage
     */
    public Query(String queryID, char queryType, String queryMessage){
        this.queryID = queryID;
        this.queryType = queryType;
        this.queryMessage = queryMessage;
    }

    /**
     *
     * @return
     */
    public String getQueryID() {
        return queryID;
    }

    /**
     *
     * @param queryID
     */
    public void setQueryID(String queryID) {
        this.queryID = queryID;
    }

    /**
     *
     * @return
     */
    public char getQueryType() {
        return queryType;
    }

    /**
     *
     * @param queryType
     */
    public void setQueryType(char queryType) {
        this.queryType = queryType;
    }

    /**
     *
     * @return
     */
    public String getQueryMessage() {
        return queryMessage;
    }

    /**
     *
     * @param queryMessage
     */
    public void setQueryMessage(String queryMessage) {
        this.queryMessage = queryMessage;
    }
}
