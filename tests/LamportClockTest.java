import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LamportClockTest {
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
    @DisplayName("Initialise Aggregation Server Clock Test - Normal start up")
    void testInitialiseAggregationServerClock1() {
        // Delete LAMPORT_AGGREGATION_SERVER.txt and PID_COUNTER.txt if they exist.
        File file = new File("LAMPORT_AGGREGATION_SERVER.txt");
        if (file.exists()) {
            file.delete();
        }
        File pidFile = new File("PID_COUNTER.txt");
        if (pidFile.exists()) {
            pidFile.delete();
        }

        // Initialise aggregation server lamport clock
        LamportClock lamportClock = LamportClock.initialiseAggregationServerClock();
        
        // Check that lamport clock returned has process id 0 and timestamp 0.
        assertEquals(0, lamportClock.getProcessId());
        assertEquals(0, lamportClock.getLamportTimestamp());

        // Check that LAMPORT_AGGREGATION_SERVER.txt and PID_COUNTER.txt exists
        assertTrue(file.exists());
        assertTrue(pidFile.exists());
    }

    @Test
    @DisplayName("Initialise Aggregation Server Clock Test - Restart after server crash")
    void testInitialiseAggregationServerClock2() throws IOException {
        // Ensure LAMPORT_AGGREGATION_SERVER.txt and PID_COUNTER.txt exist.
        File file = new File("LAMPORT_AGGREGATION_SERVER.txt");
        if (file.exists()) {
            file.delete();
            file.createNewFile();
        } else {
            file.createNewFile();
        }

        // Set process id and timestamp in LAMPORT_AGGREGATION_SERVER.txt to 0 and 3 respectively
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("Process ID: 0\nLamport Timestamp: 3");
        fileWriter.close();

        File pidFile = new File("PID_COUNTER.txt");
        if (pidFile.exists()) {
            pidFile.delete();
            pidFile.createNewFile();
        } else {
            pidFile.createNewFile();
        }

        // Set process id in PID_COUNTER.txt to 2
        fileWriter = new FileWriter(pidFile);
        fileWriter.write("Current Pid Allocated: 2");
        fileWriter.close();

        // Initialise aggregation server lamport clock
        LamportClock lamportClock = LamportClock.initialiseAggregationServerClock();
        
        // Check that lamport clock returned has process id 0 and timestamp 0.
        assertEquals(0, lamportClock.getProcessId());
        assertEquals(3, lamportClock.getLamportTimestamp());
    }

    @Test
    @DisplayName("Set lamport timestamp Test")
    void testSetLamportTimestamp() {
        // Initialise lamport clock
        LamportClock lamportClock = new LamportClock(0, 0);

        // Set lamport timestamp to 5
        lamportClock.setLamportTimestamp(5);

        // Check that lamport timestamp is 5
        assertEquals(5, lamportClock.getLamportTimestamp());
    }

    @Test
    @DisplayName("Set process id Test")
    void testSetProcessId() {
        // Initialise lamport clock
        LamportClock lamportClock = new LamportClock(0, 0);

        // Set lamport timestamp to 5
        lamportClock.setProcessId(8);

        // Check that lamport timestamp is 5
        assertEquals(8, lamportClock.getProcessId());
    }

    @Test
    @DisplayName("Tick Test") 
    void testTick() {
        // Initialise lamport clock
        LamportClock lamportClock = new LamportClock(0, 0);

        // Increment lamport clock by 1
        lamportClock.tick();

        // Check that lamport timestamp is 1
        assertEquals(1, lamportClock.getLamportTimestamp());
    }

    @Test
    @DisplayName("Sync Lamport Timestamp Test")
    void testSyncLamportTimestamp() {
        // Initialise lamport clock
        LamportClock lamportClock = new LamportClock(0, 0);

        // Set lamport timestamp to 5
        lamportClock.setLamportTimestamp(5);

        // Sync lamport timestamp to 10
        lamportClock.syncLamportTimestamp(10);

        // Check that lamport timestamp is 11
        assertEquals(11, lamportClock.getLamportTimestamp());
    }

    @Test
    @DisplayName("Get Previous lamport data Test")
    void testGetPreviousLamportData() throws IOException {
        // Ensure LAMPORT_AGGREGATION_SERVER.txt exists.
        File file = new File("LAMPORT_AGGREGATION_SERVER.txt");
        if (file.exists()) {
            file.delete();
            file.createNewFile();
        } else {
            file.createNewFile();
        }

        // Write lamport process id 0 and timestamp 43 to LAMPORT_AGGREGATION_SERVER.txt
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("Process ID: 0\nLamport Timestamp: 43");
        fileWriter.close();

        // Get lamport data and check if it is correct
        LamportClock lamportClock = LamportClock.initialiseAggregationServerClock();
        int[] lamportData = lamportClock.getPreviousLamportData();
        assertEquals(0, lamportData[0]);
        assertEquals(43, lamportData[1]);
    }

    @Test
    @DisplayName("Update Lamport File Test")
    void testUpdateLamportFile() throws IOException {
        // Initialise lamport clock
        LamportClock lamportClock = new LamportClock(0, 121);

        // Update lamport clock to process id 0 and timestamp 122
        lamportClock.updateLamportFile(122);

        // Get data from LAMPORT_AGGREGATION_SERVER.txt file
        String output = new String(Files.readAllBytes(Paths.get("LAMPORT_AGGREGATION_SERVER.txt")));
        String expectedOutput = "Process ID: 0\nLamport Timestamp: 122";

        // Check that lamport clock is updated
        assertEquals(expectedOutput, output);
    }

    @Test
    @DisplayName("Get Next Pid to Handle Test")
    void testGetNextPidToHandle() throws IOException {
        // Initialise lamport clock
        LamportClock lamportClock = new LamportClock(0, 0);
        int output = lamportClock.getNextPidToHandle();
        int expectedOutput = 1;
        assertEquals(expectedOutput, output);

        int output2 = lamportClock.getNextPidToHandle();
        int expectedOutput2 = 2;
        assertEquals(expectedOutput2, output2);
    }

    @Test
    @DisplayName("Get New Pid Test")
    void testGetNewPid() throws IOException {
        // Ensure PID_COUNTER.txt exists.
        File file = new File("PID_COUNTER.txt");
        if (file.exists()) {
            file.delete();
            file.createNewFile();
        } else {
            file.createNewFile();
        }

        // Set current pid to 29
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("Current Pid Allocated: 29");
        fileWriter.close();

        // Get new PID and check if it is correct.
        int output = LamportClock.getNewPid();
        int expectedOutput = 30;
        assertEquals(expectedOutput, output);
    }
}
