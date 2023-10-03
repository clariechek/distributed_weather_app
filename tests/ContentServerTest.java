import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ContentServerTest {
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
    @DisplayName("Build PUT Request Header Test")
    void testBuildRequestHeader() {
        ContentServer contentServer = new ContentServer();
        String output = contentServer.buildRequestHeader("PUT", "/weather.json", "localhost", "application/json", "726");
        String expectedOutput = "PUT /weather.json HTTP/1.1\r\nHost: localhost\r\nUser-Agent: ATOMClient/1/0\r\nContent-Type: application/json\r\nContent-Length: 726\r\n\r\n";
        assertEquals(expectedOutput, output);
    }

    @Test
    @DisplayName("Load Content from .txt File Test")
    void testLoadContent() {
        ContentServer contentServer = new ContentServer();
        WeatherEntry[] content = contentServer.loadContent("/Users/clariechek/Documents/Uni/1 Distributed Systems/distributed_weather_app/cs1_1.txt", contentServer);
        String output = "";
        for (WeatherEntry weatherEntry : content) {
            output += "ID: " + weatherEntry.getId() + "\n";
            output += "Name: " + weatherEntry.getName() + "\n";
            output += "State: " + weatherEntry.getState() + "\n";
            output += "Time Zone: " + weatherEntry.getTime_zone() + "\n";
            output += "Latitude: " + weatherEntry.getLat() + "\n";
            output += "Longitude: " + weatherEntry.getLon() + "\n";
            output += "Local Date Time: " + weatherEntry.getLocal_date_time() + "\n";
            output += "Local Date Time Full: " + weatherEntry.getLocal_date_time_full() + "\n";
            output += "Air Temperature: " + weatherEntry.getAir_temp() + "\n";
            output += "Apparent Temperature: " + weatherEntry.getApparent_t() + "\n";
            output += "Cloud: " + weatherEntry.getCloud() + "\n";
            output += "Dew Point: " + weatherEntry.getDewpt() + "\n";
            output += "Pressure: " + weatherEntry.getPress() + "\n";
            output += "Relative Humidity: " + weatherEntry.getRel_hum() + "\n";
            output += "Wind Direction: " + weatherEntry.getWind_dir() + "\n";
            output += "Wind Speed (km/h): " + weatherEntry.getWind_spd_kmh() + "\n";
            output += "Wind Speed (kt): " + weatherEntry.getWind_spd_kt() + "\n";
        }

        String expectedOutput = "";
        expectedOutput += "ID: IDS60901\n";
        expectedOutput += "Name: Adelaide (West Terrace /  ngayirdapira)\n";
        expectedOutput += "State: SA\n";
        expectedOutput += "Time Zone: CST\n";
        expectedOutput += "Latitude: -34.9\n";
        expectedOutput += "Longitude: 138.6\n";
        expectedOutput += "Local Date Time: 15/04:00pm\n";
        expectedOutput += "Local Date Time Full: 20230715160000\n";
        expectedOutput += "Air Temperature: 13.3\n";
        expectedOutput += "Apparent Temperature: 9.5\n";
        expectedOutput += "Cloud: Partly cloudy\n";
        expectedOutput += "Dew Point: 5.7\n";
        expectedOutput += "Pressure: 1023.9\n";
        expectedOutput += "Relative Humidity: 60\n";
        expectedOutput += "Wind Direction: S\n";
        expectedOutput += "Wind Speed (km/h): 15\n";
        expectedOutput += "Wind Speed (kt): 8\n";
        expectedOutput += "ID: IDS60902\n";
        expectedOutput += "Name: Mount Gambier\n";
        expectedOutput += "State: SA\n";
        expectedOutput += "Time Zone: CST\n";
        expectedOutput += "Latitude: -23.1\n";
        expectedOutput += "Longitude: 249.3\n";
        expectedOutput += "Local Date Time: 15/04:00pm\n";
        expectedOutput += "Local Date Time Full: 20230715160000\n";
        expectedOutput += "Air Temperature: 10.2\n";
        expectedOutput += "Apparent Temperature: 8.5\n";
        expectedOutput += "Cloud: Sunny\n";
        expectedOutput += "Dew Point: 2.1\n";
        expectedOutput += "Pressure: 1693.5\n";
        expectedOutput += "Relative Humidity: 53\n";
        expectedOutput += "Wind Direction: S\n";
        expectedOutput += "Wind Speed (km/h): 12\n";
        expectedOutput += "Wind Speed (kt): 5\n";

        assertEquals(expectedOutput, output);
    }


    @Test
    @DisplayName("Convert to JSON Test")
    void testConvertToJSON() {
        ContentServer contentServer = new ContentServer();
        WeatherEntry[] data = new WeatherEntry[2];
        WeatherEntry weatherEntry1 = new WeatherEntry();
        weatherEntry1.setId("IDS60901");
        weatherEntry1.setName("Adelaide (West Terrace /  ngayirdapira)");
        weatherEntry1.setState("SA");
        weatherEntry1.setTime_zone("CST");
        weatherEntry1.setLat(-34.9);
        weatherEntry1.setLon(138.6);
        weatherEntry1.setLocal_date_time("15/04:00pm");
        weatherEntry1.setLocal_date_time_full("20230715160000");
        weatherEntry1.setAir_temp(13.3);
        weatherEntry1.setApparent_t(9.5);
        weatherEntry1.setCloud("Partly cloudy");
        weatherEntry1.setDewpt(5.7);
        weatherEntry1.setPress(1023.9);
        weatherEntry1.setRel_hum(Long.parseLong("60"));
        weatherEntry1.setWind_dir("S");
        weatherEntry1.setWind_spd_kmh(Long.parseLong("15"));
        weatherEntry1.setWind_spd_kt(Long.parseLong("8"));
        data[0] = weatherEntry1;
        WeatherEntry weatherEntry2 = new WeatherEntry();
        weatherEntry2.setId("IDS60902");
        weatherEntry2.setName("Mount Gambier");
        weatherEntry2.setState("SA");
        weatherEntry2.setTime_zone("CST");
        weatherEntry2.setLat(-23.1);
        weatherEntry2.setLon(249.3);
        weatherEntry2.setLocal_date_time("15/04:00pm");
        weatherEntry2.setLocal_date_time_full("20230715160000");
        weatherEntry2.setAir_temp(10.2);
        weatherEntry2.setApparent_t(8.5);
        weatherEntry2.setCloud("Sunny");
        weatherEntry2.setDewpt(2.1);
        weatherEntry2.setPress(1693.5);
        weatherEntry2.setRel_hum(Long.parseLong("53"));
        weatherEntry2.setWind_dir("S");
        weatherEntry2.setWind_spd_kmh(Long.parseLong("12"));
        weatherEntry2.setWind_spd_kt(Long.parseLong("5"));
        data[1] = weatherEntry2;

        JSONObject output = contentServer.convertToJSON(data, "1", "1", "1");
        String expectedOutput = "{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        assertEquals(expectedOutput, output.toString());
    }

    @Test
    @DisplayName("Get content length Test")
    void testGetContentLength() throws Exception {
        ContentServer contentServer = new ContentServer();
        // To ensure the path is correct, you can obtain the path by right clicking the "cs1_1.txt" file and select "Copy path". Then, paste the path below as the filename.
        int output = contentServer.getContentLength("/Users/clariechek/Documents/Uni/1 Distributed Systems/distributed_weather_app/cs1_1.txt");
        int expectedOutput = 2;
        assertEquals(expectedOutput, output);
    }
}
