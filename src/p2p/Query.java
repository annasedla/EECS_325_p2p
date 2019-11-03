package p2p;

public class Query {

    // Private fields
    private String queryID;
    private char queryType;
    private String queryMessage;
    private Peer sourceSocket;

    /**
     * Class constructor
     * @param queryID ID of the query
     * @param queryType type of query, R, Q or H
     * @param queryMessage message - file we are searching for
     */
    public Query(String queryID, Peer socket, char queryType, String queryMessage){
        this.queryID = queryID;
        this.queryType = queryType;
        this.queryMessage = queryMessage;
        this.sourceSocket = socket;
    }

    /**
     * Query ID setter
     * @return id of this query
     */
    private String getQueryID() {
        return queryID;
    }

    /**
     * Query ID getter
     * @param queryID id of this query
     */
    void setQueryID(String queryID) {
        this.queryID = queryID;
    }

    /**
     * Query type setter
     * @return type of this query, Q, R or H
     */
    char getQueryType() {
        return queryType;
    }

    /**
     * Query type getter
     * @param queryType Q, R or H
     */
    void setQueryType(char queryType) {
        this.queryType = queryType;
    }

    /**
     * Query message getter
     * @return name of the file
     */
    String getQueryMessage() {
        return queryMessage;
    }

    /**
     * Query message setter
     * @param queryMessage name of the file
     */
    public void setQueryMessage(String queryMessage) {
        this.queryMessage = queryMessage;
    }

    /**
     * Get source socket of this query
     * @return source socket of query
     */
    Peer getSourceSocket() {
        return sourceSocket;
    }

    /**
     * Source socket setter
     * @param sourceSocket source socket of query
     */
    void setSourceSocket(Peer sourceSocket) {
        this.sourceSocket = sourceSocket;
    }

    /**
     * Method that creates query in a string format
     * @return String of the query
     */
    public String toString(){
        if (queryType == ('Q')){
            return "Q:(" + queryID + ");(" + queryMessage + ")";
        }

        else if (queryType == 'R'){
            return "R:(" + queryID + ");" + queryMessage;
        } else {
            return "H";
        }
    }

    /**
     * Compare two queries based on their ids
     * @param id query id
     * @return whether they match
     */
    public boolean equals(String id) {
        return (queryID.equals(id));
    }

    /**
     * Compare two queries based on the objects themselves
     * @param query other query
     * @return whether they match
     */
    public boolean equals(Query query) {
        return (queryID.equals(query.getQueryID()));
    }
}
