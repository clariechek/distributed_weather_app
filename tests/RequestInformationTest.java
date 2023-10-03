import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RequestInformationTest {
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
    @DisplayName("Get Full Request Test")
    void testGetFullRequest() {
        String fullRequest = "PUT /weather.json HTTP/1.1\r\nHost: localhost\r\nUser-Agent: ATOMClient/1/0\r\nContent-Type: application/json\r\nContent-Length: 734\r\n\r\n{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"4\",\"lamport_timestamp\":\"2\",\"number_of_entries\":2}";
        Socket socket = new Socket();
        RequestInformation request = new RequestInformation(fullRequest, socket, "PUT", "PUT /weather.json HTTP/1.1", "4", "2", "1");
        assertEquals(fullRequest, request.getFullRequest());
    }

    @Test
    @DisplayName("Get Request Type Test")
    void testRequestType () {
        String fullRequest = "PUT /weather.json HTTP/1.1\r\nHost: localhost\r\nUser-Agent: ATOMClient/1/0\r\nContent-Type: application/json\r\nContent-Length: 734\r\n\r\n{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"4\",\"lamport_timestamp\":\"2\",\"number_of_entries\":2}";
        Socket socket = new Socket();
        RequestInformation request = new RequestInformation(fullRequest, socket, "PUT", "PUT /weather.json HTTP/1.1", "4", "2", "1");
        assertEquals("PUT", request.getRequestType());
    }

    @Test
    @DisplayName("Get Request Start Line Test")
    void testRequestStartLine () {
        String fullRequest = "PUT /weather.json HTTP/1.1\r\nHost: localhost\r\nUser-Agent: ATOMClient/1/0\r\nContent-Type: application/json\r\nContent-Length: 734\r\n\r\n{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"4\",\"lamport_timestamp\":\"2\",\"number_of_entries\":2}";
        Socket socket = new Socket();
        RequestInformation request = new RequestInformation(fullRequest, socket, "PUT", "PUT /weather.json HTTP/1.1", "4", "2", "1");
        assertEquals("PUT /weather.json HTTP/1.1", request.getRequestStartLine());
    }

    @Test
    @DisplayName("Get Content server Id Test")
    void testGetContentServerId() {
        String fullRequest = "PUT /weather.json HTTP/1.1\r\nHost: localhost\r\nUser-Agent: ATOMClient/1/0\r\nContent-Type: application/json\r\nContent-Length: 734\r\n\r\n{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"4\",\"lamport_timestamp\":\"2\",\"number_of_entries\":2}";
        Socket socket = new Socket();
        RequestInformation request = new RequestInformation(fullRequest, socket, "PUT", "PUT /weather.json HTTP/1.1", "4", "2", "1");
        assertEquals("4", request.getContentServerId());
    }

    @Test
    @DisplayName("Get Lamport Timestamp Test")
    void testGetLamportTimestamp() {
        String fullRequest = "PUT /weather.json HTTP/1.1\r\nHost: localhost\r\nUser-Agent: ATOMClient/1/0\r\nContent-Type: application/json\r\nContent-Length: 734\r\n\r\n{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"4\",\"lamport_timestamp\":\"2\",\"number_of_entries\":2}";
        Socket socket = new Socket();
        RequestInformation request = new RequestInformation(fullRequest, socket, "PUT", "PUT /weather.json HTTP/1.1", "4", "2", "1");
        assertEquals("2", request.getLamportTimestamp());
    }

    @Test
    @DisplayName("Get Process Id Test")
    void testGetProcessId () {
        String fullRequest = "PUT /weather.json HTTP/1.1\r\nHost: localhost\r\nUser-Agent: ATOMClient/1/0\r\nContent-Type: application/json\r\nContent-Length: 734\r\n\r\n{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"4\",\"lamport_timestamp\":\"2\",\"number_of_entries\":2}";
        Socket socket = new Socket();
        RequestInformation request = new RequestInformation(fullRequest, socket, "PUT", "PUT /weather.json HTTP/1.1", "4", "2", "1");
        assertEquals("1", request.getProcessId());
    }
}
