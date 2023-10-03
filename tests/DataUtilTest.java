import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DataUtilTest {
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    
    // Reassign stdout stream to new PrintStream 
    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    // Restore stdout stream to original state
    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    @DisplayName("Restart Database After Crash Test")
    void testRestartDatabaseAfterCrash() throws IOException {
        DataUtil.getDataUtil().clearDatabase();
        // Create weather.json file that is in directory when aggregation server crashes
        File file = new File("weather.json");
        if (file.exists()) {
            file.delete();
            file.createNewFile();
        } else {
            file.createNewFile();
        }
        FileWriter fileWriter = new FileWriter("weather.json");
        String expectedOutput = "[{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}]";
        fileWriter.write(expectedOutput);
        fileWriter.close();
        
        // Upload weather.json file to database
        DataUtil.getDataUtil().restartDatabaseAfterCrash();
        // Check if database is updated as expected
        String output = DataUtil.getDataUtil().getAllDataFromDatabase();
        assertEquals(expectedOutput, output);
    }

    @Test
    @DisplayName("Is First Connection Test - No connected content server") 
    void testIsFirstConnection1() {
        DataUtil.getDataUtil().clearDatabase();
        boolean output = DataUtil.getDataUtil().isFirstConnection("1");
        assertEquals(true, output);
    }

    @Test
    @DisplayName("Is First Connection Test - Currently connected content server") 
    void testIsFirstConnection2() {
        DataUtil.getDataUtil().clearDatabase();
        DataUtil.getDataUtil().addContentServer(1);
        boolean output = DataUtil.getDataUtil().isFirstConnection("1");
        assertEquals(false, output);
    }

    @Test
    @DisplayName("Add Content Server Test") 
    void testAddContentServer() {
        // Clear the connected content server list and add content server with id 1.
        DataUtil.getDataUtil().clearDatabase();
        DataUtil.getDataUtil().addContentServer(1);
        // Get the id of currently connected content servers and check if id 1 is there.
        List<Integer> result = DataUtil.getDataUtil().getConnectedServerIds();
        boolean output = result.contains(1);
        boolean expectedOutput = true;
        assertEquals(expectedOutput, output);
    }

    @Test
    @DisplayName("Restart Content Server Timer Test") 
    void testRestartContentServerTimer() {
        // Clear the connected content server list and add content server with id 1.
        DataUtil.getDataUtil().clearDatabase();
        DataUtil.getDataUtil().addContentServer(1);

        // Wait 15 seconds, then restart the timer for content server with id 1.
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DataUtil.getDataUtil().restartContentServerTimer(1);
        
        // Wait 20 seconds, then check if content server with id 1 is still connected.
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<Integer> result = DataUtil.getDataUtil().getConnectedServerIds();
        
        // If it is still connected, then the timer has been restarted.
        boolean output = result.contains(1);
        boolean expectedOutput = true;
        assertEquals(expectedOutput, output);
    }

    @Test
    @DisplayName("Delete Content Server Test")
    void testDeleteContentServer() {
        // Clear the connected content server list and add content server with id 1.
        DataUtil.getDataUtil().clearDatabase();
        DataUtil.getDataUtil().addContentServer(1);

        // Wait 15 seconds, then delete content server with id 1.
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DataUtil.getDataUtil().deleteContentServer(1);
        
        // Check if content server with id 1 is still connected.
        List<Integer> result = DataUtil.getDataUtil().getConnectedServerIds();
        
        // If it is not connected, then the content server has been deleted.
        boolean output = result.contains(1);
        boolean expectedOutput = false;
        assertEquals(expectedOutput, output);
    }

    @Test
    @DisplayName("Get Data By ID From Database Test")
    void testGetDataByIdFromDatabase() throws JSONException {
        // Clear the database and add data with id 1.
        DataUtil.getDataUtil().clearDatabase();
        String expectedOutput = "{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        JSONObject json = new JSONObject(expectedOutput);
        DataUtil.getDataUtil().addNewDataToDatabase(1, json);
        
        // Get data with id 1 from database and check if matches input.
        String output = DataUtil.getDataUtil().getDataByIdFromDatabase("1");
        assertEquals(expectedOutput, output);
    }

    @Test
    @DisplayName("Get All Data From Database Test")
    void testGetAllDataFromDatabase() throws JSONException {
        // Clear the database and add data.
        DataUtil.getDataUtil().clearDatabase();
        String data1 = "{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        JSONObject json1 = new JSONObject(data1);
        DataUtil.getDataUtil().addNewDataToDatabase(1, json1);
        String data2 = "{\"process_id\":\"2\",\"data\":[{\"apparent_t\":-3.5,\"wind_spd_kmh\":8,\"rel_hum\":50,\"lon\":18.6,\"dewpt\":2.2,\"wind_spd_kt\":3,\"wind_dir\":\"N\",\"time_zone\":\"AST\",\"air_temp\":5.1,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Canberra\",\"id\":\"IDS60905\",\"state\":\"ACT\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"2\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        JSONObject json2 = new JSONObject(data2);
        DataUtil.getDataUtil().addNewDataToDatabase(2, json2);
        
        // Get all data from database and check if matches what was inputted.
        String output = DataUtil.getDataUtil().getAllDataFromDatabase();
        String expectedOutput = "[{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2},{\"process_id\":\"2\",\"data\":[{\"apparent_t\":-3.5,\"wind_spd_kmh\":8,\"rel_hum\":50,\"lon\":18.6,\"dewpt\":2.2,\"wind_spd_kt\":3,\"wind_dir\":\"N\",\"time_zone\":\"AST\",\"air_temp\":5.1,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Canberra\",\"id\":\"IDS60905\",\"state\":\"ACT\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"2\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}]";
        assertEquals(expectedOutput, output);
    }

    @Test
    @DisplayName("Add New Data To Database Test")
    void testAddNewDataToDatabase() throws JSONException, IOException {
        // Clear the database and add data.
        DataUtil.getDataUtil().clearDatabase();
        String expectedOutput = "{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        JSONObject json = new JSONObject(expectedOutput);
        DataUtil.getDataUtil().addNewDataToDatabase(1, json);

        // Check data is in database
        String output = DataUtil.getDataUtil().getDataByIdFromDatabase("1");
        assertEquals(expectedOutput, output);
        
        // Check data is in weather.json
        File file = new File("weather.json");
        if (file.exists()) {
            // Get data
            JSONArray fileData = new JSONArray(new String(Files.readAllBytes(Paths.get("weather.json"))));
            String fileOutput = fileData.toString();
            String expectedFileOutput = "[{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}]";
            assertEquals(expectedFileOutput, fileOutput);
        } else {
            // Test fail because weather.json file does not exist.
            assertEquals(true, false);
        }
    }

    @Test
    @DisplayName("Is Id Present in Database Test - ID is present in database")
    void testIsIdPresentInDatabase1() throws JSONException {
        // Clear the database and add data.
        DataUtil.getDataUtil().clearDatabase();
        String data = "{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        JSONObject json = new JSONObject(data);
        DataUtil.getDataUtil().addNewDataToDatabase(1, json);
        
        // Check if ID 1 is present in database.
        boolean output = DataUtil.getDataUtil().isIdPresentInDatabase("1");
        assertEquals(true, output);
    }

    @Test
    @DisplayName("Is Id Present in Database Test - ID is not present in database")
    void testIsIdPresentInDatabase2() throws JSONException {
        // Clear the database and add data.
        DataUtil.getDataUtil().clearDatabase();
        
        // Check if ID 1 is present in database.
        boolean output = DataUtil.getDataUtil().isIdPresentInDatabase("1");
        assertEquals(false, output);
    }

    @Test
    @DisplayName("Clear database Test")
    void testClearDatabase() throws JSONException {
        // Add data to the database
        String data = "{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        JSONObject json = new JSONObject(data);
        DataUtil.getDataUtil().addNewDataToDatabase(2, json);
        DataUtil.getDataUtil().addContentServer(2);

        // Clear the database
        DataUtil.getDataUtil().clearDatabase();
        
        // Check if database is empty
        String databaseOutput = DataUtil.getDataUtil().getAllDataFromDatabase();
        String expectedDatabaseOutput = "[]";
        assertEquals(expectedDatabaseOutput, databaseOutput);

        // Check if putHistory is empty
        boolean putHistoryOutput = DataUtil.getDataUtil().getPutHistory().isEmpty();
        boolean expectedPutHistoryOutput = true;
        assertEquals(expectedPutHistoryOutput, putHistoryOutput);
    }
}
