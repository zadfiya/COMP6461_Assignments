import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Server {

    static final String SERVER = "Server: httpfs/1.0.0";
    static final String DATE = "Date: ";
    static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin: *";
    static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials: true";
    static final String VIA = "Via : 1.1 vegur";
    static boolean debug = false;
    static final String OK_STATUS_CODE = "HTTP/1.1 200 OK";
    static final String FILE_NOT_FOUND_STATUS_CODE = "HTTP/1.1 404 FILE NOT FOUND";
    static final String FILE_OVERWRITTEN_STATUS_CODE = "HTTP/1.1 201 FILE OVER-WRITTEN";
    static String dir = System.getProperty("user.dir");
    static final String FILE_NOT_OVERWRITTEN_STATUS_CODE = "HTTP/1.1 201 FILE NOT OVER-WRITTEN";
    static final String NEW_FILE_CREATED_STATUS_CODE = "HTTP/1.1 202 NEW FILE CREATED";
    static final String CONNECTIONA_LIVE = "Connection: keep-alive";

    static File currentDir;
    static int timeout = 4000;
    static int port = 8007;
    List<String> clientRequestList;
    static String[] arr = null;

    public static void main(String[] args) throws Exception {
        String request;
        List<String> serverRequestList = new ArrayList<>();

        System.out.print("Enter the Command: ");
        Scanner sc = new Scanner(System.in);
        request = sc.nextLine();
        if (request.isEmpty() || request.length() == 0) {
            System.out.println("Command not found");
        }
        String[] serverRequestArray = request.split(" ");
        serverRequestList = new ArrayList<>();
        for (int i = 0; i < serverRequestArray.length; i++) {
            serverRequestList.add(serverRequestArray[i]);
        }

        if (serverRequestList.contains("-v")) {
            debug = true;
        }

        if (serverRequestList.contains("-p")) {
            String portStr = serverRequestList.get(serverRequestList.indexOf("-p") + 1).trim();
            port = Integer.valueOf(portStr);
        }

        if (serverRequestList.contains("-d")) {
            dir = serverRequestList.get(serverRequestList.indexOf("-d") + 1).trim();
        }

        if (debug)
            System.out.println("Server is up to the: " + port+ "Port number");

        currentDir = new File(dir);

        Server server = new Server();
        Runnable task = () -> {
            try {
                server.serveRequestToServer(port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }

    private void serveRequestToServer(int port) throws Exception {
        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.bind(new InetSocketAddress(port));

            ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);

            for (;;) {
                buf.clear();
                SocketAddress router = channel.receive(buf);
                if (router != null) {
                    buf.flip();
                    Packet packet = Packet.fromBuffer(buf);
                    buf.flip();

                    String requestPayload = new String(packet.getPayload(), UTF_8);
                    if (requestPayload.equals("Please Confirm I am Client")) {

                        System.out.println(requestPayload);
                        Packet resp = packet.toBuilder().setPayload("Hi from Server".getBytes()).create();
                        channel.send(resp.toBuffer(), router);
                        System.out.println("Hiiii, Server this side");
                    } else if (requestPayload.contains("httpfs") || requestPayload.contains("httpc")) {

                        String responsePayload = processPayload(requestPayload,arr);
                        Packet resp = packet.toBuilder().setPayload(responsePayload.getBytes()).create();
                        channel.send(resp.toBuffer(), router);
                    } else if (requestPayload.equals("Got the Message")) {
                        arr = requestPayload.split(" ");
                        System.out.println(requestPayload);
                        Packet respClose = packet.toBuilder().setPayload("Close".getBytes()).create();
                        channel.send(respClose.toBuffer(), router);

                    } else if (requestPayload.equals("Ok")) {
                        arr = requestPayload.split(" ");
                        System.out.println(requestPayload + " Got the Message");

                    }
                }
            }
        }

    }
    private String processPayload(String requestPayload, String[] arr) throws Exception {
        String body = "";
        String method ="";
        String responsePayload = "";
        //String temp = arr[1];
        int cnt=0;
        String[] clientRequestArray = requestPayload.split(" ");
        clientRequestList = new ArrayList<>();

        for (int i = 0; i < clientRequestArray.length; i++) {
            clientRequestList.add(clientRequestArray[i]);

            if (clientRequestArray[i].startsWith("http://")) {
                String[] methodarray = clientRequestArray[i].split("/");
                if (methodarray.length == 4) {
                    method = methodarray[3] + "/";
                } else if (methodarray.length == 5) {
                    method = methodarray[3] + "/" + methodarray[4];
                }
            }
        }

        if(requestPayload.contains("get")&&requestPayload.contains("txt")){
            //System.out.println("HELO"+arr[arr.length-1]);
            File myObj = new File("test.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                body = body+myReader.nextLine();

            }
            responsePayload = body;
        }
        else if(requestPayload.contains("post") && requestPayload.contains("txt") &&requestPayload.contains("json")&& requestPayload.contains("-d")){
            FileWriter fWriter = new FileWriter("test.txt");
            fWriter.write("**************************");
            fWriter.write(arr[arr.length-1]);
            fWriter.write(body);
            fWriter.write("\n");
            responsePayload = "Data Written succesfully";
        }

        else{
            String url;
            String fileData = "";
            String downloadFileName = "";

            if (requestPayload.contains("post")) {
                url = clientRequestList.get(1);
            } else {
                url = clientRequestList.get(clientRequestList.size() - 1);
            }
            String host = new URL(url).getHost();
            String responseHeaders = getResponseHeaders(OK_STATUS_CODE);


            body = body + "\t\"args\":";
            body = body + "{";

            body = body + "\n\t\t\"Content-Disposition\": \"Ass3\",";
            body = body + "\n\t    \"" + "Connection" + "\": \""
                    + "Close ," + "\"";
            body = body + "\n\t    \"" + "Host" + "\": \""
                    + "localhost ," + "\"";
            body = body + "\t},\n";
            body = body + "\"" + "File-Data" + "\",\n";
            body = body + "\t\"files\": {},\n";
            body = body + "\t\"form\": {},\n";

            body = body + "\n\t\t\"Connection\": \"close\",\n";
            body = body + "\t\t\"Host\": \"" + host + "\"\n";
            body = body + "\t},\n";
            body = body + "\t\"origin\": \"" + InetAddress.getLocalHost().getHostAddress() + "\",\n";
            body = body + "\t\"url\": \"" + url + "\"\n";
            body = body + "}";

            responsePayload = responseHeaders + body;

        }

        return responsePayload;
    }

    static private List<String> getFilesFromDir(File currentDir) {
        List<String> filelist = new ArrayList<>();
        for (File file : currentDir.listFiles()) {
            if (!file.isDirectory()) {
                filelist.add(file.getName());
            }
        }
        return filelist;
    }
    static String getResponseHeaders(String status) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String datetime = dateFormat.format(date);
        String responseHeaders = status + "\n" + CONNECTIONA_LIVE + "\n" + Server.SERVER + "\n" + Server.DATE + datetime
                + "\n" + ACCESS_CONTROL_ALLOW_ORIGIN + "\n" + ACCESS_CONTROL_ALLOW_CREDENTIALS + "\n" + VIA + "\n";
        return responseHeaders;
    }
}