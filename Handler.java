
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
     * Process the PUT requests by uploading data to the server and process
     * GET requests by fetching data from the server. Sends HTTP response to
     * the content server or client that made the request.
     */
    public void processRequest(RequestInformation req) {
        Socket socket = req.getSocket();
        String startLine = req.getRequestStartLine();
        
        if (req.getRequestType().equals("PUT")) {
            // PUT request. Get current request's intermediate file
            String currentRequestFileName = "cs" + req.getContentServerId() + "_p" + req.getProcessId() + ".json";
            
            File currentRequestFile = new File(currentRequestFileName);
            if (currentRequestFile.exists()) {
                String content_length = currentRequestFile.length() + "";

                if (currentRequestFile.length() == 0) {
                    // File is empty, return no content
                    response = new Response("No Content", "204", "0", "", startLine, "", socket, true);
                    response.sendResponse();
                } else {
                    // Check if JSON content is valid.
                    JSONObject json = checkIfJsonIsValid(currentRequestFileName);
                    if (json == null) {
                        // Return 500 Internal Server Error
                        response = new Response("Internal Server Error", "500", "0", "", startLine, "The JSON data is invalid.", socket, true);
                        response.sendResponse();
                    } else {
                        // File is not empty, process PUT request
                        uploadNewData(json);
                        
                        // If first connection, add content server to list of connected content servers, and create timer.
                        if (DataUtil.getDataUtil().isFirstConnection(req.getContentServerId())) {
                            // Add to connected content server list and start timer
                            DataUtil.getDataUtil().addContentServer(Integer.parseInt(req.getContentServerId()));

                            // Return 201 Created response
                            response = new Response("Created", "201", String.valueOf(content_length), json.toString(), startLine, "", socket, true);
                            response.sendResponse();
                        } else {
                            // If subsequent connection, cancel timer and start new timer.
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
        } else {
            // GET request. Check if it is a GET request from GETClient.
            if (req.getContentServerId() == null) {
                // Normal GET request. Return 200 OK response.
                response = new Response("OK", "200", "0", "", req.getRequestStartLine(), "", socket, false);
                response.sendResponse();
            } else {
                // Get request from GETClient. Get data from database.
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
    }

    /*
     * Returns JSONObject if the JSON data is valid, otherwise null.
     */
    public JSONObject checkIfJsonIsValid(String fileName) {
        File file = new File(fileName);
        JSONObject json = null;
        try {
            String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
            json = new JSONObject(content);
            JSONArray array = json.getJSONArray("data");

            for (int i=0; i<array.length(); i++) {
                // boolean hasId = false, hasName = false, hasState = false, hasTimeZone = false, hasLat = false, hasLon = false, hasLocalDateTime = false, hasLocalDateTimeFull = false, hasAirTemp = false, hasApparentT = false, hasCloud = false, hasDewpt = false, hasPress = false, hasRelHum = false, hasWindDir = false, hasWindSpdKmh = false, hasWindSpdKt = false;
                try {
                    JSONObject o = array.getJSONObject(i);
                    
                    // System.out.println("has Id: " + o.has("id"));
                    // System.out.println("has Name: " + o.has("name"));
                    // System.out.println("has State: " + o.has("state"));
                    // System.out.println("has Time Zone: " + o.has("time_zone"));
                    // System.out.println("has Lat: " + o.has("lat"));
                    // System.out.println("has Lon: " + o.has("lon"));
                    // System.out.println("has Local Date Time: " + o.has("local_date_time"));
                    // System.out.println("has Local Date Time Full: " + o.has("local_date_time_full"));
                    // System.out.println("has Air Temp: " + o.has("air_temp"));
                    // System.out.println("has Apparent T: " + o.has("apparent_t"));
                    // System.out.println("has Cloud: " + o.has("cloud"));
                    // System.out.println("has Dewpt: " + o.has("dewpt"));
                    // System.out.println("has Press: " + o.has("press"));
                    // System.out.println("has Rel Hum: " + o.has("rel_hum"));
                    // System.out.println("has Wind Dir: " + o.has("wind_dir"));
                    // System.out.println("has Wind Spd Kmh: " + o.has("wind_spd_kmh"));
                    // System.out.println("has Wind Spd Kt: " + o.has("wind_spd_kt"));

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
     * Parses the intermediate .json file contents and uploads it to the
     * server (database and weather.json file).
     */
    private void uploadNewData(JSONObject json) {
        // Parse .json file and add to database and weather.json file
        try {
            int contentServerId = json.getInt("station_id");
            DataUtil.getDataUtil().addNewDataToDatabase(contentServerId, json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}