# distributed_weather_app
A distributed weather application that consists of an aggregation server, content servers and GET Clients. The project uses JUnit Jupiter framework for automated unit testing.

## Architecture
The overall architecture is shown in the image below.
![architecture](https://github.com/clariechek/distributed_weather_app/assets/44283405/121589e0-1cf5-463e-99f1-2b26e6c569a1)
The content servers send PUT requests and Get Clients send GET requests to the aggregation server. 
The aggregation server listens for incoming connection requests. When the aggregation server accepts an incoming request, it saves the request information into a RequestInformation object. Furthermore, if it is a PUT request, it saves the request body into an intermediate .json file to be processed later. 
The Listener adds the RequestInformation into the RequestQueue which ensures that the requests are ordered according to their lamport timestamp and process id.
The Handler processes one request at the time, always processing the first request in the RequestQueue. It uploads or fetches data from the local database and updates the external weather.json database if any changes are made. Once it is done processing the request, it returns the HTTP response code to the content servers and get clients.

### Aggregation Server
The aggregation server (web server) accepts a port number (default port: 4567). It contains a lamport clock and a semaphore. The semaphore only permits 1 thread to access the database at a time.
When it is started, it checks if the server has crashed before by checking if the weather.json file exists.
- If the weather.json file exists, that means the server crashed unexpectedly. The aggregation server then reinitialises it database and lamport clock from the weather.json, LAMPORT_AGGREGATION_SERVER.txt, and PID_COUNTER.txt files respectively.
- If the weather.json file does not exist, that means the server did not crash and this is a new start up. The aggregation server will initialise a new lamport clock.

The aggregation server checks if the request is a PUT or GET request. 
- If it is a PUT request, the aggregation server stores the request body into an intermediate .json file to be processed later, and stores all the relevant request information into a RequestInformation object. The naming convention for this intermediate json file is cs1_p2.json where the 1 is the content server id, and the 2 is the process id.
- If it is a GET request, the aggregation server stores all the relevant request information into a RequestInformation object.
- If it is not a PUT or GET request, the aggregation server returns 400 Bad Request.

Each time the aggregation server receives a request, it increments its lamport clock time by 1 using the tick() method in LamportClock. Then it checks the lamport time of the request and updates its lamport timestamp to max(aggregation_server_timestamp, request_timestamp) + 1. The lamport information is then saved to the LAMPORT_AGGREGATION_SERVER.txt file.
Then it calls the Listener to add the request to the RequestQueue. The Listener checks the request's lamport timestamp and process id to order them accordingly. Requests with the smaller lamport timestamp will be processed first. If the timestamps are the same, then the request with the smaller process id is processed first.
The Listener then passes the first request in the queue to the aggregation server.
The aggregation server passes the first request in the queue and its semaphore to the Handler.
The Handler then acquires the semaphore, causing other threads to wait before processing their request. The Handler process requests as follows:
- If it is a PUT request, it checks if the intermediate .json file contains any data. If yes, then it uploads the data to the server's local database and weather.json file, and deletes the intermediate .json file. Otherwise, it returns a 204 No Content response. 
It also checks if this is the content server's first connection. If yes, it adds its id to the list of connected content servers and starts a 30 second TimerTask that will delete the content server and its data from the server after 30 seconds. Otherwise, if this is not the content server's first connection, it will cancel its existing TimerTask and create a new one for it.
- If it is a GET request, it checks if the data is present in the database. If yes, it returns the data and 200 OK response. Otherwise, it returns 404 Not Found response.

The aggregation server automatically shutdowns in 2 minutes. This will trigger the server to automatically delete the LAMPORT_AGGREGATION_SERVER.txt, PID_COUNTER.txt, and weather.json files.
However, if the server is interrupted whether through CTRL+C in the terminal, or due to a crash, the files will not be automatically deleted. 

LAMPORT_AGGREGATION_SERVER.txt: This file contains the aggregation server's process id and lamport timestamp. If the server crashes, its lamport clock is restored using this file. By default, the aggregation server's process id is always 0.
PID_COUNTER.txt: This file contains an integer that keeps that of the next unique process id that can be allocated to a content server or get client. 

### Content Server
The content server (weather station) accepts a url containing the server name and port number (http://localhost:4567), a station ID (the station ID of this content server), and a file name for its data (eg. cs1_1.txt, cs1_2.txt, cs2_1.txt, etc).
It contains a lamport clock and weather entry array that stores each entry in the .txt file.

When it is started, the content server gets a unique process id from the PID_COUNTER.txt file, and initialises its lamport clock with the new process id and a lamport timestamp of 0.
The content server connects to the aggregation server and parses the text in the .txt file into a weather entry array. 
It then converts the weather entry array into a JSONObject.

Each time the content server sends a PUT request, it increments its lamport clock time using the tick() method in LamportClock.
### GET Client

## Compilation
1. In the project folder run the instruction below. This will compile the files and create the corresponding `.class` files in the `bin` folder.
```
make
```

2. Navigate to the `bin` folder using the below command. This is where we will start the Java RMI Registry, start the server, and run the client.
```
cd bin
```

## Run the Application
1. In the `bin` directory, run the instruction below to start Java RMI Registry (for Solaris(tm) Operating System)
```
rmiregistry &
```
or if you're using Windows, run this:
```
start rmiregistry 
```
The default port the registry runs on is 1099.

2. Next, start the server using the following command (for Solaris(tm) Operating System)
```
java -classpath ./ CalculatorServer &
```
or for Windows, run this:
```
start java -classpath ./ CalculatorServer
```
The output should be the following:
```
Server ready
```

3. Finally, run the client using the command below. To run multiple clients parallelly, run the command below in each terminal for each client. 
```
java  -classpath ./ CalculatorClient
```

## Testing
### Setup
1. The test script is located in the `tests` folder. To run the tests in Visual Studio Code, you will need:
- JDK (version 1.8 or later)
- Visual Studio Code (version 1.59.0 or later)
- [Visual Studio Code Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)


2. Follow the 'Get Started with Java Development' instructions on Visual Studio Code by clicking the `Install JDK` button (on the left of the screenshot below) and blue `Download` button (on the right of the screenshot below).
<img width="1137" alt="Screenshot 2023-08-07 at 2 58 49 pm" src="https://github.com/clariechek/JavaRMI/assets/44283405/37a2d5c7-5e6b-4ee9-9e73-355de496eea7">

### Running automated tests
1. Open the project folder and select the test script `CalculatorImplementationTest.java`.

2. Click on the testing button on the left sidebar.
<img width="1378" alt="Screenshot 2023-08-07 at 3 04 17 pm" src="https://github.com/clariechek/JavaRMI/assets/44283405/27fd4cb0-e693-40f2-b7dd-86ce8e729259">


3. Select Enable Java Tests
<img width="275" alt="Screenshot 2023-08-07 at 3 05 17 pm" src="https://github.com/clariechek/JavaRMI/assets/44283405/492fd8da-0ba4-4b94-b5a4-4a81d1df0821">

4. In the dropdown menu, select JUnit Jupiter.
<img width="275" alt="Screenshot 2023-08-07 at 3 05 47 pm" src="https://github.com/clariechek/JavaRMI/assets/44283405/ced2905b-79b0-45cf-a85e-e9506c0ff9f9">


5. To run the tests, select the play button next to the JavaRMI title as shown in the video link below. Alternatively, you can run each test individually by clicking the play button next to each test title.
https://github.com/clariechek/JavaRMI/assets/44283405/0ff80949-5b6b-4d0e-a4d6-f8239762fa63


For more information on Java testing in VS Code: https://code.visualstudio.com/docs/java/java-testing
