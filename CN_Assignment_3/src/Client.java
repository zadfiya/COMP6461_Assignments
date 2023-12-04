import static java.nio.channels.SelectionKey.OP_READ;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Client {

    static long SEQ_NO = 0;
    static List<Long> receivedPackets = new ArrayList<>();
    static int TIME_OUT = 3000;
    static int ACK_COUNT = 0;
    static String routerHost = "127.0.0.1";
    static int ROUTER_PORT = 3000;

    public static void main(String[] args) throws Exception {

        File file = new File("attachment");
        file.mkdir();
        while (true) {
            String url = "";
            String url1 = "";
            System.out.print("Enter httpfs command: ");
            receivedPackets.clear();
            SEQ_NO = 0;
            ACK_COUNT = 0;
            Scanner sc = new Scanner(System.in);
            url = sc.nextLine();

            if (url.isEmpty() || url.length() == 0) {
                System.out.println("Inavalid Command...");
                continue;
            }

            String[] arr = url.split(" ");
            ArrayList<String>  urlList = new ArrayList<>();
            for (int i = 0; i < arr.length; ++i) {
                if(arr[i].startsWith("http://")) {
                    url1 = arr[i];
                }
                urlList.add(arr[i]);
            }

            String hostName = new URL(url1).getHost();
            int hostPort = new URL(url1).getPort();
            SocketAddress routerAddress = new InetSocketAddress(routerHost, ROUTER_PORT);
            InetSocketAddress serverAddress = new InetSocketAddress(hostName, hostPort);
            connect(routerAddress, serverAddress);
            runClient(routerAddress, serverAddress, url);
        }
    }

    private static void connect(SocketAddress routerAddress, InetSocketAddress serverAddress) throws Exception {

        try (DatagramChannel dataChannel = DatagramChannel.open()) {

            String msg = "Connect";
            SEQ_NO++;
            Packet pkt = (new Packet.Builder())
                    .setType(0).setSequenceNumber(SEQ_NO)
                    .setPortNumber(serverAddress.getPort())
                    .setPeerAddress(serverAddress.getAddress())
                    .setPayload(msg.getBytes()).create();
            dataChannel.send(pkt.toBuffer(), routerAddress);
            System.out.println("Sending Connect from Client");

            dataChannel.configureBlocking(false);
            Selector selector = Selector.open();
            dataChannel.register(selector, OP_READ);

            selector.select(TIME_OUT);

            Set<SelectionKey> set = selector.selectedKeys();
            if (set.isEmpty()) {

                System.out.println("Timeout...Sending again!!");
                resend(dataChannel, pkt, routerAddress);
            }
            ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
            dataChannel.receive(buf); //temp: receiving lines
            buf.flip(); //temp: buffer flipping
            Packet respPkt = Packet.fromBuffer(buf);
            String payload = new String(respPkt.getPayload(), StandardCharsets.UTF_8);
            System.out.println(payload.trim() + " received from server");
            receivedPackets.add(respPkt.getSequenceNumber());
            set.clear();
        }
    }
    private static void resend(DatagramChannel dataChannel, Packet pkt, SocketAddress routerAddress) throws IOException {
        dataChannel.send(pkt.toBuffer(), routerAddress);

        String payload = new String(pkt.getPayload());
        PrintStream print = System.out;
        print.println(payload);
        print.println();
        if (payload.equals("Received")) {
            ACK_COUNT++;
        }
        dataChannel.configureBlocking(false);
        Selector selector = Selector.open();
        dataChannel.register(selector, OP_READ);
        selector.select(TIME_OUT);

        Set<SelectionKey> keys = selector.selectedKeys();
        if (keys.isEmpty() && ACK_COUNT < 10) {
            System.out.println("Timeout...Sending again");
            resend(dataChannel, pkt, routerAddress);
        }
    }

    private static void runClient(SocketAddress routerAddr, InetSocketAddress serverAddr, String msg)
            throws IOException {
        String dir = System.getProperty("user.dir");
        try (DatagramChannel dataChannel = DatagramChannel.open()) {
            SEQ_NO++;
            Packet pkt = (new Packet.Builder())
                    .setType(0)
                    .setSequenceNumber(SEQ_NO)
                    .setPortNumber(serverAddr.getPort())
                    .setPeerAddress(serverAddr.getAddress())
                    .setPayload(msg.getBytes()).create();
            dataChannel.send(pkt.toBuffer(), routerAddr);
            System.out.println("Sending the request to the Router");
            dataChannel.configureBlocking(false);
            Selector selector = Selector.open();
            dataChannel.register(selector, OP_READ);
            selector.select(TIME_OUT);
            Set<SelectionKey> keys = selector.selectedKeys();
            if (keys.isEmpty()) {
                System.out.println("Timeout...Sending again");
                resend(dataChannel, pkt, routerAddr);
            }
            ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
            SocketAddress router = dataChannel.receive(buf);
            buf.flip();
            Packet resp = Packet.fromBuffer(buf);
            String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);
            if (!receivedPackets.contains(resp.getSequenceNumber())) {
                receivedPackets.add(resp.getSequenceNumber());
                if (msg.contains("Content-Disposition:attachment")) {

                    String urlArray[] = payload.substring(payload.indexOf("\"url\":")).split(" ")[1]
                            .replaceAll("\"","").replaceAll("}","").split("/");

                    String dataSubString = payload.substring(payload.indexOf("\"data\":"));
                    int dataEndIndex =dataSubString.indexOf(",");
                    String Data = dataSubString.substring(9,dataEndIndex-1);

                    File file = new File(dir + "/attachment/" + urlArray[urlArray.length-1].trim());
                    file.createNewFile();
                    FileWriter fw = new FileWriter(file);
                    BufferedWriter bw = new BufferedWriter(fw);
                    PrintWriter pw = new PrintWriter(bw);
                    pw.print(Data);
                    pw.flush();
                    pw.close();
                    System.out.println();
                    System.out.println("File downloaded in: "+dir+"\\attachment");
                } else {
                    System.out.println("\n*********************************");
                    System.out.println(payload);
                }
                SEQ_NO++;
                Packet pkt2 = (new Packet.Builder()).setType(0)
                        .setSequenceNumber(SEQ_NO)
                        .setPortNumber(serverAddr.getPort())
                        .setPeerAddress(serverAddr.getAddress())
                        .setPayload("Received".getBytes()).create();
                dataChannel.send(pkt2.toBuffer(), routerAddr);
                dataChannel.configureBlocking(false);
                selector = Selector.open();
                dataChannel.register(selector, OP_READ);
                selector.select(TIME_OUT);
                keys = selector.selectedKeys();
                if (keys.isEmpty()) {
                    resend(dataChannel, pkt2, router);
                }
                buf.flip();
                System.out.println("Connection closed..!");
                keys.clear();
                SEQ_NO++;
                Packet pClose = (new Packet.Builder()).setType(0)
                        .setSequenceNumber(SEQ_NO)
                        .setPortNumber(serverAddr.getPort())
                        .setPeerAddress(serverAddr.getAddress())
                        .setPayload("Ok".getBytes()).create();
                dataChannel.send(pClose.toBuffer(), routerAddr);
                System.out.println("OK sent");
            }
        }
    }
}