
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

public class GETClient {
    public static void main(String[] args) {
        GETClient getClient = new GETClient();
        URI uri;
        String serverName = "localhost";
        int port = 4567;

        // Initialise lamport clock
        int process_id = LamportClock.getNewPid();
        LamportClock lamportClock = new LamportClock(process_id, 0);

        // Get server information
        try {
            uri = new URI(args[0]);
            serverName = uri.getHost();
            if (uri.getPort() != -1) {
                port = 4567;
            } else {
                port = uri.getPort();
            }
        } catch (URISyntaxException e) {
            System.out.println("Invalid URI");
            System.exit(1);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }

        // Get station ID. If not provided, then return error.
        if (args.length != 2) {
            System.out.println("Usage: java GETClient <server URI> <station ID>");
            System.exit(1);
        }
        String stationID = args[1];
        
        // Create a new server socket
        try (
            Socket socket = new Socket(serverName, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {

            System.out.println("Connected to " + serverName + " on port " + port);

            // Increment lamport clock
            lamportClock.tick();

            // Send GET Request
            String requestHeader = getClient.buildRequestHeader("GET", "/", serverName, "application/json", stationID, Integer.toString(lamportClock.getLamportTimestamp()), Integer.toString(lamportClock.getProcessId()));
            out.write(requestHeader);
            out.flush();

            // Read response
            boolean isOk = false, hasBody = false, isNotFound = false, isBadRequest = false;
            String serverResponse, data = null;
            while ((serverResponse = in.readLine()) != null) {
                if (serverResponse.contains("200")) {
                    isOk = true;
                } else if (serverResponse.contains("404")) {
                    isNotFound = true;
                } else if (serverResponse.contains("Content-Length")) {
                    String length = serverResponse.split(":")[1].trim();
                    System.out.println("Is content length 0: " + length.equals("0"));
                    if (!length.equals("0")) {
                        hasBody = true;
                    }
                } else if (hasBody) {
                    // Finish reading header of response
                    System.out.println(serverResponse);
                    serverResponse = in.readLine();
                    System.out.println(serverResponse);
                    serverResponse = in.readLine();
                    System.out.println(serverResponse);
                    serverResponse = in.readLine();
                    System.out.println(serverResponse);

                    // Read data from server response
                    serverResponse = in.readLine();
                    System.out.println(serverResponse);
                    data = serverResponse;
                    break;
                } else {
                    // Do nothing.
                }
                System.out.println(serverResponse);
            }

            // If server response contains weather data, parse it into text.
            if (isOk && hasBody) {
                JSONObject json = new JSONObject(data);
                String weatherData = "Station ID: " + json.getString("station_id") + "\n\n";
                
                JSONArray array = json.getJSONArray("data");
                for (int i=0; i<array.length(); i++) {
                    try {
                        JSONObject o = array.getJSONObject(i);
                        weatherData += "Id: " + o.getString("id") + "\n";
                        weatherData += "Name: " + o.getString("name") + "\n";
                        weatherData += "State: " + o.getString("state") + "\n";
                        weatherData += "Timezone: " + o.getString("time_zone") + "\n";
                        weatherData += "Latitude: " + o.getDouble("lat") + "\n";
                        weatherData += "Longitude: " + o.getDouble("lon") + "\n";
                        weatherData += "Local Date Time: " + o.getString("local_date_time") + "\n";
                        weatherData += "Local Date Time Full: " + o.getString("local_date_time_full") + "\n";
                        weatherData += "Air Temperature: " + o.getDouble("air_temp") + "\n";
                        weatherData += "Apparent Temperature: " + o.getDouble("apparent_t") + "\n";
                        weatherData += "Cloud: " + o.getString("cloud") + "\n";
                        weatherData += "Dew Point: " + o.getDouble("dewpt") + "\n";
                        weatherData += "Pressure: " + o.getDouble("press") + "\n";
                        weatherData += "Relative Humidity: " + o.getLong("rel_hum") + "\n";
                        weatherData += "Wind Direction: " + o.getString("wind_dir") + "\n";
                        weatherData += "Wind Speed Kmh: " + o.getLong("wind_spd_kmh") + "\n";
                        weatherData += "Wind Speed Kt: " + o.getLong("wind_spd_kt") + "\n";
                        weatherData += "\n";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                System.out.println(weatherData);
            } else if (isNotFound) {
                System.out.println("Data for station ID " + stationID + " is not found in database.");
            } else if (isBadRequest) {
                System.out.println("Error with request.");
            }

            // Close connection
            out.close();
            in.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Return String representation of HTTP GET request header
     */
    public String buildRequestHeader(String method, String path, String host, String contentType, String stationID, String lamportClockTimestamp, String processId) {
        StringBuilder header = new StringBuilder();
        header.append(method + " " + path + " HTTP/1.1\r\n");
        header.append("Host: " + host + "\r\n");
        header.append("Accept: " + contentType + "\r\n");
        header.append("StationID: " + stationID + "\r\n");
        header.append("LamportTimestamp: " + lamportClockTimestamp + "\r\n");
        header.append("ProcessID: " + processId + "\r\n");
        header.append("\r\n");
        return header.toString();
    }
}
