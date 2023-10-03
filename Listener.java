
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Listener {
    private static final Listener listener = new Listener();
    private static BlockingQueue<RequestInformation> requestQueue = new ArrayBlockingQueue<RequestInformation>(50);
    // private static Queue<RequestInformation> requestQueue = new PriorityQueue<RequestInformation>(50);
    
    public static Listener getListener() {
        return listener;
    }

    /*
     * Adds a new request to the queue.
     */
    public void addRequestToQueue(RequestInformation newRequest) {
        try {
            if (newRequest.getRequestType().equals("PUT")) {
                LogUtil.write("New request from Content Server " + newRequest.getContentServerId() + " added to queue.");
            } else if (newRequest.getRequestType().equals("GET") && newRequest.getContentServerId() != null) {
                LogUtil.write("New request from GETClient with process ID " + newRequest.getProcessId() + " added to queue.");
            } else {
                LogUtil.write("New request added to queue.");
            }
            addRequest(newRequest);
        } catch (Exception e) {
            LogUtil.write("Error adding request to queue: " + e.getMessage());
        }
    }

    /*
     * Returns the first request in the queue.
     */
    public RequestInformation handleRequest() {
        RequestInformation req = null;
        try {
            req = requestQueue.take();
            if (req.getRequestType().equals("PUT")) {
                LogUtil.write("Processing request from Content Server " + req.getContentServerId() + "...");
            } else if (req.getRequestType().equals("GET") && req.getContentServerId() != null) {
                LogUtil.write("Processing request from GETClient with process ID " + req.getProcessId() + "...");
            } else {
                LogUtil.write("Processing request...");
            }
        } catch (Exception e) {
            LogUtil.write("Error handling request: " + e.getMessage());
        }
        return req;
    }

    /*
     * Adds request to queue by ordering them according to the lamport
     * timestamp and process id.
     */
    public void addRequest(RequestInformation request) {
        // Normal HTTP request that is not from Content Server or GETClient.
        if (request.getContentServerId() == null) {
            requestQueue.add(request);
            return;
        }

        // Order the request queue by lamport timestamp
        BlockingQueue<RequestInformation> newQueue = null;
        if (requestQueue.isEmpty()) {
            // If request queue is empty, add request to queue.
            requestQueue.add(request);
        } else {
            newQueue = new ArrayBlockingQueue<RequestInformation>(50);

            // If request queue is not empty, compare lamport timestamp of request with lamport timestamp of requests in queue.
            while (!requestQueue.isEmpty()) {
                RequestInformation currentRequest = request;
                for (RequestInformation r : requestQueue) {
                    if (Integer.parseInt(r.getLamportTimestamp()) < Integer.parseInt(currentRequest.getLamportTimestamp())) {
                        newQueue.add(r);
                    } else if (r.getLamportTimestamp() == currentRequest.getLamportTimestamp()) {
                        if (Integer.parseInt(r.getProcessId()) < Integer.parseInt(currentRequest.getProcessId())) {
                            newQueue.add(r);
                        } else {
                            newQueue.add(currentRequest);
                            currentRequest = r;
                        }
                    } else {
                        newQueue.add(currentRequest);
                        currentRequest = r;
                    }
                }
            }
            requestQueue.clear();
            requestQueue = newQueue;
        }
    }
}