import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class Httpc implements Serializable{
    private static final long serialVersionUID = 2207162898379302282L;
    static ObjectOutputStream oos = null;
    static ObjectInputStream ois = null;
    static ServerResponse serverResponse;
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        boolean isRedirection=false;
        while(true) {
            String request;
            if (!isRedirection) {
                System.out.print("Enter Command: ");
                Scanner sc = new Scanner(System.in);
                request = sc.nextLine();

                if (request.isEmpty() || request.length() == 0 || !request.contains(Constant.HTTPC)) {
                    System.out.println(Constant.INVALID_COMMAND);
                    continue;
                }
            } else {
                System.out.println(Constant.REDIRECTION_SUCCESSFUL);
                request = Constant.REDIRECTION_REQUEST;
            }

            String[] requestArr = request.split(" ");
            ArrayList<String> requestList = new ArrayList<>();

            for (int i = 0; i < requestArr.length; i++) {
                requestList.add(requestArr[i]);
            }

            if (requestList.contains(Constant.HELP)) {
                if (requestList.contains(Constant.GET)) {
                    System.out.println(Constant.helpMenuPrint(Constant.GET));
                } else if (requestList.contains(Constant.POST)) {
                    System.out.println(Constant.helpMenuPrint(Constant.POST));
                } else {
                    System.out.println(Constant.helpMenuPrint(Constant.HELP));
                }
            } else {
                // -d and -f can not be used at same time
                if (requestList.contains("-d") && requestList.contains("-f")) {
                    System.out.println(Constant.InlineFileDataError);
                    continue;
                }

                int urlIdx = 1;
                if (requestList.contains("-o")) {
                    urlIdx = 3;
                }

                //URL of Web server
//                String targetURL = requestList.get(requestList.size() - urlIdx).substring(0, requestList.get(requestList.size() - urlIdx).lastIndexOf('/') + 1);
                String targetURL = requestList.get(requestList.size() - urlIdx);

                if (targetURL.contains("\'")) {
                    targetURL = targetURL.replace("\'", "");
                }
                URL url = new URL(targetURL);
                String hostName = url.getHost();

                //Socket
                Socket client = new Socket(hostName, 8080);
                OutputStream outStream = client.getOutputStream();
                oos = new ObjectOutputStream(client.getOutputStream());
                System.out.println("Sending request to Socket Temp.Server");
                oos.writeObject(request);

                ois = new ObjectInputStream(client.getInputStream());
                serverResponse = (ServerResponse) ois.readObject();
                printResult(serverResponse);

                // getting request method like get, post etc.
                String httpMethod = requestList.get(1).toUpperCase();

                //getting parameters(text after host)
//                String queryParams = requestList.get(requestList.size() - urlIdx).substring(requestList.get(requestList.size() - urlIdx).lastIndexOf('/'));
                    String queryParams = url.getPath();
                    if(url.getQuery()!=null)
                    {
                        queryParams+= '?' + url.getQuery();
                    }


                if (queryParams.contains("\'")) {
                    queryParams = queryParams.replace("\'", "");
                }

                PrintWriter printWriter = new PrintWriter(outStream);

                // Preparing request by adding method and parameters
                printWriter.print(httpMethod + " " + queryParams + " HTTP/1.1\r\n");

                // Adding host to request
                printWriter.print("Host: " + hostName + "\r\n");

                String inlineData = new String();
                StringBuffer fileData = new StringBuffer();

                if (requestList.contains("-d")) {
                    inlineData = requestList.get(requestList.indexOf("-d") + 1);
                    if (inlineData.contains("\'")) {
                        inlineData = inlineData.replace("\'", "");
                    }
                    printWriter.print("Content-Length: " + inlineData.length() + "\r\n");
                }
                //for the file data
                else if (requestList.contains("-f")) {
                    File file = new File(requestList.get(requestList.indexOf("-f") + 1));
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String st;
                    while ((st = br.readLine()) != null) {
                        fileData.append(st);
                    }
                    printWriter.println("Content-Length: " + fileData.length() + "\r\n");
                }
                if (requestList.contains("-h")) {
                    if (!requestList.contains("-d") && !requestList.contains("-f")) {
                        int noOfHeaders = requestList.size() - 1 - requestList.indexOf("-h") - 1;
                        for (int i = 1; i <= noOfHeaders; i++) {
                            printWriter.print(requestList.get(requestList.indexOf("-h") + i) + "\r\n");
                        }
                    } else if (requestList.contains("-d") || requestList.contains("-f")) {
                        int noOfHeaders = 0;
                        if (requestList.contains("-d")) {
                            noOfHeaders = requestList.indexOf("-d") - requestList.indexOf("-h") - 1;
                        } else if (requestList.contains("-f")) {
                            noOfHeaders = requestList.indexOf("-f") - requestList.indexOf("-h") - 1;
                        }
                        for (int i = 1; i <= noOfHeaders; i++) {
                            printWriter.print(requestList.get(requestList.indexOf("-h") + i) + "\r\n");
                        }
                    }
                }
                // Code for adding in-line data and file data to the request
                if (requestList.contains("-d")) {
                    printWriter.print("\r\n");
                    printWriter.print(inlineData);
                } else if (requestList.contains("-f")) {
                    printWriter.print(fileData);
                    printWriter.print("\r\n");
                } else {
                    printWriter.print("\r\n");
                }
                printWriter.flush();


                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String temp;
                // if request contains 'verbose'(-v) command

                String statusResponse = reader.readLine();

                String[] statusArr = statusResponse.split(" ");
                if (statusArr[1].contains("3")) {
                    // if redirect code in status
                    System.out.println(statusResponse);
                    isRedirection = true;
                    Constant.setRedirectionRequest("");
                    if(requestList.contains("-o")) {
                        String filePath = requestList.get(requestList.size() - 1);
                        Constant.setRedirectionRequest(" -o " + filePath);
                        FileWriter file = new FileWriter(filePath, true);
                        BufferedWriter writer = new BufferedWriter(file);
                        PrintWriter pWriter = new PrintWriter(writer);
                        pWriter.println(Constant.NEW_LINE);
                        pWriter.flush();
                        pWriter.close();
                    }

                    String t;
                    while ((t = reader.readLine()) != null) {
                        if (t.startsWith("Location:")) {

                            System.out.println("redirectLocation ==> " + t.split(" ")[1]+ Constant.NEW_LINE);
                            break;
                        }

                    }
                    continue;
                }
                isRedirection = false;

                if (requestList.contains("-o")) {
                    String filePath = requestList.get(requestList.size() - 1);

                    FileWriter file = new FileWriter(filePath, true);
                    BufferedWriter writer = new BufferedWriter(file);
                    PrintWriter pWriter = new PrintWriter(writer);

                    if (requestList.contains("-v")) {
                        pWriter.println(statusResponse);
                        while ((temp = reader.readLine()) != null) {
                            pWriter.println(temp);
                            if (temp.equals("}"))
                                break;
                        }
                    }
                    // if request does not contain 'verbose'(-v) command
                    else {
                        int flag = 0;
                        while ((temp = reader.readLine()) != null) {
                            if (temp.trim().equals("{")) flag = 1;
                            if (flag == 1) {

                                pWriter.println(temp);
                                if (temp.equals("}"))
                                    break;
                            }
                        }
                    }
                    pWriter.flush();
                    pWriter.close();
                }

                else {
                    if (requestList.contains("-v")) {
                        System.out.println(statusResponse);
                        while ((temp = reader.readLine()) != null) {
                            System.out.println(temp);
                            if (temp.equals("}"))
                                break;
                        }
                    }
                    // if request does not contain 'verbose'(-v) command
                    else {
                        int flag = 0;

                        while ((temp = reader.readLine()) != null) {
                            if (temp.trim().equals("{")) flag = 1;
                            if (flag == 1) {
                                System.out.println(temp);
                                if (temp.equals("}"))
                                    break;
                            }
                        }
                    }

                }
                reader.close();
                client.close();
            }
        }
    }

    private static void printResult(ServerResponse serverResponse) {
        System.out.println("=============================================================================>>");

//        if (request.isVerbosePreset()) {
//
//            System.out.println(serverResponse.getResponseHeaders());
//            System.out.println(serverResponse.getBody());
//        } else {

            System.out.println(serverResponse.getBody());
        System.out.println("=============================================================================>>");

//        }
    }
}
