import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Semaphore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class HandlerTest {
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
    @DisplayName("Process Put Request Test - 200 OK")
    void testProcessPutRequest1() throws JSONException, IOException {
        // Ensure weather.json exists and is empty.
        File file = new File("weather.json");
        if (file.exists()) {
            file.delete();
            file.createNewFile();
        } else {
            file.createNewFile();
        }

        // Ensure request file exists and contains data.
        String data = "{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        JSONObject json = new JSONObject(data);
        File requestFile = new File("cs1_p1.json");
        if (requestFile.exists()) {
            requestFile.delete();
            requestFile.createNewFile();
        } else {
            requestFile.createNewFile();
        }
        FileWriter fileWriter = new FileWriter("cs1_p1.json");
        fileWriter.write(json.toString());
        fileWriter.close();

        // Ensure content server with id 1 is in connectedContentServer list.
        DataUtil.getDataUtil().clearDatabase();
        DataUtil.getDataUtil().addContentServer(1);
        
        // Initialise PUT request information
        Semaphore sem = new Semaphore(1);
        String fullRequest = "PUT /weather.json HTTP/1.1\r\nHost: localhost\r\nUser-Agent: ATOMClient/1/0\r\nContent-Type: application/json\r\nContent-Length: 734\r\n\r\n{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        Socket socket = new Socket();
        RequestInformation request = new RequestInformation(fullRequest, socket, "PUT", "PUT /weather.json HTTP/1.1", "1", "1", "1");
        
        // Process PUT request
        Handler handler = new Handler(request, sem);
        handler.processPutRequest(request, "PUT /weather.json HTTP/1.1", socket);
        String output = outputStreamCaptor.toString();
        String expectedOutput = "1970-01-01 09:30:00 weather.json deleted.\n1970-01-01 09:30:00 Added content server 1 to connected content servers list.\n1970-01-01 09:30:00 Added new JSON data to database...\n1970-01-01 09:30:00 Database size: 1\n1970-01-01 09:30:00 Database contents for id 1: {\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}\n1970-01-01 09:30:00 weather.json is EMPTY. Adding new data...\n1970-01-01 09:30:00 200(OK): PUT /weather.json HTTP/1.1\n1970-01-01 09:30:00 Error sending response: Socket is not connected\n";
        assertEquals(expectedOutput, output);
    }
    
    @Test
    @DisplayName("Process Put Request Test - 201 Created")
    void testProcessPutRequest2() throws JSONException, IOException {
        // Ensure weather.json exists and is empty.
        File file = new File("weather.json");
        if (file.exists()) {
            file.delete();
            file.createNewFile();
        } else {
            file.createNewFile();
        }

        // Ensure request file exists and contains data.
        String data = "{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        JSONObject json = new JSONObject(data);
        File requestFile = new File("cs1_p1.json");
        if (requestFile.exists()) {
            requestFile.delete();
            requestFile.createNewFile();
        } else {
            requestFile.createNewFile();
        }
        FileWriter fileWriter = new FileWriter("cs1_p1.json");
        fileWriter.write(json.toString());
        fileWriter.close();

        // Ensure content server with id 1 is not in connectedContentServer list.
        DataUtil.getDataUtil().clearDatabase();
        
        // Initialise PUT request information
        Semaphore sem = new Semaphore(1);
        String fullRequest = "PUT /weather.json HTTP/1.1\r\nHost: localhost\r\nUser-Agent: ATOMClient/1/0\r\nContent-Type: application/json\r\nContent-Length: 734\r\n\r\n{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        Socket socket = new Socket();
        RequestInformation request = new RequestInformation(fullRequest, socket, "PUT", "PUT /weather.json HTTP/1.1", "1", "1", "1");
        
        // Process PUT request
        Handler handler = new Handler(request, sem);
        handler.processPutRequest(request, "PUT /weather.json HTTP/1.1", socket);
        String output = outputStreamCaptor.toString();
        String expectedOutput = "1970-01-01 09:30:00 weather.json deleted.\n1970-01-01 09:30:00 Added new JSON data to database...\n1970-01-01 09:30:00 Database size: 1\n1970-01-01 09:30:00 Database contents for id 1: {\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}\n1970-01-01 09:30:00 weather.json is EMPTY. Adding new data...\n1970-01-01 09:30:00 Added content server 1 to connected content servers list.\n1970-01-01 09:30:00 201(Created): PUT /weather.json HTTP/1.1\n1970-01-01 09:30:00 Error sending response: Socket is not connected\n";
        assertEquals(expectedOutput, output);
    }

    @Test
    @DisplayName("Process Put Request Test - 204 No content")
    void testProcessPutRequest3() throws JSONException, IOException {
       // Ensure weather.json exists and is empty.
        File file = new File("weather.json");
        if (file.exists()) {
            file.delete();
            file.createNewFile();
        } else {
            file.createNewFile();
        }

        // Ensure request file exists and is empty.
        File requestFile = new File("cs1_p1.json");
        if (requestFile.exists()) {
            requestFile.delete();
            requestFile.createNewFile();
        } else {
            requestFile.createNewFile();
        }

        // Ensure data for content server with id 1 is not in database.
        DataUtil.getDataUtil().clearDatabase();
        
        // Initialise PUT request information
        Semaphore sem = new Semaphore(1);
        String fullRequest = "PUT /weather.json HTTP/1.1\r\nHost: localhost\r\nUser-Agent: ATOMClient/1/0\r\nContent-Type: application/json\r\nContent-Length: 734\r\n\r\n{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        Socket socket = new Socket();
        RequestInformation request = new RequestInformation(fullRequest, socket, "PUT", "PUT /weather.json HTTP/1.1", "1", "1", "1");
        
        // Process PUT request
        Handler handler = new Handler(request, sem);
        handler.processPutRequest(request, "PUT /weather.json HTTP/1.1", socket);
        String output = outputStreamCaptor.toString();
        String expectedOutput = "1970-01-01 09:30:00 weather.json deleted.\n1970-01-01 09:30:00 204(No Content): PUT /weather.json HTTP/1.1\n1970-01-01 09:30:00 Error sending response: Socket is not connected\n";
        assertEquals(expectedOutput, output);
    }

    @Test
    @DisplayName("Process Put Request Test - 500 Invalid JSON")
    void testProcessPutRequest4() throws JSONException, IOException {
       // Ensure weather.json exists and is empty.
        File file = new File("weather.json");
        if (file.exists()) {
            file.delete();
            file.createNewFile();
        } else {
            file.createNewFile();
        }

        // Ensure request file exists and contains data that is missing id.
        String data = "{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        JSONObject json = new JSONObject(data);
        File requestFile = new File("cs1_p1.json");
        if (requestFile.exists()) {
            requestFile.delete();
            requestFile.createNewFile();
        } else {
            requestFile.createNewFile();
        }
        FileWriter fileWriter = new FileWriter("cs1_p1.json");
        fileWriter.write(json.toString());
        fileWriter.close();

        // Ensure data for content server with id 1 is not in database.
        DataUtil.getDataUtil().clearDatabase();
        
        // Initialise PUT request information
        Semaphore sem = new Semaphore(1);
        String fullRequest = "PUT /weather.json HTTP/1.1\r\nHost: localhost\r\nUser-Agent: ATOMClient/1/0\r\nContent-Type: application/json\r\nContent-Length: 734\r\n\r\n{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        Socket socket = new Socket();
        RequestInformation request = new RequestInformation(fullRequest, socket, "PUT", "PUT /weather.json HTTP/1.1", "1", "1", "1");
        
        // Process PUT request
        Handler handler = new Handler(request, sem);
        handler.processPutRequest(request, "PUT /weather.json HTTP/1.1", socket);
        String output = outputStreamCaptor.toString();
        String expectedOutput = "1970-01-01 09:30:00 weather.json deleted.\n1970-01-01 09:30:00 500(Internal Server Error): PUT /weather.json HTTP/1.1\n1970-01-01 09:30:00 Error sending response: Socket is not connected\n";
        assertEquals(expectedOutput, output);
    }

    @Test
    @DisplayName("Process Put Request Test - JSON File does not exist")
    void testProcessPutRequest5() throws JSONException, IOException {
       // Ensure weather.json exists and is empty.
        File file = new File("weather.json");
        if (file.exists()) {
            file.delete();
            file.createNewFile();
        } else {
            file.createNewFile();
        }

        // Ensure request file does not exist.
        File requestFile = new File("cs1_p1.json");
        if (requestFile.exists()) {
            requestFile.delete();
        }

        // Ensure data for content server with id 1 is not in database.
        DataUtil.getDataUtil().clearDatabase();
        
        // Initialise PUT request information
        Semaphore sem = new Semaphore(1);
        String fullRequest = "PUT /weather.json HTTP/1.1\r\nHost: localhost\r\nUser-Agent: ATOMClient/1/0\r\nContent-Type: application/json\r\nContent-Length: 734\r\n\r\n{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        Socket socket = new Socket();
        RequestInformation request = new RequestInformation(fullRequest, socket, "PUT", "PUT /weather.json HTTP/1.1", "1", "1", "1");
        
        // Process PUT request
        Handler handler = new Handler(request, sem);
        handler.processPutRequest(request, "PUT /weather.json HTTP/1.1", socket);
        String output = outputStreamCaptor.toString();
        String expectedOutput = "1970-01-01 09:30:00 weather.json deleted.\n";
        assertEquals(expectedOutput, output);
    }

    @Test
    @DisplayName("Process Get Request Test - 200 OK")
    void testprocessGetRequest1() throws JSONException, IOException {
         // Ensure weather.json exists and is empty.
        File file = new File("weather.json");
        if (file.exists()) {
            file.delete();
            file.createNewFile();
        } else {
            file.createNewFile();
        }
        
        // Ensure data for content server id 1 is in database.
        DataUtil.getDataUtil().clearDatabase();
        String data = "{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        JSONObject json = new JSONObject(data);
        DataUtil.getDataUtil().addNewDataToDatabase(1, json);

        // Initialise GET request information
        Semaphore sem = new Semaphore(1);
        String fullRequest = "GET / HTTP/1.1\r\nHost: localhost\r\nAccept: application/json\r\nStationID: 1\r\nLamportTimestamp: 1\r\nProcessID: 1\r\n\r\n";
        Socket socket = new Socket();
        RequestInformation request = new RequestInformation(fullRequest, socket, "GET", "GET / HTTP/1.1", "1", "1", "1");
        
        // Process GET request
        Handler handler = new Handler(request, sem);
        handler.processGetRequest(request, "GET / HTTP/1.1", socket);
        String output = outputStreamCaptor.toString();
        String expectedOutput = "1970-01-01 09:30:00 weather.json deleted.\n1970-01-01 09:30:00 Added new JSON data to database...\n1970-01-01 09:30:00 Database size: 1\n1970-01-01 09:30:00 Database contents for id 1: {\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}\n1970-01-01 09:30:00 weather.json is EMPTY. Adding new data...\n1970-01-01 09:30:00 200(OK): GET / HTTP/1.1\n1970-01-01 09:30:00 Error sending response: Socket is not connected\n";
        assertEquals(expectedOutput, output);
    }

    @Test
    @DisplayName("Process Get Request Test - 404 Not Found")
    void testprocessGetRequest2() throws JSONException, IOException {
         // Ensure weather.json exists and is empty.
        File file = new File("weather.json");
        if (file.exists()) {
            file.delete();
            file.createNewFile();
        } else {
            file.createNewFile();
        }
        
        // Ensure data for content server id 1 is not in database.
        DataUtil.getDataUtil().clearDatabase();

        // Initialise GET request information
        Semaphore sem = new Semaphore(1);
        String fullRequest = "GET / HTTP/1.1\r\nHost: localhost\r\nAccept: application/json\r\nStationID: 1\r\nLamportTimestamp: 1\r\nProcessID: 1\r\n\r\n";
        Socket socket = new Socket();
        RequestInformation request = new RequestInformation(fullRequest, socket, "GET", "GET / HTTP/1.1", "1", "1", "1");
        
        // Process GET request
        Handler handler = new Handler(request, sem);
        handler.processGetRequest(request, "GET / HTTP/1.1", socket);
        String output = outputStreamCaptor.toString();
        String expectedOutput = "1970-01-01 09:30:00 weather.json deleted.\n1970-01-01 09:30:00 404(Not Found): GET / HTTP/1.1\n1970-01-01 09:30:00 Error sending response: Socket is not connected\n";
        assertEquals(expectedOutput, output);
    }
    
    @Test
    @DisplayName("Check if JSON is Valid Test - No name field")
    void testcheckIfJsonIsValid1() throws IOException, JSONException {
        // Create json data without name field.
        String data = "{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        JSONObject json = new JSONObject(data);
       
        // Add data to intermediate file.
        File file = new File("cs1_p1.json");
        if (file.exists()) {
            file.delete();
            file.createNewFile();
        } else {
            file.createNewFile();
        }
        FileWriter fileWriter = new FileWriter("cs1_p1.json");
        fileWriter.write(json.toString());
        fileWriter.close();

        // Check if JSON is valid.
        RequestInformation info = new RequestInformation(null, null, null, null, null, null, null);
        Semaphore sem = new Semaphore(1);
        Handler handler = new Handler(info, sem);
        JSONObject output = handler.checkIfJsonIsValid("cs1_p1.json");
        JSONObject expectedOutput = null;
        assertEquals(expectedOutput, output);
    }

    @Test
    @DisplayName("Check if JSON is Valid Test - No apparent temperature field")
    void testcheckIfJsonIsValid2() throws IOException, JSONException {
        // Create json data without name field.
        String data = "{\"process_id\":\"1\",\"data\":[{\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        JSONObject json = new JSONObject(data);
       
        // Add data to intermediate file.
        File file = new File("cs1_p1.json");
        if (file.exists()) {
            file.delete();
            file.createNewFile();
        } else {
            file.createNewFile();
        }
        FileWriter fileWriter = new FileWriter("cs1_p1.json");
        fileWriter.write(json.toString());
        fileWriter.close();

        // Check if JSON is valid.
        RequestInformation info = new RequestInformation(null, null, null, null, null, null, null);
        Semaphore sem = new Semaphore(1);
        Handler handler = new Handler(info, sem);
        JSONObject output = handler.checkIfJsonIsValid("cs1_p1.json");
        JSONObject expectedOutput = null;
        assertEquals(expectedOutput, output);
    }

    @Test
    @DisplayName("Check if JSON is Valid Test - all fields present")
    void testcheckIfJsonIsValid3() throws IOException, JSONException {
        // Create json data without name field.
        String data = "{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        JSONObject expectedOutput = new JSONObject(data);
       
        // Add data to intermediate file.
        File file = new File("cs1_p1.json");
        if (file.exists()) {
            file.delete();
            file.createNewFile();
        } else {
            file.createNewFile();
        }
        FileWriter fileWriter = new FileWriter("cs1_p1.json");
        fileWriter.write(expectedOutput.toString());
        fileWriter.close();

        // Check if JSON is valid.
        RequestInformation info = new RequestInformation(null, null, null, null, null, null, null);
        Semaphore sem = new Semaphore(1);
        Handler handler = new Handler(info, sem);
        JSONObject output = handler.checkIfJsonIsValid("cs1_p1.json");
        assertEquals(expectedOutput.toString(), output.toString());
    }

    @Test
    @DisplayName("Upload new data Test")
    void testUploadNewData() throws IOException, JSONException {
        // Ensure weather.json exists and is empty.
        File file = new File("weather.json");
        if (file.exists()) {
            file.delete();
            file.createNewFile();
        } else {
            file.createNewFile();
        }

        // Ensure database is empty.
        DataUtil.getDataUtil().clearDatabase();

        // Upload new data.
        String data = "{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        JSONObject json = new JSONObject(data);
        RequestInformation info = new RequestInformation(null, null, null, null, null, null, null);
        Semaphore sem = new Semaphore(1);
        Handler handler = new Handler(info, sem);
        handler.uploadNewData(json);

        // Check if new data is in weather.json
        String expectedOutput = "[{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}]";
        JSONArray fileOutput = new JSONArray(new String(Files.readAllBytes(Paths.get("weather.json"))));
        assertEquals(expectedOutput, fileOutput.toString());

        // Check if new data is in database.
        String databaseOutput = DataUtil.getDataUtil().getAllDataFromDatabase();
        assertEquals(expectedOutput, databaseOutput);
    }
}

