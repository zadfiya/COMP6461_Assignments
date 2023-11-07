import java.io.*;
import java.net.*;
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
	static final String FILE_NOT_OVERWRITTEN_STATUS_CODE = "HTTP/1.1 201 FILE NOT OVER-WRITTEN";
	static final String NEW_FILE_CREATED_STATUS_CODE = "HTTP/1.1 202 NEW FILE CREATED";
	static final String CONNECTIONA_LIVE = "Connection: keep-alive";

	private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	// static ServerSocket variable
	private static ServerSocket serverSocket;
	// socket server port on which it will listen
	private static int port = 8080;

	static ObjectOutputStream oos = null;
	static ObjectInputStream ois = null;

	private static HttpClientResponse serverResponse;

	private static HttpClientRequest clientRequest;

	public static void main(String[] args) throws IOException, ClassNotFoundException, URISyntaxException {

		String request;
		List<String> requestList = new ArrayList<>();

		String dir = System.getProperty("user.dir");

		System.out.println("Dir ==>>>>> " + dir);

		System.out.print(">");
		Scanner sc = new Scanner(System.in);
		request = sc.nextLine();
		if (request.isEmpty() || request.length() == 0) {
			System.out.println("Invalid Command Please try again!!");
		}
		String[] requestArray = request.split(" ");
		requestList = new ArrayList<>();
		for (int i = 0; i < requestArray.length; i++) {
			requestList.add(requestArray[i]);
		}

		if (requestList.contains("-v")) {
			debug = true;
		}

		if (requestList.contains("-p")) {
			String portStr = requestList.get(requestList.indexOf("-p") + 1).trim();
			port = Integer.valueOf(portStr);
		}

		if (requestList.contains("-d")) {
			dir = requestList.get(requestList.indexOf("-d") + 1).trim();
			System.out.println("Dir ==>>>>> " + dir);
		}
		serverSocket = new ServerSocket(port);
		if (debug)
			System.out.println("Server is up and it assign to port Number: " + port);

		File currentFolder = new File(dir);

		while (true) {

			serverResponse = new HttpClientResponse();

			Socket socket = serverSocket.accept();
			if (debug)
				System.out.println("Server is Connected to client ======>>>");

			// read from socket to ObjectInputStream object
			ois = new
					ObjectInputStream(socket.getInputStream());
			oos = new ObjectOutputStream(socket.getOutputStream());
			// convert ObjectInputStream object to String
			clientRequest = (HttpClientRequest) ois.readObject();
//			System.out.println(clientRequest+ "client Request:::");
			// System.out.println("Message Received: " + clientRequest);

			String clientType = clientRequest.getClientType();

			String method = clientRequest.getRequestMethod();

			String responseHeaders = getResponseHeaders(OK_STATUS_CODE);

			if (clientType.equalsIgnoreCase("httpc")) {

				String url = clientRequest.getRequestUrl();
				URI uri = new URI(clientRequest.getRequestUrl());
				String host = uri.getHost();

				String query = uri.getQuery();

				System.out.println("clientType ==> " + clientType);

				if (debug)
					System.out.println(" Server is Processing the httpc request");

				String[] paramArr = {};
				if (query != null && !query.isEmpty()) {

					paramArr = query.split("&");
				}
				String inlineData = "";
				String fileData = "";

				if (clientRequest.isVerbosePreset()) {
					serverResponse.setResponseHeaders(responseHeaders);
				}
				String body = "{\n";
				body = body + "\t\"args\":";
				body = body + "{";
				// for query parameters from client
				if (paramArr.length > 0) {
					for (int i = 0; i < paramArr.length; i++) {
						body = body + "\n\t    \"" + paramArr[i].substring(0, paramArr[i].indexOf("=")) + "\": \""
								+ paramArr[i].substring(paramArr[i].indexOf("=") + 1) + "\"";
						if (i != paramArr.length - 1) {
							body = body + ",";
						} else {
							body = body + "\n";
							body = body + "\t},\n";
						}
					}
				} else {
					body = body + "},\n";
				}

				// if method type is POST then
				if (method.equalsIgnoreCase("POST")) {
					body = body + "\t\"data\": ";
					if (clientRequest.isInlineData()) {
						inlineData = clientRequest.getInlineData();
						body = body + "\"" + inlineData + "\",\n";
					} else if (clientRequest.isFilesend()) {
						fileData = clientRequest.getFileSendData();
						body = body + "\"" + fileData + "\",\n";
					} else {
						body = body + "\"\",\n";
					}
					body = body + "\t\"files\": {},\n";
					body = body + "\t\"form\": {},\n";
				}

				body = body + "\t\"headers\": {";

				// for headers only
				if (clientRequest.isHttpHeader()) {

					for (String header : clientRequest.getHeaderLst()) {
						String[] headerArr = header.split(":");
						if (headerArr[0].equalsIgnoreCase("connection"))
							continue;
						body = body + "\n\t\t\"" + headerArr[0] + "\": \"" + headerArr[1].trim() + "\",";

					}
				}
				if (clientRequest.isInlineData()) {
					body = body + "\n\t\t\"Content-Length\": \"" + clientRequest.getInlineData().length() + "\",";
				} else if (clientRequest.isFilesend()) {
					body = body + "\n\t\t\"Content-Length\": \"" + clientRequest.getFileSendData().length() + "\",";
				}
				body = body + "\n\t\t\"Connection\": \"close\",\n";
				body = body + "\t\t\"Host\": \"" + host + "\"\n";
				body = body + "\t},\n";

				if (method.equalsIgnoreCase("POST")) {
					body = body + "\t\"json\": ";
					if (clientRequest.isInlineData()) {
						body = body + "{\n\t\t " + inlineData.substring(1, inlineData.length() - 1) + "\n\t},\n";
					} else {
						body = body + "{\n\t\t " + fileData + "\n\t},\n";
					}
				}
				body = body + "\t\"origin\": \"" + InetAddress.getLocalHost().getHostAddress() + "\",\n";
				body = body + "\t\"url\": \"" + url + "\"\n";
				body = body + "}";

				serverResponse.setBody(body);
				
				if (debug)
					System.out.println("Sending the response to Client ======>");

				// write object to Socket
				oos.writeObject(serverResponse);
				// close resources
				ois.close();
				oos.close();
				socket.close();

			}
			else if (clientType.equalsIgnoreCase("httpfs")) {

				URI uri = new URI(clientRequest.getRequestUrl());
				String host = uri.getHost();

				String url = clientRequest.getHttpRequest();

				if (debug)
					System.out.println("Processing the httpfs request");
				String body = "{\n";
				body = body + "\t\"args\":";
				body = body + "{},\n";
				body = body + "\t\"headers\": {";

				if (!method.endsWith("/") && method.contains("get/")
						&& url.contains("Content-Disposition:attachment")) {
					body = body + "\n\t\t\"Content-Disposition\": \"attachment\",";
				} else if (!method.endsWith("/") && method.contains("get/")
						&& url.contains("Content-Disposition:inline")) {
					body = body + "\n\t\t\"Content-Disposition\": \"inline\",";
				}
				body +=  "\n\t\t\"Connection\": \"close\",\n";
				body = body + "\t\t\"Host\": \"" + host + "\"\n";
				body = body + "\t},\n";

				System.out.println("body 1: "+body);
				if (method.equalsIgnoreCase("get/")) {
					System.out.println("inside get 1");
					body = body + "\t\"files\": { ";
					List<String> files = getFilesFromDir(currentFolder);
					List<String> fileFilterList = new ArrayList<String>();
					fileFilterList.addAll(files);
					if (url.contains("Content-Type")) {
						String fileType = clientRequest.getHeaderLst().get(0).split(":")[1];
						fileFilterList = new ArrayList<String>();
						for (String file : files) {
							if (file.endsWith(fileType)) {
								fileFilterList.add(file);
							}
						}
					}
					for (int i = 0; i < fileFilterList.size(); i++) {

						if (i != fileFilterList.size() - 1) {
							body = body + fileFilterList.get(i) + " , ";
						} else {
							body = body + fileFilterList.get(i) + " },\n";
						}

					}

				}

				// if the request is get/fileName
				else if (!method.endsWith("/") && method.contains("get/")) {
					System.out.println("inside get 2");
					String response = "";
					String requestedFileName = method.split("/")[1];
					List<String> files = getFilesFromDir(currentFolder);

					if (!files.contains(requestedFileName)) {
						responseHeaders = getResponseHeaders(FILE_NOT_FOUND_STATUS_CODE);

					} else {

						File file = new File(dir + "/" + requestedFileName);
						response = readToFile(file);

						if (url.contains("Content-Disposition:attachment")) {
							serverResponse.setResponseCode("203");
							serverResponse.setBody(response);
							System.out.println("temp: response "+ response);
							file = new File(dir + "/attachment/" + requestedFileName);
							writeToFile(file, response);
							serverResponse.setRequestFileName(requestedFileName);

						} else {

							serverResponse.setResponseCode("203");
							body = body + "\t\"data\": \"" + response + "\",\n";
						}

					}

				}

				else if (!method.endsWith("/") && method.contains("post/")) {
					System.out.println("inside post 3:  ");
					String fileName = method.split("/")[1];
					File file = new File(fileName);
					List<String> files = getFilesFromDir(currentFolder);
					if (files.contains(fileName)) {

						if (url.contains("overwrite")) {
							String overwrite = clientRequest.getHeaderLst().get(0).split(":")[1];
							if (overwrite.equalsIgnoreCase("true")) {
								synchronized (file) {
									file.delete();
									file = new File(dir + "/" + fileName);
									writeToFile(file,clientRequest.getInlineData());
//									file.createNewFile();
//									FileWriter fw = new FileWriter(file);
//									fw.write(clientRequest.getInlineData());
//									fw.close();
								}
								responseHeaders = getResponseHeaders(FILE_OVERWRITTEN_STATUS_CODE);
							} else {
								responseHeaders = getResponseHeaders(FILE_NOT_OVERWRITTEN_STATUS_CODE);
							}
						} else {
							synchronized (file) {
								file.delete();
								file = new File(dir + "/" + fileName);
								writeToFile(file,clientRequest.getInlineData());
//								file.createNewFile();
//								FileWriter fw = new FileWriter(file);
//								fw.write(clientRequest.getInlineData());
//								fw.close();
							}
							responseHeaders = getResponseHeaders(FILE_OVERWRITTEN_STATUS_CODE);
						}

					} else {
						System.out.println("inside post 4:  ");
						file = new File(dir + "/" + fileName);
						synchronized (file) {
							file.createNewFile();
							FileWriter fw = new FileWriter(file);
							BufferedWriter bw = new BufferedWriter(fw);
							PrintWriter pw = new PrintWriter(bw);

							pw.write(clientRequest.getInlineData());
							pw.flush();
							pw.close();
						}
						responseHeaders = getResponseHeaders(NEW_FILE_CREATED_STATUS_CODE);

					}

				}

				body = body + "\t\"origin\": \"" + InetAddress.getLocalHost().getHostAddress() + "\",\n";
				body = body + "\t\"url\": \"" + url + "\"\n";
				body = body + "}";

				if (debug)
					System.out.println("Sending the response to Client ======>");

				// write object to Socket
				serverResponse.setResponseHeaders(responseHeaders);
				serverResponse.setBody(body);
				oos.writeObject(serverResponse);


			}

		}
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
			file.createNewFile();
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
