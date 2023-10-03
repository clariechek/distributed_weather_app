
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LamportClock {
    private static final String FILE_SERVER_LAMPORT = "LAMPORT_AGGREGATION_SERVER.txt";
    private static final String FILE_PID = "PID_COUNTER.txt";

    private static int nextPidToHandle = 0;
    private int lamportTimestamp;
    private int processId;

    /*
     * Constructor for LamportClock class.
     */
    public LamportClock(int processId, int lamportTimestamp) {
        this.processId = processId;
        this.lamportTimestamp = lamportTimestamp;
    }
    
    /*
     * If new session, initialise lamport clock. Otherwise retrive lamport clock
     * data from LAMPORT_AGGREGATION_SERVER.txt file.
     */
    public static LamportClock initialiseAggregationServerClock() {
        File file = new File("LAMPORT_AGGREGATION_SERVER.txt");
        LamportClock lamportClock = null;
        if (file.exists()) {
            // If aggregation server crashed, reset lamport clock to timestamp from file.
            lamportClock = new LamportClock(0,0);
            int[] lamportData = lamportClock.getPreviousLamportData();
            if (lamportData != null) {
                lamportClock.setProcessId(lamportData[0]);
                lamportClock.setLamportTimestamp(lamportData[1]);
            }
        } else {
            // New aggregation server, initialise lamport clock to 0 and pid counter to 0.
            lamportClock = new LamportClock(0,0);
            lamportClock.initialiseLamportFile();
            lamportClock.initialisePidFile();
        }
        return lamportClock;
    }

    /*
     * Deletes the LAMPORT_AGGREGATION_SERVER.txt and PID_COUNTER files 
     * when the aggregation server is shut down properly.
     */
    public void deleteLamportAndPidFiles() {
        File file = new File(FILE_SERVER_LAMPORT);
        file.deleteOnExit();

        File pidFile = new File(FILE_PID);
        pidFile.deleteOnExit();
    }

    /*
     * Set the timestamp of the lamport clock.
     */
    public void setLamportTimestamp(int time) {
        this.lamportTimestamp = time;
    }

    /*
     * Set the process id of the lamport clock.
     */
    public void setProcessId(int processId) {
        this.processId = processId;
    }

    /*
     * Returns the timestamp of the lamport clock.
     */
    public int getLamportTimestamp() {
        return this.lamportTimestamp;
    }

    /*
     * Returns the process id of the lamport clock.
     */
    public int getProcessId() {
        return this.processId;
    }

    /*
     * Increments the timestamp of the lamport clock by 1 when an event occurs. 
     * An event is considered to be a PUT/GET request that is sent or received.
     */
    public void tick() {
        this.lamportTimestamp++;
    }

    /*
     * Updates the timestamp of the aggregation server's lamport clock when a 
     * request is received so that it is synchronised.
     */
    public void syncLamportTimestamp(int sent_lamportTimestamp) {
        this.lamportTimestamp = Math.max(this.lamportTimestamp, sent_lamportTimestamp) + 1;
    }

    /*
     * Creates a LAMPORT_AGGREGATION_SERVER.txt file to store the lamport clock 
     * of the aggregation server. If the server crashes, the lamport clock can 
     * be retrieved from this file.
     */
    public void initialiseLamportFile() {
        try {
            File file = new File(FILE_SERVER_LAMPORT);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(FILE_SERVER_LAMPORT);
            fw.write("Process ID: 0\nLamport Timestamp: 0");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Creates a PID_COUNTER.txt file to store the process id counter. Each
     * content server and GETClient instance is allocated a unique pid.
     */
    public void initialisePidFile() {
        try {
            File file = new File(FILE_PID);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(FILE_PID);
            fw.write("Current Pid Allocated: 0");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Returns the previous lamport clock data from the LAMPORT_AGGREGATION_SERVER.txt file.
     * If the file does not exist, returns null.
     * Return value: int[0] = process id, int[1] = lamport timestamp.
     */
    public int[] getPreviousLamportData() {
        int pid = 0;
        int time = 0;
        int[] result = new int[2];
        try {
            File file = new File(FILE_SERVER_LAMPORT);
            if (file.exists()) {
                FileReader fileReader = new FileReader(file);
                BufferedReader br = new BufferedReader(fileReader);
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("Process ID:")) {
                        pid = Integer.parseInt(line.substring(line.indexOf(":") + 2));
                    } else if (line.contains("Lamport Timestamp:")) {
                        time = Integer.parseInt(line.substring(line.indexOf(":") + 2));
                    }
                }
                br.close();
                
                result[0] = pid;
                result[1] = time;
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * Updates the LAMPORT_AGGREGATION_SERVER.txt file with the new lamport clock data.
     */
    public void updateLamportFile(int newTimestamp) {
        try {
            File file = new File(FILE_SERVER_LAMPORT);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(FILE_SERVER_LAMPORT);
            fw.write("Process ID: " + this.processId + "\nLamport Timestamp: " + newTimestamp);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Returns the next pid to handle in the request queue.
     */
    public int getNextPidToHandle() {
        nextPidToHandle++;
        return nextPidToHandle;
    }

    /*
     *  Returns a unique pid to allocate to a new content server or GETClient.
     */
    public static int getNewPid() {
        int newPid = -1;
        try {
            File file = new File(FILE_PID);
            if (file.exists()) {
                FileReader fileReader = new FileReader(file);
                BufferedReader br = new BufferedReader(fileReader);
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("Current Pid Allocated:")) {
                        newPid = Integer.parseInt(line.substring(line.indexOf(":") + 2));
                        newPid++;
                        break;
                    }
                }
                br.close();

                FileWriter fw = new FileWriter(FILE_PID);
                fw.write("Current Pid Allocated: " + newPid);
                fw.close();

                System.out.println("New PID Allocated: " + newPid);
                return newPid;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newPid;
    }
}