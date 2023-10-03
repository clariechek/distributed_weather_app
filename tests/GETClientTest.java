import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GETClientTest {
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
    @DisplayName("Build GET Request Header Test")
    void testBuildRequestHeader() {
        GETClient getClient = new GETClient();
        String output = getClient.buildRequestHeader("GET", "/", "localhost", "application/json", "1", "1", "1");
        String expectedOutput = "GET / HTTP/1.1\r\nHost: localhost\r\nAccept: application/json\r\nStationID: 1\r\nLamportTimestamp: 1\r\nProcessID: 1\r\n\r\n";
        assertEquals(expectedOutput, output);
    }
}
