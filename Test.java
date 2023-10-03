import java.io.File;
import java.io.FileWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class Test {
    public static void main(String[] args) throws Exception {
        // Ensure request queue is empty.
        Listener.getListener().clearRequestQueue();
        System.out.println("Request queue is empty: " + Listener.getListener().getRequestQueue().isEmpty() + "\n");

        // Add request 1 with pid 1 and lamport timestamp 3 to queue.
        String fullRequest = "PUT /weather.json HTTP/1.1\r\nHost: localhost\r\nUser-Agent: ATOMClient/1/0\r\nContent-Type: application/json\r\nContent-Length: 734\r\n\r\n{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"3\",\"number_of_entries\":2}";
        Socket socket = new Socket();
        RequestInformation request = new RequestInformation(fullRequest, socket, "PUT", "PUT /weather.json HTTP/1.1", "1", "3", "1");
        Listener.getListener().addRequest(request);

        System.out.println("Main: Request queue remaining capacity: " + Listener.getListener().getRequestQueue().remainingCapacity());
        // Add request 2 with pid 2 and lamport timestamp 1 to queue.
        String fullRequest2 = "PUT /weather.json HTTP/1.1\r\nHost: localhost\r\nUser-Agent: ATOMClient/1/0\r\nContent-Type: application/json\r\nContent-Length: 734\r\n\r\n{\"process_id\":\"2\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"2\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}";
        Socket socket2 = new Socket();
        RequestInformation request2 = new RequestInformation(fullRequest2, socket2, "PUT", "PUT /weather.json HTTP/1.1", "1", "1", "2");
        Listener.getListener().addRequest(request2);
        
        // Check if queue contains request in correct order.
        BlockingQueue<RequestInformation> output = Listener.getListener().getRequestQueue();

        // DataUtil.getDataUtil().clearDatabase();
        // // Create weather.json file that is in directory when aggregation server crashes
        // File file = new File("weather.json");
        // if (file.exists()) {
        //     file.delete();
        //     file.createNewFile();
        // } else {
        //     file.createNewFile();
        // }
        // FileWriter fileWriter = new FileWriter("weather.json");
        // String expectedOutput = "[{\"process_id\":\"1\",\"data\":[{\"apparent_t\":9.5,\"wind_spd_kmh\":15,\"rel_hum\":60,\"lon\":138.6,\"dewpt\":5.7,\"wind_spd_kt\":8,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":13.3,\"cloud\":\"Partly cloudy\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Adelaide (West Terrace /  ngayirdapira)\",\"id\":\"IDS60901\",\"state\":\"SA\",\"press\":1023.9,\"lat\":-34.9},{\"apparent_t\":8.5,\"wind_spd_kmh\":12,\"rel_hum\":53,\"lon\":249.3,\"dewpt\":2.1,\"wind_spd_kt\":5,\"wind_dir\":\"S\",\"time_zone\":\"CST\",\"air_temp\":10.2,\"cloud\":\"Sunny\",\"local_date_time_full\":\"20230715160000\",\"local_date_time\":\"15/04:00pm\",\"name\":\"Mount Gambier\",\"id\":\"IDS60902\",\"state\":\"SA\",\"press\":1693.5,\"lat\":-23.1}],\"station_id\":\"1\",\"lamport_timestamp\":\"1\",\"number_of_entries\":2}]";
        // fileWriter.write(expectedOutput);
        // fileWriter.close();
        
        // // Upload weather.json file to database
        // DataUtil.getDataUtil().restartDatabaseAfterCrash();
        // DataUtil.getDataUtil().printDatabaseContent();
        // // Check if database is updated as expected
        // String output = DataUtil.getDataUtil().getAllDataFromDatabase();

        // // Test get all data from database function
        // String result = DataUtil.getDataUtil().getAllDataFromDatabase();
        // System.out.println();
        // System.out.println("Result: " + result);

        // // Run aggregation server and then crash it test
        // AggregationServer aggregationServer = new AggregationServer();
        // aggregationServer.startServer(args);

        // String[] contentServerArgs = new String[3];
        // contentServerArgs[0] = "http://localhost:4567";
        // contentServerArgs[1] = "1";
        // contentServerArgs[2] = "cs1_1.txt";

        // ContentServer.main(contentServerArgs);

        // // Crash server
        // Crash.main();


        // // Run aggregation server and content server test
        // AggregationServer aggregationServer = new AggregationServer();
        // aggregationServer.startServer(args);
        // // Start Content server and send PUT request
        // String[] contentServerArgs = new String[3];
        // contentServerArgs[0] = "http://localhost:4567";
        // contentServerArgs[1] = "1";
        // contentServerArgs[2] = "cs1_1.txt";

        // ContentServer.main(contentServerArgs);
        // // Automatically shutdown in 5 minutes
        // try {
        //     Thread.sleep(300000);
        // } catch (InterruptedException e) {
        //     e.printStackTrace();
        // }
        // aggregationServer.stopServer();
    }
}
