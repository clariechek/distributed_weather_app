
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataUtil {
    private static final String FILE_DATA = "weather.json";
    private static final DataUtil dataUtilInstance = new DataUtil();
    private static LinkedList<Integer> putHistory = new LinkedList<Integer>();
    // private static HashMap<Integer, Timer> connectedContentServers = new HashMap<Integer, Timer>();
    Timer timer = new Timer();
    long time_out_in_milliseconds = 30000;
    // private static LinkedList<Integer> connectedContentServers = new LinkedList<Integer>();
    private static HashMap<Integer, TimerTask> connectedContentServers = new HashMap<Integer, TimerTask>();
    private static HashMap<Integer, JSONObject> database = new HashMap<Integer, JSONObject>();
    
    /*
     * Return instance of DataUtil
     */
    public static DataUtil getDataUtil() {
        return dataUtilInstance;
    }

    /*
     * Returns the put history list.
     */
    public LinkedList<Integer> getPutHistory() {
        return putHistory;
    }

    /*
     * Returns a list of connected content server ids.
     */
    public List<Integer> getConnectedServerIds() {
        List<Integer> connectedServerIds = new ArrayList<Integer>();
        for (int key : connectedContentServers.keySet()) {
            connectedServerIds.add(key);
        }
        return connectedServerIds;
    }

    /*
     * Reads the weather.json file and stores the data in the database.
     */
    public void restartDatabaseAfterCrash() {
        File file = new File(FILE_DATA);
        if (file.exists()) {
            try {
                JSONArray fileData = new JSONArray(new String(Files.readAllBytes(Paths.get(FILE_DATA))));
                for (int i = 0; i < fileData.length(); i++) {
                    JSONObject fileObject = fileData.getJSONObject(i);
                    database.put(fileObject.getInt("station_id"), fileObject);
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * Returns true if this is the first time this content server id has 
     * connected to the aggregation server.
     */
    public boolean isFirstConnection(String id) {
        if (connectedContentServers.containsKey(Integer.parseInt(id))) {
            // If weather station id is in the connected content servers list, then this is not its first connection.
            return false;
        }
        // Otherwise, this is the first connection from this weather station.
        return true;
    }

    /*
     * Adds a new content server to the connected content servers list with
     * a 30 second timer task. 
     * 
     * At the end of 30 seconds, the content server will be removed from the 
     * connected content servers list and its data will be removed from the 
     * database.
     * 
     * Also checks if there are more than 20 connected content servers and
     * removes the oldest one if so.
     */
    public void addContentServer(int id) {
        // Check if there more than 20 connected content servers. If so, delete the oldest one before adding new one.
        int oldestContentServer = -1;
        while (putHistory.size() >= 20) {
            oldestContentServer = putHistory.removeFirst();
            deleteContentServer(oldestContentServer);
        }
        putHistory.add(id);
        LogUtil.write("Added content server " + id + " to connected content servers list.");
        TimerTask scheduleDelete = new TimerTask() {
            @Override
            public void run() {
                deleteContentServer(id);
            }
        };
        // Create a timer that will remove the content server from the connected content servers list after 30 seconds and its data from the database.
        if (scheduleDelete != null) {
            timer.schedule(scheduleDelete , time_out_in_milliseconds);
            connectedContentServers.put(id,scheduleDelete);
        }
    }

    /*
     * Resets the timer associated to the content server.
     */
    public void restartContentServerTimer(int id) {
        TimerTask oldScheduleDelete = connectedContentServers.get(id);
        if (oldScheduleDelete != null) {
            oldScheduleDelete.cancel();
        }
        TimerTask newScheduleDelete = new TimerTask() {
            @Override
            public void run() {
                deleteContentServer(id);
            }
        };
        if (newScheduleDelete != null) {
            timer.schedule(newScheduleDelete , time_out_in_milliseconds);
            connectedContentServers.put(id,newScheduleDelete);
        }
    }

    /*
     * Remove the content server with id from the connected content servers 
     * list and deletes its data from the database and weather.json file.
     */
    public void deleteContentServer(int id) {
        // Remove the content server with id from the connected content servers
        TimerTask scheduledDelete = connectedContentServers.get(id);
        if (scheduledDelete != null) {
            scheduledDelete.cancel();
        }
        connectedContentServers.remove(id);
        LogUtil.write("Content server " + id + " expired. Deleting content server...");

        // Remove data from database
        if (database.containsKey(id)) {
            database.remove(id);
        }
        // Update database file contents
        FileWriter fileWriter = null;
        try {
            File file = new File(FILE_DATA);
            if (file.exists()) {
                fileWriter = new FileWriter(FILE_DATA);
                String newData = getAllDataFromDatabase();
                fileWriter.write(newData);
                fileWriter.flush();
                fileWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileWriter != null) {
                try { 
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * Returns the data for content server with id from the database.
     * If no data is available, returns null.
     */
    public String getDataByIdFromDatabase(String id) {
        return database.get(Integer.parseInt(id)).toString();
    }

    /*
     * Returns all data from the database.
     */
    public String getAllDataFromDatabase() {
        JSONArray dataArray = new JSONArray();
        LogUtil.write("Getting data from database variable...");
        LogUtil.write("Database size: " + database.size());
        printDatabaseContent();
        for (int key : database.keySet()) {
            dataArray.put(database.get(key));
        }
        // for (int i = 0; i < database.size(); i++) {
        //     LogUtil.write(database.get(i).toString());
        //     dataArray.put(database.get(i));
        // }
        return dataArray.toString();
    }

    /*
     * Adds new JSON data to the database and weather.json file.
     */
    public void addNewDataToDatabase(int id, JSONObject data) {
        // Add data to database
        if (database.containsKey(id)) {
            database.remove(id);
        }
        database.put(id, data);
        LogUtil.write("Added new JSON data to database...");
        LogUtil.write("Database size: " + database.size());
        LogUtil.write("Database contents for id " + id + ": " + database.get(id).toString());

        // Add data to file
        try {
            File file = new File(FILE_DATA);
            if (!file.exists()) {
                file.createNewFile();
                LogUtil.write("Created new weather.json file");
            }
            
            if (file.length() == 0) {
                LogUtil.write("weather.json is EMPTY. Adding new data...");
            // If file is empty, write new data to file.
                try {
                    JSONArray fileData = new JSONArray();
                    fileData.put(data);
                    Files.write(Paths.get(FILE_DATA), fileData.toString().getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                LogUtil.write("Weather.json contains DATA. Appending data...");
            // If file is not empty, read data from file, and update it with new data.
                try {
                    JSONArray fileData = new JSONArray(new String(Files.readAllBytes(Paths.get(FILE_DATA))));

                    boolean foundID = false;
                    for (int i = 0; i < fileData.length(); i++) {
                        JSONObject fileObject = fileData.getJSONObject(i);
                        if (fileObject.getInt("station_id") == id) {
                            // If id is found, replace old data with new data.
                            foundID = true;
                            fileData.remove(i);
                            fileData.put(data);
                            break;
                        }
                    }

                    if (!foundID) {
                        // If id not found, add new data to file.
                        fileData.put(data);
                    }

                    Files.write(Paths.get(FILE_DATA), fileData.toString().getBytes());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Prints the contents of the database to the the log file.
     */
    public void printDatabaseContent() {
        for (int key : database.keySet()) {
            LogUtil.write("Content Server ID " + key + ": " + database.get(key).toString());
        }
    }

    /*
     * Returns true if there is JSON data for the station with id in 
     * the database, otherwise false.
     */
    public boolean isIdPresentInDatabase(String id) {
        if (database.get(Integer.parseInt(id)) == null) {
            return false;
        }
        return true;
    }

    /*
     * Clears all data from the database and weather.json file, as well
     * as put history and connected content server lists.
     */
    public void clearDatabase() {
        if (!database.isEmpty()) {
            database.clear();
        }

        if (!putHistory.isEmpty()) {
            putHistory.clear();
        }

        if (!connectedContentServers.isEmpty()) {
            for (TimerTask timer : connectedContentServers.values()) {
                timer.cancel();
            }
            this.timer.cancel();
            connectedContentServers.clear();
        }

        File file = new File(FILE_DATA);
        if (file.exists()) {
            try {
                LogUtil.write("Deleting " + FILE_DATA + "...");
            } finally {
                file.delete();
                LogUtil.write(FILE_DATA + " deleted.");
            }
        } else {
            LogUtil.write("No " + FILE_DATA + " to delete.");
        }
    }
}
