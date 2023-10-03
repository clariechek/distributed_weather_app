import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ListenerTest {
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
    @DisplayName("Add request to queue Test - PUT request")
    void testAddRequestToQueue1() {
        // Ensure request queue is empty. Then add request.
        Listener.getListener().clearRequestQueue();
        String fullRequest = "PUT /weather.json HTTP/1.1\r\nHost: localhost\r\nUser-Agent: ATOMClient/1/0\r\nContent-Type: application/json\r\nContent-Length: 734\r\n\r\n{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        Socket socket = new Socket();
        RequestInformation request = new RequestInformation(fullRequest, socket, "PUT", "PUT /weather.json HTTP/1.1", "1", "1", "1");
        Listener.getListener().addRequestToQueue(request);

        String output = outputStreamCaptor.toString();
        String expectedOutput = "1970-01-01 09:30:00 New request from Content Server 1 added to queue.\n";
        assertEquals(expectedOutput, output);
    }

    @Test
    @DisplayName("Add request to queue Test - GET request from GETClient")
    void testAddRequestToQueue2() {
        // Ensure request queue is empty. Then add request.
        Listener.getListener().clearRequestQueue();
        String fullRequest = "GET / HTTP/1.1\r\nHost: localhost\r\nAccept: application/json\r\nStationID: 1\r\nLamportTimestamp: 1\r\nProcessID: 1\r\n\r\n";
        Socket socket = new Socket();
        RequestInformation request = new RequestInformation(fullRequest, socket, "GET", "GET / HTTP/1.1", "1", "1", "1");
        Listener.getListener().addRequestToQueue(request);

        String output = outputStreamCaptor.toString();
        String expectedOutput = "1970-01-01 09:30:00 New request from GETClient with process ID 1 added to queue.\n";
        assertEquals(expectedOutput, output);
    }

    @Test
    @DisplayName("Add request to queue Test - Normal GET request")
    void testAddRequestToQueue3() {
        // Ensure request queue is empty. Then add request.
        Listener.getListener().clearRequestQueue();
        String fullRequest = "GET / HTTP/1.1\r\nHost: localhost\r\nAccept: text/html\r\n\r\n";
        Socket socket = new Socket();
        RequestInformation request = new RequestInformation(fullRequest, socket, "GET", "GET / HTTP/1.1", null, null, null);
        Listener.getListener().addRequestToQueue(request);

        String output = outputStreamCaptor.toString();
        String expectedOutput = "1970-01-01 09:30:00 New request added to queue.\n";
        assertEquals(expectedOutput, output);
    }
    
    /*
     * Request 1 with process id 1 and lamport timestamp 3 is added to queue first.
     * Request 2 with process id 2 and lamport timestamp 1 is added to queue second.
     * Queue should be ordered such that request 2 is first and request 1 is second.
     */
    @Test
    @DisplayName("Add request Test - Order request by lamport timestamp")
    void testAddRequest1() throws InterruptedException {
        // Ensure request queue is empty.
        Listener.getListener().clearRequestQueue();

        // Add request 1 with pid 1 and lamport timestamp 3 to queue.
        String fullRequest = "PUT /weather.json HTTP/1.1\r\nHost: localhost\r\nUser-Agent: ATOMClient/1/0\r\nContent-Type: application/json\r\nContent-Length: 734\r\n\r\n{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"3\",\"number_of_entries\":2}";
        Socket socket = new Socket();
        RequestInformation request = new RequestInformation(fullRequest, socket, "PUT", "PUT /weather.json HTTP/1.1", "1", "3", "1");
        Listener.getListener().addRequest(request);

        // Add request 2 with pid 2 and lamport timestamp 1 to queue.
        String fullRequest2 = "PUT /weather.json HTTP/1.1\r\nHost: localhost\r\nUser-Agent: ATOMClient/1/0\r\nContent-Type: application/json\r\nContent-Length: 734\r\n\r\n{\"process_id\":\"2\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"2\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        Socket socket2 = new Socket();
        RequestInformation request2 = new RequestInformation(fullRequest2, socket2, "PUT", "PUT /weather.json HTTP/1.1", "1", "1", "2");
        Listener.getListener().addRequest(request2);
        
        // Check if queue contains request in correct order.
        BlockingQueue<RequestInformation> output = Listener.getListener().getRequestQueue();
        assertEquals(request2.getContentServerId(), output.peek().getContentServerId());
        assertEquals(request2.getFullRequest(), output.peek().getFullRequest());
        assertEquals(request2.getLamportTimestamp(), output.peek().getLamportTimestamp());
        assertEquals(request2.getProcessId(), output.peek().getProcessId());
        output.take();
        assertEquals(request.getContentServerId(), output.peek().getContentServerId());
        assertEquals(request.getFullRequest(), output.peek().getFullRequest());
        assertEquals(request.getLamportTimestamp(), output.peek().getLamportTimestamp());
        assertEquals(request.getProcessId(), output.peek().getProcessId());
        
    }

    /*
     * Request 1 with process id 3 and lamport timestamp 2 is added to queue first.
     * Request 2 with process id 2 and lamport timestamp 2 is added to queue second.
     * Request 3 with process id 1 and lamport timestamp 2 is added to queue second.
     * Queue should be ordered request 1, 2, then 3.
     */
    @Test
    @DisplayName("Add request Test - Order request by process id when lamport timestamp is the same")
    void testAddRequest2() throws InterruptedException {
        // Ensure request queue is empty.
        Listener.getListener().clearRequestQueue();

        // Add request 1 with pid 3 and lamport timestamp 2 to queue.
        String fullRequest = "PUT /weather.json HTTP/1.1\r\nHost: localhost\r\nUser-Agent: ATOMClient/1/0\r\nContent-Type: application/json\r\nContent-Length: 734\r\n\r\n{\"process_id\":\"3\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"2\",\"number_of_entries\":2}";
        Socket socket = new Socket();
        RequestInformation request = new RequestInformation(fullRequest, socket, "PUT", "PUT /weather.json HTTP/1.1", "1", "2", "3");
        Listener.getListener().addRequest(request);

        // Add request 2 with pid 2 and lamport timestamp 2 to queue.
        String fullRequest2 = "PUT /weather.json HTTP/1.1\r\nHost: localhost\r\nUser-Agent: ATOMClient/1/0\r\nContent-Type: application/json\r\nContent-Length: 734\r\n\r\n{\"process_id\":\"2\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"2\",\"lamport_timestamp\":\"2\",\"number_of_entries\":2}";
        Socket socket2 = new Socket();
        RequestInformation request2 = new RequestInformation(fullRequest2, socket2, "PUT", "PUT /weather.json HTTP/1.1", "2", "2", "2");
        Listener.getListener().addRequest(request2);

         // Add request 3 with pid 1 and lamport timestamp 2 to queue.
        String fullRequest3 = "PUT /weather.json HTTP/1.1\r\nHost: localhost\r\nUser-Agent: ATOMClient/1/0\r\nContent-Type: application/json\r\nContent-Length: 734\r\n\r\n{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"3\",\"lamport_timestamp\":\"2\",\"number_of_entries\":2}";
        Socket socket3 = new Socket();
        RequestInformation request3 = new RequestInformation(fullRequest3, socket3, "PUT", "PUT /weather.json HTTP/1.1", "3", "2", "1");
        Listener.getListener().addRequest(request3);
        
        // Check if queue contains request in correct order.
        BlockingQueue<RequestInformation> output = Listener.getListener().getRequestQueue();
        assertEquals(request3.getContentServerId(), output.peek().getContentServerId());
        assertEquals(request3.getFullRequest(), output.peek().getFullRequest());
        assertEquals(request3.getLamportTimestamp(), output.peek().getLamportTimestamp());
        assertEquals(request3.getProcessId(), output.peek().getProcessId());
        output.take();
        assertEquals(request2.getContentServerId(), output.peek().getContentServerId());
        assertEquals(request2.getFullRequest(), output.peek().getFullRequest());
        assertEquals(request2.getLamportTimestamp(), output.peek().getLamportTimestamp());
        assertEquals(request2.getProcessId(), output.peek().getProcessId());
        output.take();
        assertEquals(request.getContentServerId(), output.peek().getContentServerId());
        assertEquals(request.getFullRequest(), output.peek().getFullRequest());
        assertEquals(request.getLamportTimestamp(), output.peek().getLamportTimestamp());
        assertEquals(request.getProcessId(), output.peek().getProcessId());
        
    }

    @Test
    @DisplayName("Clear request queue Test")
    void testClearRequestQueue() {
        // Clear queue and add 1 request
        Listener.getListener().clearRequestQueue();
        String fullRequest = "GET / HTTP/1.1\r\nHost: localhost\r\nAccept: text/html\r\n\r\n";
        Socket socket = new Socket();
        RequestInformation request = new RequestInformation(fullRequest, socket, "GET", "GET / HTTP/1.1", null, null, null);
        Listener.getListener().addRequestToQueue(request);

        // Check if queue contains 1 request
        int output = Listener.getListener().getRequestQueue().size();
        int expectedOutput = 1;
        assertEquals(expectedOutput, output);

        // Check if queue is empty.
        Listener.getListener().clearRequestQueue();
        boolean output2 = Listener.getListener().getRequestQueue().isEmpty();
        boolean expectedOutput2 = true;
        assertEquals(expectedOutput2, output2);
    }

    @Test
    @DisplayName("Get request queue Test")
    void testGetRequestQueue() {
        // Clear queue and add 1 request
        Listener.getListener().clearRequestQueue();
        String fullRequest = "GET / HTTP/1.1\r\nHost: localhost\r\nAccept: text/html\r\n\r\n";
        Socket socket = new Socket();
        RequestInformation request = new RequestInformation(fullRequest, socket, "GET", "GET / HTTP/1.1", "1", "3", "2");
        Listener.getListener().addRequestToQueue(request);

        // Get request queue and check if request is correct
        BlockingQueue<RequestInformation> output = Listener.getListener().getRequestQueue();
        assertEquals(request.getFullRequest(), output.peek().getFullRequest());
        assertEquals(request.getRequestType(), output.peek().getRequestType());
        assertEquals(request.getRequestStartLine(), output.peek().getRequestStartLine());
        assertEquals(request.getContentServerId(), output.peek().getContentServerId());
        assertEquals(request.getLamportTimestamp(), output.peek().getLamportTimestamp());
        assertEquals(request.getProcessId(), output.peek().getProcessId());
    }

}
