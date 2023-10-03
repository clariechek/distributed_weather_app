
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Semaphore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Handler implements Runnable {
    private RequestInformation requestInformation;
    private Response response;
    private Semaphore semaphore;
    
    /*
     * Constructor for Handler class.
     */
    public Handler(RequestInformation requestInformation, Semaphore semaphore) {
        this.requestInformation = requestInformation;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        try {
            semaphore.acquire();
            processRequest(this.requestInformation);
            semaphore.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
     * Process the PUT and GET requests.
     */
    public void processRequest(RequestInformation req) {
        Socket socket = req.getSocket();
        String startLine = req.getRequestStartLine();
        
        if (req.getRequestType().equals("PUT")) {
            processPutRequest(req, startLine, socket);    
        } else {
            processGetRequest(req, startLine, socket);
        }
    }

    /*
     * Process GET requests by checking if it is from the GETClient or not.
     * If it is not from the GETClient, then returns a HTTP 200 OK response.
     * 
     * Otherwise, it checks if the data for the given id is present in the
     * database. If yes, it returns a HTTP 200 OK response with the data.
     * Otherwise, it returns a HTTP 404 Not Found response.
     */
    void processGetRequest(RequestInformation req, String startLine, Socket socket) {
        // Check if it is a GET request from GETClient. If there is not content server id, then it is not from the GETClient.
        if (req.getContentServerId() == null) {
            // Normal GET request. Return 200 OK response.
            response = new Response("OK", "200", "0", "", req.getRequestStartLine(), "", socket, false);
            response.sendResponse();
        } else {
            // Get request from GETClient. Get data for given id from database.
            if (DataUtil.getDataUtil().isIdPresentInDatabase(req.getContentServerId())) {
                // Return 200 OK response
                String data = DataUtil.getDataUtil().getDataByIdFromDatabase(req.getContentServerId());
                String content_length = data.length() + "";
                response = new Response("OK", "200", String.valueOf(content_length), data, startLine, "", socket, true);
                response.sendResponse();
            } else {
                // Return 404 Not Found response.
                response = new Response("Not Found", "404", "0", "", startLine, "Data not found in database.", socket, true);
                response.sendResponse();
            }
        }
    }

    /*
     * Process PUT request by checking if the request's intermediate file exists and 
     * if it contains any data. 
     * If the file exists and contains data, then the data is uploaded to the server.
     * After the file is processed, it is deleted.
     * 
     * Replies the server with the appropriate HTTP response 204, 500, 201, or 200.
     * 
     * It also checks if this is the content server's first connection. If yes, it adds
     * its id to the list of connected content servers and starts a 30s timer. Otherwise,
     * it cancels the existing timer associated with this content server and creates a 
     * new 30s timer for it.
     */
    void processPutRequest(RequestInformation req, String startLine, Socket socket) {
        // Get current request's intermediate file
        String currentRequestFileName = "cs" + req.getContentServerId() + "_p" + req.getProcessId() + ".json";
        File currentRequestFile = new File(currentRequestFileName);

        // Check if intermediate file exists. If yes, check if it contains any data. Otherwise, do nothing.
        if (currentRequestFile.exists()) {
            String content_length = currentRequestFile.length() + "";

            if (currentRequestFile.length() == 0) {
                // File is empty, reply with HTTP response 204 no content.
                response = new Response("No Content", "204", "0", "", startLine, "", socket, true);
                response.sendResponse();
            } else {
                // Check if JSON content is valid. If yes, upload the data to the server. Otherwise, reply with HTTP response 500 internal server error.
                JSONObject json = checkIfJsonIsValid(currentRequestFileName);
                if (json == null) {
                    // Return 500 Internal Server Error
                    response = new Response("Internal Server Error", "500", "0", "", startLine, "The JSON data is invalid.", socket, true);
                    response.sendResponse();
                } else {
                    // Upload data to server
                    uploadNewData(json);
                    
                    // Check if this is the content server's first connection. If yes, add its id to list of connected content servers, and start 30s timer. Otherwise, cancel the existing timer associated with this content server and create a new 30s timer.
                    if (DataUtil.getDataUtil().isFirstConnection(req.getContentServerId())) {
                        DataUtil.getDataUtil().addContentServer(Integer.parseInt(req.getContentServerId()));

                        // Return 201 Created response
                        response = new Response("Created", "201", String.valueOf(content_length), json.toString(), startLine, "", socket, true);
                        response.sendResponse();
                    } else {
                        DataUtil.getDataUtil().restartContentServerTimer(Integer.parseInt(req.getContentServerId()));

                        // Return 200 OK response
                        response = new Response("OK", "200", String.valueOf(content_length), json.toString(), startLine, "", socket, true);
                        response.sendResponse();
                    }
                }
            }
            // Delete intermediate file after handling.
            currentRequestFile.delete();
        }
    }

    /*
     * Returns JSONObject if the JSON data is valid, otherwise null.
     * JSON is valid only if it contains all the required fields.
     */
    public JSONObject checkIfJsonIsValid(String fileName) {
        File file = new File(fileName);
        JSONObject json = null;
        try {
            // Read the file contents into a JSONArray
            String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
            json = new JSONObject(content);
            JSONArray array = json.getJSONArray("data");

            // Check if the JSON data contains all the required fields
            for (int i=0; i<array.length(); i++) {
                try {
                    JSONObject o = array.getJSONObject(i);

                    if (!o.has("id") || !o.has("name") || !o.has("state") || !o.has("time_zone") || !o.has("lat") || !o.has("lon") || !o.has("local_date_time") || !o.has("local_date_time_full") || !o.has("air_temp") || !o.has("apparent_t") || !o.has("cloud") || !o.has("dewpt") || !o.has("press") || !o.has("rel_hum") || !o.has("wind_dir") || !o.has("wind_spd_kmh") || !o.has("wind_spd_kt")) {
                        // Return false if any of the required fields are missing
                        return null;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    /*
     * Uploads new JSON data to the server database (database and weather.json file).
     */
    public void uploadNewData(JSONObject json) {
        try {
            int contentServerId = json.getInt("station_id");
            DataUtil.getDataUtil().addNewDataToDatabase(contentServerId, json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}