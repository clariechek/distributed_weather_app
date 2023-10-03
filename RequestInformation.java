
import java.net.Socket;

public class RequestInformation {
    private String fullRequest;
    private Socket socket;
    private String requestType;
    private String requestStartLine;
    private String contentServerId;
    private String lamportTimestamp;
    private String processId;

    public RequestInformation(String fullRequest, Socket socket, String requestType, String requestStartLine, String contentServerId, String lamportTimestamp, String processId) {
        this.fullRequest = fullRequest;
        this.socket = socket;
        this.requestType = requestType;
        this.requestStartLine = requestStartLine;
        this.contentServerId = contentServerId;
        this.lamportTimestamp = lamportTimestamp;
        this.processId = processId;
    }

    public String getFullRequest() {
        return fullRequest;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getRequestStartLine() {
        return requestStartLine;
    }

    public String getContentServerId() {
        return contentServerId;
    }

    public String getLamportTimestamp() {
        return lamportTimestamp;
    }

    public String getProcessId() {
        return processId;
    }
}