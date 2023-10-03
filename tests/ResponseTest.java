import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ResponseTest {
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
    @DisplayName("Create Response Headers Test")
    void testCreateResponseHeaders() {
        Socket socket = new Socket();
        Response response = new Response("OK", "200", "0", null, "GET / HTTP/1.1", null, socket, false);
        response.createResponseHeaders("text/html");
        assertEquals("HTTP/1.1 200 OK", response.getHeaders().get(0));
        assertEquals("Content-Type: text/html", response.getHeaders().get(1));
        assertEquals("Content-Length: 0", response.getHeaders().get(2));
        assertEquals("Connnection: close", response.getHeaders().get(3));
    }

    @Test
    @DisplayName("Build Error page Test")
    void testBuildErrorPage() {
        Socket socket = new Socket();
        Response response = new Response("Internal Server Error", "500", "0", null, "PUT /weather.json HTTP/1.1", "Invalid JSON - Missing ID field.", socket, false);
        String output = response.buildErrorPage("500", "Internal Server Error", "Invalid JSON - Missing ID field.");
        String expectedOutput = "HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/html\r\n\r\n<!DOCTYPE html>\r\n<html>\r\n<head>\r\n<title>500 Internal Server Error</title>\r\n</head>\r\n<body>\r\n<h1>500 Internal Server Error</h1>\r\n<p>Invalid JSON - Missing ID field.</p>\r\n</body>\r\n</html>\r\n";
        assertEquals(expectedOutput, output);
    }

    @Test
    @DisplayName("Build Html page Test")
    void testBuildHtmlPage() {
        Socket socket = new Socket();
        Response response = new Response("OK", "200", "0", null, "GET / HTTP/1.1", null, socket, true);
        String output = response.buildHtmlPage(null);
        String expectedOutput = "<!DOCTYPE html>\r\n<html>\r\n<head>\r\n<title>Aggregation Web Server</title>\r\n</head>\r\n<body>\r\n<h1>Aggregation Web Server</h1>\r\n<p>(204) No content found in database.</p>\r\n</body>\r\n</html>\r\n";
        assertEquals(expectedOutput, output);
    }

    @Test
    @DisplayName("Send response Test")
    void testSendResponse() {
        Socket socket = new Socket();
        String content = "{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        Response response = new Response("Created", "201", Integer.toString(content.length()), content, "PUT /weather.json HTTP/1.1", null, socket, true);
        response.sendResponse();
        String output = outputStreamCaptor.toString();
        String expectedOutput = "1970-01-01 09:30:00 201(Created): PUT /weather.json HTTP/1.1\n1970-01-01 09:30:00 Error sending response: Socket is not connected\n";
        assertEquals(expectedOutput, output);
    }
}