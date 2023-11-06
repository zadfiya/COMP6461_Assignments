package Temp;

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {
    static final String SERVER = "Temp.Server: httpfs/1.0.0";
    static final String DATE = "Date: ";
    static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin: *";
    static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials: true";
    static final String VIA = "Via : 1.1 vegur";
    static boolean debug = false;
    static final String OK_STATUS_CODE = "HTTP/1.1 200 OK";
    static final String FILE_NOT_FOUND_STATUS_CODE = "HTTP/1.1 404 FILE NOT FOUND";
    static final String FILE_OVERWRITTEN_STATUS_CODE = "HTTP/1.1 201 FILE OVER-WRITTEN";
    static final String FILE_NOT_OVERWRITTEN_STATUS_CODE = "HTTP/1.1 201 FILE NOT OVER-WRITTEN";
    static final String NEW_FILE_CREATED_STATUS_CODE = "HTTP/1.1 202 NEW FILE CREATED";
    static final String CONNECTIONA_LIVE = "Connection: keep-alive";

    // static ServerSocket variable
    private static ServerSocket serverSocket;
    // socket server port on which it will listen
    private static int port = 8080;

    static ObjectOutputStream oos = null;
    static ObjectInputStream ois = null;

    private static ServerResponse serverResponse;
//
//    private static HttpClientRequest clientRequest;
    private static String clientReq;

    public static void main(String args[]) throws IOException, ClassNotFoundException, URISyntaxException {

        Scanner sc = new Scanner(System.in);
      ;
        List<String> reqList = new ArrayList<>();

        String directory = System.getProperty("user.div");
        System.out.println("Current Directory ===>> "+ directory);
        System.out.println("Enter Cpmmand >> ");
        String request = sc.nextLine();
        if (request.isEmpty() || request.length() == 0) {
            System.out.println("Invalid Command Please try again!!");
        }

        String reqArr[] = request.split(" ");
        reqList = Arrays.asList(reqArr);

        if (reqList.contains("-v")) {
            debug = true;
        }

        if (reqList.contains("-p")) {
            String portStr = reqList.get(reqList.indexOf("-p") + 1).trim();
            port = Integer.valueOf(portStr);
        }

        if (reqList.contains("-d")) {
            directory = reqList.get(reqList.indexOf("-d") + 1).trim();
            System.out.println("Current Directory ===>> "+ directory);
        }

        serverSocket = new ServerSocket(port);
        if (debug)
            System.out.println("Temp.Server is up and it assign to port Number: " + port);

//        File currentFolder = new File(directory);

        while(true)
        {
            serverResponse = new ServerResponse();
            Socket socket = serverSocket.accept();
            if (debug)
                System.out.println("Temp.Server is Connected to client ======>>>");

            ois = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());

            clientReq = (String) ois.readObject();
            System.out.println("Message Received: " + clientReq);
            String[] clientReqArr = clientReq.split(" ");
            List<String> requestList = Arrays.asList(clientReqArr);
            String responseHeaders = getResponseHeaders(OK_STATUS_CODE);
            int urlIdx = 1;
            if (requestList.contains("-o")) {
                urlIdx = 3;
            }
            String targetURL = clientReqArr[clientReqArr.length - urlIdx];

            if (targetURL.contains("\'")) {
                targetURL = targetURL.replace("\'", "");
            }

            if(clientReq.startsWith("httpc"))
            {

                URL url = new URL(targetURL);
                String host = url.getHost();
                String httpMethod = requestList.get(1).toUpperCase();
                String query = url.getQuery();
                System.out.println("clientType ==> " + "httpc");

                if (debug)
                    System.out.println(" Temp.Server is Processing the httpc request");

                String[] paramArr = {};
                if (query != null && !query.isEmpty()) {

                    paramArr = query.split("&");
                }
                String inlineData = "";
                String fileData = "";

                if (requestList.contains("-v")) {
                    serverResponse.setResponseHeaders(responseHeaders);
                }

                String body = "{\n";
                body += "\t\"args\":";
                body += "{";
                // for query parameters from client
                if (paramArr.length > 0) {
                    for (int i = 0; i < paramArr.length; i++) {
                        body += "\n\t    \"" + paramArr[i].substring(0, paramArr[i].indexOf("=")) + "\": \""
                                + paramArr[i].substring(paramArr[i].indexOf("=") + 1) + "\"";
                        if (i != paramArr.length - 1) {
                            body += ",";
                        } else {
                            body += "\n";
                            body += "\t},\n";
                        }
                    }
                } else {
                    body += "},\n";
                }

                if (httpMethod.equalsIgnoreCase("POST")) {
                    body = body + "\t\"data\": ";
                    if (requestList.contains("-d")) {
                        inlineData = requestList.get(requestList.indexOf("-d") + 1);
                        body = body + "\"" + inlineData + "\",\n";
                    } else if (requestList.contains("-f")) {
                        File file = new File(requestList.get(requestList.indexOf("-f") + 1));
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String st;
                        while ((st = br.readLine()) != null) {
                            fileData+= st;
                        }
                        body = body + "\"" + fileData + "\",\n";
                    } else {
                        body = body + "\"\",\n";
                    }
                    body += "\t\"files\": {},\n";
                    body += "\t\"form\": {},\n";
                }

                body = body + "\t\"headers\": {";

                if (requestList.contains("-h")) {

                    if (!requestList.contains("-d") && !requestList.contains("-f")) {
                        int noOfHeaders = requestList.size() - 1 - requestList.indexOf("-h") - 1;
                        for (int i = 1; i <= noOfHeaders; i++) {
                            String[] headerArr = requestList.get(requestList.indexOf("-h") + i).split(":");
                            if (headerArr[0].equalsIgnoreCase("connection"))
                                continue;
                            body += "\n\t\t\"" + headerArr[0] + "\": \"" + headerArr[1].trim() + "\",";
                        }
                    }
                }
                if (requestList.contains("-d")) {
                    body = body + "\n\t\t\"Content-Length\": \"" + inlineData.length() + "\",";
                } else if (requestList.contains("-f")) {
                    body = body + "\n\t\t\"Content-Length\": \"" + fileData.length() + "\",";
                }
                body += "\n\t\t\"Connection\": \"close\",\n";
                body += body + "\t\t\"Host\": \"" + host + "\"\n";
                body += body + "\t},\n";

                if (httpMethod.equalsIgnoreCase("POST")) {
                    body = body + "\t\"json\": ";
                    if (requestList.contains("-d")) {
                        body = body + "{\n\t\t " + inlineData.substring(1, inlineData.length() - 1) + "\n\t},\n";
                    } else if(requestList.contains("-f")) {
                        body = body + "{\n\t\t " + fileData + "\n\t},\n";
                    }else
                    {
                        body += "{},\n";
                    }
                }
                body = body + "\t\"origin\": \"" + InetAddress.getLocalHost().getHostAddress() + "\",\n";
                body = body + "\t\"url\": \"" + url + "\"\n";
                body = body + "}";

                if (debug)
                    System.out.println("Sending the response to Client ======>");

                // write object to Socket
                oos.writeObject(serverResponse);
                // close resources
                ois.close();
                oos.close();
                socket.close();

            }

        }

    }

    public static String getResponseHeaders(String status) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String datetime = dateFormat.format(date);
        String responseHeaders = status + "\n" + CONNECTIONA_LIVE + "\n" + Server.SERVER + "\n" + Server.DATE + datetime
                + "\n" + ACCESS_CONTROL_ALLOW_ORIGIN + "\n" + ACCESS_CONTROL_ALLOW_CREDENTIALS + "\n" + VIA + "\n";
        return responseHeaders;
    }

}
