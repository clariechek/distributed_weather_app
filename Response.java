
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;

public class Response {
    private String status;
    private String statusCode;
    private String content_length;
    private String body;
    private String requestStartLine;
    private String errorDescription;
    private Socket socket;
    private ArrayList<String> headers = new ArrayList<String>();
    private boolean isFromContentServerOrGetClient;

    /*
     * Create a Response instance.
     */
    public Response(String status, String statusCode, String content_length, String body, String requestStartLine, String errorDescription, Socket socket, boolean isFromContentServerOrGetClient) {
        this.status = status;
        this.statusCode = statusCode;
        this.content_length = content_length;
        this.body = body;
        this.requestStartLine = requestStartLine;
        this.errorDescription = errorDescription;
        this.socket = socket;
        this.isFromContentServerOrGetClient = isFromContentServerOrGetClient;
    }

    /*
     * Create headers for the response.
     */
    public void createResponseHeaders(String contentType) {
        headers.add("HTTP/1.1 " + this.statusCode + " " + this.status);
        if (!contentType.equals("")) {
            headers.add("Content-Type: " + contentType);
        }
        headers.add("Content-Length: " + this.content_length);
        headers.add("Connnection: close");
    }

    /*
     * Returns String representation of the error page.
     */
    public String buildErrorPage(String errorCode, String errorName, String errorDescription) {
        String errorPage = "HTTP/1.1 " + errorCode + " " + errorName + "\r\n";
        errorPage += "Content-Type: text/html\r\n";
        errorPage += "\r\n";
        errorPage += "<!DOCTYPE html>\r\n";
        errorPage += "<html>\r\n";
        errorPage += "<head>\r\n";
        errorPage += "<title>" + errorCode + " " + errorName + "</title>\r\n";
        errorPage += "</head>\r\n";
        errorPage += "<body>\r\n";
        errorPage += "<h1>" + errorCode + " " + errorName + "</h1>\r\n";
        errorPage += "<p>" + errorDescription + "</p>\r\n";
        errorPage += "</body>\r\n";
        errorPage += "</html>\r\n";
        return errorPage;
    }

    /*
     * Returns String representation of the HTML page.
     */
    public String buildHtmlPage(String body) {
        String htmlPage ="<!DOCTYPE html>\r\n";
        htmlPage += "<html>\r\n";
        htmlPage += "<head>\r\n";
        htmlPage += "<title>Aggregation Web Server</title>\r\n";
        htmlPage += "</head>\r\n";
        htmlPage += "<body>\r\n";
        if (!isFromContentServerOrGetClient) {
            htmlPage += "<h1>Aggregation Web Server</h1>\r\n";
        } else if (body == null || body.isEmpty()) {
            htmlPage += "<h1>Aggregation Web Server</h1>\r\n";
            htmlPage += "<p>(204) No content found in database.</p>\r\n";
        } else {
            htmlPage += "<h1>Aggregation Web Server</h1>\r\n";
            htmlPage += "<p>" + body + "</p>";
        }
        htmlPage += "</body>\r\n";
        htmlPage += "</html>\r\n";
        return htmlPage;
    }

    /*
     * Sends HTTP response and HTML page to client.
     */
    public void sendResponse() {
        LogUtil.write(this.statusCode + "(" + this.status + "): " + this.requestStartLine);
        try {
            PrintStream printer = new PrintStream(socket.getOutputStream());
            if (!this.isFromContentServerOrGetClient) {
                if (this.statusCode == "200" || this.statusCode == "201" || this.statusCode == "204") {
                    createResponseHeaders("text/html");
                    printer.println(headers.get(0));
                    printer.println(headers.get(1));
                    printer.println(headers.get(2));
                    printer.println(headers.get(3));
                    printer.println("\r\n\r\n");
                    printer.println(buildHtmlPage(this.body));
                } else {
                    buildErrorPage(this.statusCode, this.status, this.errorDescription);
                }
            } else {
                // GET from GETClient or PUT from Content Server
                if (this.statusCode == "200" || this.statusCode == "201" || this.statusCode == "204") {
                    createResponseHeaders("application/json");
                    printer.println(headers.get(0));
                    printer.println(headers.get(1));
                    printer.println(headers.get(2));
                    printer.println(headers.get(3));
                    printer.println("\r\n\r\n");
                    printer.println(this.body);
                } else {
                    createResponseHeaders("");
                    printer.println(headers.get(0));
                    printer.println(headers.get(1));
                    printer.println(headers.get(2));
                    printer.println("\r\n\r\n");
                }
            }
            
        } catch (Exception e) {
            LogUtil.write("Error sending response: " + e.getMessage());
        }
    }
}