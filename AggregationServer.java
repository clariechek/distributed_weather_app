
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.concurrent.Semaphore;

import org.json.JSONException;
import org.json.JSONObject;

public class AggregationServer extends Thread {
    ServerSocket serverSocket;
    boolean running = false;

    // Default port number
    int port = 4567;

    // Store request headers and body if available
    RequestInformation requestInformation = null;

    // Create semaphore so only one thread can access the database at a time.
    Semaphore semaphore = new Semaphore(1);

    // Lamport clock for synchronisation
    LamportClock lamportClock = null;

    // Reference to client socket
    Socket socket;

    public void startServer(String[] args) {
        try {
            // Check if server crashed before
            File file = new File("weather.json");
            if (file.exists()) {
                // If aggregation server crashed, restart database from weather.json file.
                DataUtil.getDataUtil().restartDatabaseAfterCrash();

                // If aggregation server crashed, restart lamport clock from LAMPORT_AGGREGATION_SERVER.txt file.
                lamportClock = LamportClock.initialiseAggregationServerClock();

                // Assign port number and start server
                if (args.length > 0) {
                    port = Integer.parseInt(args[0]);
                }
                serverSocket = new ServerSocket(port);
                System.out.println("Aggregation server restarted, listening at port " + port);
                System.out.println("You can access http://localhost:4567 now.");
            } else {
                // Initialise server as usual. Clear the log file and restart lamport clock.
                LogUtil.clear();
                lamportClock = LamportClock.initialiseAggregationServerClock();

                // Assign port number and start server
                if (args.length > 0) {
                    port = Integer.parseInt(args[0]);
                }
                serverSocket = new ServerSocket(port);
                System.out.println("Aggregation server starting up, listening at port " + port);
                System.out.println("You can access http://localhost:4567 now.");
            }
            this.start();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } 
    }

    public void stopServer() {
        running = false;
        lamportClock.deleteLamportAndPidFiles();
        DataUtil.getDataUtil().clearDatabase();
        System.out.println("Aggregation Server has been shutdown.");
        this.interrupt();
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                socket = serverSocket.accept();

                // Local reader from the client
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Parse request headers
                String line, fullRequest = "", getStationID = "", getLamportTimestamp = "", getProcessID = "";
                boolean getReq = false, putReq = false;
                int content_length = 0;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);

                    if (line.contains("GET")) {
                        // Get request
                        getReq = true;
                        fullRequest += line;
                        continue;
                    } else if (line.contains("PUT")) {
                        // Put request
                        putReq = true;
                        fullRequest += line;
                        fullRequest += "\r\n";
                        continue;
                    }

