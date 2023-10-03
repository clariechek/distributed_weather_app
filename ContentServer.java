
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONObject;

public class ContentServer extends Thread {
    URI uri;
    String serverName = "localhost", stationID = "";
    int port = 4567;
    WeatherEntry[] content = null;
    LamportClock lamportClock = null;
    int process_id = -1;

    public void startServer(String[] args, ContentServer contentServer) throws URISyntaxException {
        // Assign process id and initialise lamport clock
        process_id = LamportClock.getNewPid();
        lamportClock = new LamportClock(process_id, 0);
        
        uri = new URI(args[0]);
        serverName = uri.getHost();
        if (uri.getPort() != -1) {
            port = 4567;
        } else {
            port = uri.getPort();
        }

        // Get station ID
        stationID = args[1];

        // Parse file and get content
        content = contentServer.loadContent(args[2], contentServer);
        if (content == null) {
            System.out.println("Error: Invalid content");
            System.exit(1);
        }

        this.start();
    }

    public void stopServer() {
        this.interrupt();
    }
    
    @Override
    public void run() {
        try (
            Socket socket = new Socket(serverName, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {

            System.out.println("Connected to " + serverName + " on port " + port);

            // Increment lamport clock
            lamportClock.tick();

            // Convert content to JSON format
            JSONObject json = this.convertToJSON(content, stationID, Integer.toString(lamportClock.getLamportTimestamp()), Integer.toString(lamportClock.getProcessId()));


            // Send PUT Request
            String requestHeader = this.buildRequestHeader("PUT", "/weather.json", serverName, "application/json", Integer.toString((json.toString()).length()));
            String requestBody = json.toString();
            out.write(requestHeader);
            out.write(requestBody);
            out.flush();

            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                System.out.println(serverResponse);
            }

            // Close connection
            out.close();
            in.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        ContentServer contentServer = new ContentServer();
        try {
            contentServer.startServer(args, contentServer);
        } catch (URISyntaxException e) {
            System.out.println("Invalid URI");
            System.exit(1);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        contentServer.stopServer();
    }

    // public static void main(String[] args) {
    //     ContentServer contentServer = new ContentServer();
    //     URI uri;
    //     String serverName = "localhost";
    //     int port = 4567;
    //     // Initialise lamport clock
    //     int process_id = LamportClock.getNewPid();
    //     LamportClock lamportClock = new LamportClock(process_id, 0);
    //     // Get server information
    //     try {
    //         uri = new URI(args[0]);
    //         serverName = uri.getHost();
    //         if (uri.getPort() != -1) {
    //             port = 4567;
    //         } else {
    //             port = uri.getPort();
    //         }
    //     } catch (URISyntaxException e) {
    //         System.out.println("Invalid URI");
    //         System.exit(1);
    //     } catch (Exception e) {
    //         System.out.println("Exception: " + e.getMessage());
    //     }
    //     // Get station ID
    //     String stationID = args[1];
    //     // Parse file and get content
    //     WeatherEntry[] content = contentServer.loadContent(args[2], contentServer);
    //     if (content == null) {
    //         System.out.println("Error: No content found");
    //         System.exit(1);
    //     } 
    //     // Create a new server socket
    //     try (
    //         Socket socket = new Socket(serverName, port);
    //         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
    //         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    //     ) {
    //         System.out.println("Connected to " + serverName + " on port " + port);
    //         // Increment lamport clock
    //         lamportClock.tick();
    //         // Convert content to JSON format
    //         JSONObject json = contentServer.convertToJSON(content, stationID, Integer.toString(lamportClock.getLamportTimestamp()), Integer.toString(lamportClock.getProcessId()));
    //         // Send PUT Request
    //         String requestHeader = contentServer.buildRequestHeader("PUT", "/weather.json", serverName, "application/json", Integer.toString((json.toString()).length()));
    //         String requestBody = json.toString();
    //         out.write(requestHeader);
    //         out.write(requestBody);
    //         out.flush();
    //         String serverResponse;
    //         while ((serverResponse = in.readLine()) != null) {
    //             System.out.println(serverResponse);
    //         }
    //         // Close connection
    //         out.close();
    //         in.close();
    //         socket.close();
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    /*
     * Return String representation of HTTP PUT request header
     */
    public String buildRequestHeader(String method, String path, String host, String contentType, String contentLength) {
        StringBuilder header = new StringBuilder();
        header.append(method + " " + path + " HTTP/1.1\r\n");
        header.append("Host: " + host + "\r\n");
        header.append("User-Agent: ATOMClient/1/0" + "\r\n");
        header.append("Content-Type: " + contentType + "\r\n");
        header.append("Content-Length: " + contentLength + "\r\n");
        header.append("\r\n");
        return header.toString();
    }

    /*
     * Load contents from weather station file and store in WeatherEntry objects.
     * Returns array of WeatherEntry objects.
     */
    public WeatherEntry[] loadContent(String filename, ContentServer contentServer) {
        // Parse file
        File file = new File(filename);
        FileInputStream fis;
        InputStreamReader isr;
        BufferedReader br;
        WeatherEntry[] data = null;

        try {
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis, "UTF-8");
            br = new BufferedReader(isr);

            // Get number of data in file
            int content_length = contentServer.getContentLength(filename);

            // Create array of WeatherEntry objects to store data
            data = new WeatherEntry[content_length];
            int index = 0;

            // Read file line by line and store data in WeatherEntry objects
            boolean isStartOfNewEntry = true;
            String line = br.readLine();
            String[] splitLine = null;
            if (line == null) {
                // File is empty
                System.out.println("Error: File is empty.");
                br.close();
                return null;
            } else {
                while (line != null) {
                    if (isStartOfNewEntry) {
                        // Parse id field. If no id, then return invalid file format.
                        if (!line.contains("id:")) {
                            // Invalid file format because does not contain id
                            System.out.println("Error: Invalid file format. Missing id field.");
                            br.close();
                            return null;
                        } else {
                            // Get id
                            data[index] = new WeatherEntry();
                            splitLine = line.split(":");
                            data[index].setId(splitLine[1]);
                        }
                        // Go to next line
                        isStartOfNewEntry = false;
                        line = br.readLine();
                        continue;
                    } else {
                        // Parse remaining fields
                        if (line.contains("name:")) {
                            splitLine = line.split(":");
                            String fieldData = splitLine[1];
                            data[index].setName(fieldData);
                        } else if (line.contains("state:")) {
                            splitLine = line.split(":");
                            String fieldData = splitLine[1];
                            data[index].setState(fieldData);
                        } else if (line.contains("time_zone:")) {
                            splitLine = line.split(":");
                            String fieldData = splitLine[1];
                            data[index].setTime_zone(fieldData);
                        } else if (line.contains("lat:")) {
                            splitLine = line.split(":");
                            Double fieldData = Double.parseDouble(splitLine[1]);
                            data[index].setLat(fieldData);
                        } else if (line.contains("lon:")) {
                            splitLine = line.split(":");
                            Double fieldData = Double.parseDouble(splitLine[1]);
                            data[index].setLon(fieldData);
                        } else if (line.contains("local_date_time_full:")) {
                            splitLine = line.split(":");
                            String fieldData = splitLine[1];
                            data[index].setLocal_date_time_full(fieldData);
                        } else if (line.contains("local_date_time:")) {
                            splitLine = line.split(":");
                            String fieldData = splitLine[1] + ":" + splitLine[2];
                            data[index].setLocal_date_time(fieldData);
                        } else if (line.contains("air_temp:")) {
                            splitLine = line.split(":");
                            Double fieldData = Double.parseDouble(splitLine[1]);
                            data[index].setAir_temp(fieldData);
                        } else if (line.contains("apparent_t:")) {
                            splitLine = line.split(":");
                            Double fieldData = Double.parseDouble(splitLine[1]);
                            data[index].setApparent_t(fieldData);
                        } else if (line.contains("cloud:")) {
                            splitLine = line.split(":");
                            String fieldData = splitLine[1];
                            data[index].setCloud(fieldData);
                        } else if (line.contains("dewpt:")) {
                            splitLine = line.split(":");
                            Double fieldData = Double.parseDouble(splitLine[1]);
                            data[index].setDewpt(fieldData);
                        } else if (line.contains("press:")) {
                            splitLine = line.split(":");
                            Double fieldData = Double.parseDouble(splitLine[1]);
                            data[index].setPress(fieldData);
                        } else if (line.contains("rel_hum:")) {
                            splitLine = line.split(":");
                            Long fieldData = Long.parseLong(splitLine[1]);
                            data[index].setRel_hum(fieldData);
                        } else if (line.contains("wind_dir:")) {
                            splitLine = line.split(":");
                            String fieldData = splitLine[1];
                            data[index].setWind_dir(fieldData);
                        } else if (line.contains("wind_spd_kmh:")) {
                            splitLine = line.split(":");
                            Long fieldData = Long.parseLong(splitLine[1]);
                            data[index].setWind_spd_kmh(fieldData);
                        } else if (line.contains("wind_spd_kt:")) {
                            splitLine = line.split(":");
                            Long fieldData = Long.parseLong(splitLine[1]);
                            data[index].setWind_spd_kt(fieldData);

                            // Increment counter and update isFirstLine for next entry
                            index++;
                            isStartOfNewEntry = true;
                        } else {
                            // Invalid field
                            System.out.println("Error: Invalid file format. Invalid field name.");
                            br.close();
                            return null;
                        }
                        line = br.readLine();
                    }
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error: File " + filename + " not found.");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    /*
     * Converts array of WeatherEntry objects into JSON objects.
     * Returns a JSON object.
     */
    public JSONObject convertToJSON(WeatherEntry[] data, String stationID, String lamport_timestamp, String processID) {
        JSONObject json = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            for (WeatherEntry w : data) {
                if (w == null) {
                    break;
                }
                
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", w.getId());
                jsonObject.put("name", w.getName());
                jsonObject.put("state", w.getState());
                jsonObject.put("time_zone", w.getTime_zone());
                jsonObject.put("lat", w.getLat());
                jsonObject.put("lon", w.getLon());
                jsonObject.put("local_date_time", w.getLocal_date_time());
                jsonObject.put("local_date_time_full", w.getLocal_date_time_full());
                jsonObject.put("air_temp", w.getAir_temp());
                jsonObject.put("apparent_t", w.getApparent_t());
                jsonObject.put("cloud", w.getCloud());
                jsonObject.put("dewpt", w.getDewpt());
                jsonObject.put("press", w.getPress());
                jsonObject.put("rel_hum", w.getRel_hum());
                jsonObject.put("wind_dir", w.getWind_dir());
                jsonObject.put("wind_spd_kmh", w.getWind_spd_kmh());
                jsonObject.put("wind_spd_kt", w.getWind_spd_kt());
                jsonArray.put(jsonObject);
            }

            json.put("data", jsonArray);
            json.put("station_id", stationID);
            json.put("lamport_timestamp", lamport_timestamp);
            json.put("process_id", processID);
            json.put("number_of_entries", jsonArray.length());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }
    
    /*
     * Returns the number of entries in the file.
     */
    public int getContentLength(String filename) throws IOException {
        int count = 0;
        
        try {
            FileInputStream fis = new FileInputStream(filename);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("id:")) {
                    count++;
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            System.out.println("File " + filename + " not found.");
            return -1;
        }
        catch (Exception e) {
            e.printStackTrace();
            return -2;
        }
        return count;
    }

    
}