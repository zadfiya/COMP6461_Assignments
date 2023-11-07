import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


public class Httpc {

	private static final String HTTPC = "httpc";

	private static HttpClientRequest request = new HttpClientRequest();
	private static List<String> headerLst = null;
	private static StringBuilder fileData = null;

	// InetAddress host = InetAddress.getLocalHost();
	static Socket socket = null;
	static ObjectOutputStream oos = null;
	static ObjectInputStream ois = null;
	static HttpClientResponse serverResponse;


	public static void main(String[] args) throws Exception {

		int count = 0;

		while (true) {

			try {

				headerLst = new ArrayList<String>();
				fileData = new StringBuilder();

				// checking for redirection
				if (request.isRedirect() && count <= 3) {
					count++;

					request.setHttpRequest(
							HTTPC + " " + request.getRequestMethod() + " -v " + request.getRedirectLocation());
					request.setRedirect(false);

				} else {
					count = 0;
					request = new HttpClientRequest();
					System.out.print("Please Enter httpc command ==> ");
					Scanner scanner = new Scanner(System.in);
					request.setHttpRequest(scanner.nextLine());

					if (request.getHttpRequest() == null || request.getHttpRequest().isEmpty()) {
						System.out.println("Invalid URL please try again");
						continue;
					}
				}

				String[] dataArray = request.getHttpRequest().split(" ");
				List<String> dataList = Arrays.asList(dataArray);

				if (dataList.contains("help")) {

					if (dataList.contains("post")) {
						System.out.println(
								"usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL\\nPost executes a HTTP ");
					} else if (dataList.contains("get")) {
						System.out
								.println("usage: httpc get [-v] [-h key:value] URL\\nGet executes a HTTP GET request ");
					} else {
						System.out.println("httpc is a curl-like application but supports HTTP protocol only.\n");
					}
				}

				// validation start
				if (dataList.get(0).contains("httpc")
						&& (dataList.get(1).contains("get") || dataList.get(1).contains("post"))) {
					request.setClientType("httpc");

					if (dataList.get(1).contains("get")
							&& (dataList.contains("--d") || dataList.contains("-d") || dataList.contains("-f"))) {
						System.out.println("-f or -d are not allowed in GET Request");
						continue;
					}

					if (dataList.get(1).contains("post")
							&& ((dataList.contains("--d") || dataList.contains("-d")) && dataList.contains("-f"))) {
						System.out.println(
								"your command is not Valid ==> -f and -d both are not allowed in POST request");
						continue;
					}

					parseInputRequest(dataList);

					URI uri = new URI(request.getRequestUrl());
					String hostName = uri.getHost();

					// establish socket connection to server
					socket = new Socket(hostName, uri.getPort() !=-1 ?uri.getPort():80 );
					// write to socket using ObjectOutputStream
					oos = new ObjectOutputStream(socket.getOutputStream());
					System.out.println("Sending request to Socket Server");
					oos.writeObject(request);

					// read the server response message
					ois = new ObjectInputStream(socket.getInputStream());
					serverResponse = (HttpClientResponse) ois.readObject();

					if (request.isFileWrite()) {

						// Method call for write response in file
						writetoFile(serverResponse);

					} else {

						// Method call for printing response in console
						printresult(serverResponse);

					}

//					clientSocket.close();

					// close resources
					ois.close();
					oos.close();

				} else {

					System.out.println("Invalid URL please. Provide valid httpc get or httpc post URL");
				}

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Invalid URL please. Provide valid httpc get or httpc post URL");
				continue;
			}

		}

	}


	private static void writetoFile(HttpClientResponse serverResponse) throws IOException {

		System.out.println("=============================================================================>>");

		FileWriter fileWriter = new FileWriter(request.getFileWritePath(), true);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		PrintWriter printWriter = new PrintWriter(bufferedWriter);

		if (request.isVerbosePreset()) {
			printWriter.println("=============================================================================>>");
			printWriter.println(serverResponse.getResponseHeaders());
			printWriter.println(serverResponse.getBody());

		} else {
			printWriter.println("=============================================================================>>");
			printWriter.println(serverResponse.getBody());
		}
		System.out
				.println("Response has been successfully written in ==> " + request.getFileWritePath() + "  File path");

		printWriter.flush();
		printWriter.close();

	}


	private static void printresult(HttpClientResponse serverResponsep) throws IOException {

		System.out.println("=============================================================================>>");

		if (request.isVerbosePreset()) {

			System.out.println(serverResponse.getResponseHeaders());
			System.out.println(serverResponse.getBody());
		} else {

			System.out.println(serverResponse.getBody());

		}

	}


	private static void parseInputRequest(List<String> dataList)
			throws URISyntaxException, UnknownHostException, IOException {

		// Collecting user request elements
		for (int i = 0; i < dataList.size(); i++) {

			if (dataList.get(i).equals("-v")) {
				request.setVerbosePreset(true);

			} else if (dataList.get(i).startsWith("http://") || dataList.get(i).startsWith("https://")) {
				request.setRequestUrl(dataList.get(i));

			} else if (dataList.get(i).equals("-h")) {

				headerLst.add(dataList.get(i + 1));

				request.setHttpHeader(true);
				request.setHeaderLst(headerLst);

			} else if (dataList.get(i).equals("-d") || dataList.get(i).equals("--d")) {

				request.setInlineData(true);
				request.setInlineData(dataList.get(i + 1));

			} else if (dataList.get(i).equals("-f")) {

				request.setFilesend(true);
				request.setFileSendPath(dataList.get(i + 1));

			} else if (dataList.get(i).equals("-o")) {

				request.setFileWrite(true);
				request.setFileWritePath(dataList.get(i + 1));

			}
		}

		request.setRequestMethod(dataList.get(1));

		// for -d inline data
		if (request.isInlineData()) {
			if (request.getInlineData().contains("\'")) {

				request.setInlineData(request.getInlineData().replace("\'", ""));

			}

			// -f for sending file data
		} else if (request.isFilesend()) {

			File filetoSend = new File(request.getFileSendPath());
			BufferedReader bufferedReader = new BufferedReader(new FileReader(filetoSend));
			String string;
			while ((string = bufferedReader.readLine()) != null) {
				fileData.append(string);
			}

			bufferedReader.close();
			request.setFileSendData(fileData.toString());

		}

	}
}