                    if (!putReq && !getReq) {
                        // Invalid request
                        Response response = new Response("Bad Request", "400", "0", "", line, "Invalid request type.", socket, true);
                        response.sendResponse();
                        in.close();
                        break;
                    } else if (putReq) {
                        if (line.contains("Content-Length:")) {
                            // End of put request headers
                            fullRequest += line;
                            String s = line.substring("Content-Length:".length()).trim();
                            content_length = Integer.parseInt(s) + 13;
                            break;
                        } else {
                            // Continue reading headers
                            fullRequest += line;
                            fullRequest += "\r\n";
                        }
                    } else {
                        if (line.equals("")) {
                            // End of get request
                            fullRequest += line;
                            break;
                        } else if (line.contains("StationID:")) {
                            fullRequest += line;
                            fullRequest += "\r\n";
                            // Get station ID
                            getStationID = line.substring("StationID:".length()).trim();
                        } else if (line.contains("LamportTimestamp:")) {
                            fullRequest += line;
                            fullRequest += "\r\n";
                            // Get lamport timestamp
                            getLamportTimestamp = line.substring("LamportTimestamp:".length()).trim();
                        } else if (line.contains("ProcessID:")) {
                            fullRequest += line;
                            fullRequest += "\r\n";
                            // Get process ID
                            getProcessID = line.substring("ProcessID:".length()).trim();
                        } else {
                            // Continue reading headers
                            fullRequest += line;
                            fullRequest += "\r\n";
                        }
                    }
                }

                // Read PUT request body
                if (putReq) {
                    char[] body = new char[content_length];
                    in.read(body, 0, content_length);
                    fullRequest += new String(body);

                    String req = fullRequest.substring(4, fullRequest.length()-9).trim();
                    req = URLDecoder.decode(req, "UTF-8");
                    if (req.endsWith("/")) {
                        req = req.substring(0, req.length() - 1);
                    }
                    LogUtil.write("PUT request" + req);

                    // Parse PUT request body into JSON object
                    String[] parsedRequest = parsePutRequest(fullRequest);
                    String content = parsedRequest[2];
                    JSONObject json;
                    try {
                        json = new JSONObject(content.toString());
                        
                        // Save JSON object to intermediate file to process later.
                        saveJsonToIntermediateFile(json);
                        
                        // Create Request Information object.
                        requestInformation = new RequestInformation(fullRequest, socket, "PUT", req, json.get("station_id").toString(), json.get("lamport_timestamp").toString(), json.get("process_id").toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (getReq) {
                    if (getStationID == "" || getLamportTimestamp == "" || getProcessID == "") {
                        // Normal GET
                        String req = fullRequest.substring(4, fullRequest.length()-9).trim();
                        req = URLDecoder.decode(req, "UTF-8");
                        if (req.endsWith("/")) {
                            req = req.substring(0, req.length() - 1);
                        }
                        LogUtil.write("GET request: " + req);
                        requestInformation = new RequestInformation(fullRequest, socket, "GET", req, null, null, null);
                    } else {
                        // GET from GETClient
                        LogUtil.write("GET request: GET / HTTP/1.1");
                        requestInformation = new RequestInformation(fullRequest, socket, "GET", "GET / HTTP/1.1", getStationID, getLamportTimestamp, getProcessID);
                    }
                }
                // Call Listener and Handler
                if (fullRequest != null && !fullRequest.equals("")) {
                    // Tick aggregation server lamport clock when request received and save to file.
                    lamportClock.tick();
                    lamportClock.updateLamportFile(lamportClock.getLamportTimestamp());

                    // Add request to queue
                    Listener.getListener().addRequestToQueue(requestInformation);
                    // Handle request
                    RequestInformation req = Listener.getListener().handleRequest();
                    // System.out.println("Request Information: " + req.getRequestStartLine());
                    Handler handler = new Handler(req, semaphore);
                    handler.run();
                }
                

                in.close();
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        AggregationServer aggregationServer = new AggregationServer();
        aggregationServer.startServer(args);
        // Automatically shutdown in 2 minutes
        try {
            Thread.sleep(120000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        aggregationServer.stopServer();
    }

    /*
     * Parses PUT request into three parts: start line, header, and body.
     */
    public static String[] parsePutRequest(String fullRequest) {
        String[] lines = fullRequest.split("\n");
        String startLine = lines[0];
        String header = "";
        String body = "";
        boolean isBody = false;

        
        for (int i=1; i<lines.length; i++) {
            // System.out.println(lines[i]);
            if (lines[i].contains("Content-Length")) {
                header += lines[i];
                isBody = true;
                continue;
            }
            
            if (isBody) {
                body += lines[i];
            } else {
                header += lines[i];
                header += "\r\n";
            }
        }

        String[] result = new String[3];
        result[0] = startLine;
        result[1] = header;
        result[2] = body;
        return result;
    }

    /*
     * Saves JSON data to a unique file.
     */
    public static void saveJsonToIntermediateFile(JSONObject json) {
        String filename = "";
        String contentServerId = "";
        String processId = "";
        try {
            contentServerId = json.get("station_id").toString();
            processId = json.get("process_id").toString();
            filename = "cs" + contentServerId + "_p" + processId + ".json";
            FileWriter file = new FileWriter(filename);
            file.write(json.toString());
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } 
        // return filename;
    } 
}
