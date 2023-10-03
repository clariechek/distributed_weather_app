


public class SinglePutTest {
    public static void main(String[] args) throws Exception {
        // Start aggregation server
        AggregationServer aggregationServer = new AggregationServer();
        aggregationServer.startServer(args);

        // Start Content server and send PUT request
        String[] contentServerArgs = new String[3];
        contentServerArgs[0] = "http://localhost:4567";
        contentServerArgs[1] = "1";
        contentServerArgs[2] = "cs1_1.txt";

        ContentServer.main(contentServerArgs);
        // Automatically shutdown in 30 seconds
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Stop the aggregation server
        aggregationServer.stopServer();
    }
}
