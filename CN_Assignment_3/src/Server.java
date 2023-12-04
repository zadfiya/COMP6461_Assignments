import com.sun.net.httpserver.Headers;

import javax.sound.midi.SysexMessage;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

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
                    // Parse a packet from the received raw data.
                    buf.flip();

                    Packet packet = Packet.fromBuffer(buf);
                    buf.flip();

                    String requestPayload = new String(packet.getPayload(), UTF_8);
                    if (requestPayload.equals("Connect")) {

                        System.out.println(requestPayload);
                        Packet resp = packet.toBuilder().setPayload("ACK".getBytes()).create();
                        channel.send(resp.toBuffer(), router);
                        System.out.println("Sending ACK from Server");
                    } else if (requestPayload.contains("httpfs") || requestPayload.contains("httpc")) {

                        String responsePayload = processRequest(requestPayload);
                        Packet resp = packet.toBuilder().setPayload(responsePayload.getBytes()).create();
                        channel.send(resp.toBuffer(), router);
                    } else if (requestPayload.equals("Received")) {
                        arr = requestPayload.split(" ");
                        System.out.println(requestPayload);
                        Packet respClose = packet.toBuilder().setPayload("Close".getBytes()).create();
                        channel.send(respClose.toBuffer(), router);

                    } else if (requestPayload.equals("Ok")) {
                        arr = requestPayload.split(" ");
                        System.out.println(requestPayload + " received..!");

                    }
                }
            }
        }

    }
    private String processRequest(String requestPayload) throws Exception {
        String body = "{\n";
        String method ="";
        String responsePayload = "";
        String splitArray[];
        String clientRequestURL ="";
        List<String> files = getFilesFromDir(new File(dir));
        //String temp = arr[1];
        int cnt=0;
        String[] clientRequestArray = requestPayload.split(" ");
        clientRequestList = new ArrayList<>();
        List<String> headerList = new ArrayList<>();
        for (int i = 0; i < clientRequestArray.length; i++) {
            clientRequestList.add(clientRequestArray[i]);

            if (clientRequestArray[i].startsWith("http://")) {
                String[] methodarray = clientRequestArray[i].split("/");
                clientRequestURL = clientRequestArray[i];
                if (methodarray.length == 4) {
                    method = methodarray[3] + "/";
                } else if (methodarray.length == 5) {
                    method = methodarray[3] + "/" + methodarray[4];
                }
            }

            if(clientRequestArray[i].equals("-h"))
            {
                headerList.add(clientRequestArray[i+1]);
            }
        }
        String args="\t\"args\":";
        URI uri = new URI(clientRequestURL);
        String host = uri.getHost();

        String query = uri.getQuery();
        String[] paramArr = {};
        if (query != null && !query.isEmpty()) {

            paramArr = query.split("&");
        }

        args += "{";
        if (paramArr.length > 0) {
            for (int i = 0; i < paramArr.length; i++) {
                args += "\n\t    \"" + paramArr[i].substring(0, paramArr[i].indexOf("=")) + "\": \""
                        + paramArr[i].substring(paramArr[i].indexOf("=") + 1) + "\"";
                if (i != paramArr.length - 1) {
                    args += ",";
                } else {
                    args += "\n";
                    args += "\t},\n";
                }
            }
        } else {
            args += "},\n";
        }

        String headers= "\t\"headers\": {";
        if (headerList.size()>0) {

            for (String header : headerList) {
                String[] headerArr = header.split(":");
                if (headerArr[0].equalsIgnoreCase("connection"))
                    continue;
                headers += "\n\t\t\"" + headerArr[0] + "\": \"" + headerArr[1].trim() + "\",";

            }
        }

        headers += "\n\t\t\"Connection\": \"close\",\n";
        headers += "\t\t\"Host\": \"" + host + "\"\n";
        headers += "\t},\n";
        String dataBody ="\t\"data\": \"";
        String jsonBody = "\t\"json\": {";
        String fileBody =  "\t\"files\": {";
        String formBody = "\t\"form\": {";

        if (debug)
            System.out.println("Server is Processing the httpfs request");

        body += args;
        if(method.contains("get/")&&(!method.endsWith("/"))){

            splitArray = requestPayload.split(" ");
            String fileName = splitArray[splitArray.length-1].split("/")[4];
            File fileToread = new File(fileName);

            String responseHeader;
            body +=headers;


            if(files.contains(fileName))
            {
                synchronized (fileToread)
                {
                    dataBody += readToFile(fileToread);
                }

              responseHeader = getResponseHeaders(OK_STATUS_CODE);
            }
            else
            {

                responseHeader = getResponseHeaders(FILE_NOT_FOUND_STATUS_CODE);
            }
            dataBody += "\",\n";
            body+=dataBody;


            responsePayload = responseHeader ;
        }
        else if(requestPayload.contains("post") && (requestPayload.contains("txt") || requestPayload.contains("json")|| requestPayload.contains("-d"))){
            splitArray = requestPayload.split(" ");
            String fileName = splitArray[splitArray.length-3].split("/")[4];
            String responseHeader;
            File fileToWrite = new File(dir + "/"+ fileName);

            boolean isExist = files.contains(fileName);

            String dataToWrite = "";
            if(requestPayload.contains("-h") && requestPayload.contains("overwrite:false") && isExist){

                responseHeader = getResponseHeaders(FILE_NOT_OVERWRITTEN_STATUS_CODE);
            }
            else if(isExist)
            {
                synchronized (fileToWrite)
                {
                    dataToWrite+= readToFile(fileToWrite);
                }


                responseHeader = getResponseHeaders(FILE_OVERWRITTEN_STATUS_CODE);
            }
            else
            {
                fileToWrite.createNewFile();
                responseHeader = getResponseHeaders(NEW_FILE_CREATED_STATUS_CODE);
            }
            synchronized (fileToWrite)
            {
                dataToWrite += splitArray[splitArray.length-1].replaceAll("\'","");
                writeToFile(fileToWrite,dataToWrite);
            }



            dataBody +=   splitArray[splitArray.length-1].replaceAll("\'","") +"\n";
            dataBody += "\",\n";
            body+=dataBody;
            fileBody +="},\n";
            formBody +="},\n";


            if(fileName.endsWith(".json"))
            {
                jsonBody+= "\"" + splitArray[splitArray.length-1] + "\",\n";
            }
            jsonBody += "\n\t},\n";
            body+=fileBody;
            body+=formBody;
            body += headers;
            body+=jsonBody;

            responsePayload =responseHeader ;
        }

        else{
            String url;

            if (requestPayload.contains("post")) {
                url = clientRequestList.get(clientRequestList.size() - 3);
            } else {
                url = clientRequestList.get(clientRequestList.size() - 1);
            }
             host = new URL(url).getHost();
            String responseHeaders = getResponseHeaders(OK_STATUS_CODE);
            jsonBody += "\n\t},\n";
            dataBody += "\",\n";

            if(method.contains("get"))
            {
                List<String> fileFilterList = new ArrayList<String>();
                if (requestPayload.contains("Content-Type")) {
                    String fileType = clientRequestList.get(clientRequestList.indexOf("-h") + 1).split(":")[1];
                    fileFilterList = new ArrayList<String>();
                    for (String file : files) {
                        if (file.endsWith(fileType)) {
                            fileFilterList.add(file);
                        }
                    }
                }
                else
                    fileFilterList.addAll(files);

                if (!fileFilterList.isEmpty()) {
                    for (int i = 0; i < fileFilterList.size(); i++) {
                        if (i != fileFilterList.size() - 1) {
                            fileBody +=  fileFilterList.get(i) + ", ";
                        } else {
                            fileBody += fileFilterList.get(i)  ;
                        }
                    }
                }
            }


            fileBody +="},\n";

            formBody +="},\n";
            body+= dataBody;
            body+= fileBody;
            body+=formBody;

            body+=headers;

            body+=jsonBody;


            responsePayload = responseHeaders ;

        }
        body = body + "\t\"url\": \"" + clientRequestURL + "\"\n";
        body = body + "}";
        responsePayload += "\n"+body+"\n";

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

    static private String readToFile(File file) throws IOException {
        lock.readLock().lock();
        String st, response="";
        try
        {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            while ((st = bufferedReader.readLine()) != null) {
                response = response + st;
            }
        }catch(IOException e)
        {
            throw new RuntimeException(e);
        }
        finally {
            lock.readLock().unlock();
        }

        return response;
    }

    static private void writeToFile(File file, String data) throws IOException {
        lock.writeLock().lock();

        try {
            FileWriter fw = null;

            fw = new FileWriter(file);
            fw.write(data);
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            lock.writeLock().unlock();
        }

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