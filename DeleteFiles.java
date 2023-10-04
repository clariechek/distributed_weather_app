/*
 * Helper function to delete weather.json, LAMPORT_AGGREGATION_SERVER.txt, and PID_COUNTER.txt files 
 * from project root directory during testing.
 */

public class DeleteFiles {
    public static void main(String[] args) throws Exception {
        DataUtil.getDataUtil().clearDatabase();
        LamportClock lamportClock = new LamportClock(0, 0);
        lamportClock.deleteLamportAndPidFiles();
    }
}
