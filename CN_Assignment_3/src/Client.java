import static java.nio.channels.SelectionKey.OP_READ;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
        ArrayList<String> urlList = new ArrayList<>();
        File file = new File("Ass3");
        file.mkdir();
        while (true) {
            String url = "";
            String url1 = "";
            System.out.print("Please enter your URL: ");
            receivedPackets.clear();
            SEQ_NO = 0;
            ACK_COUNT = 0;
            Scanner sc = new Scanner(System.in);
            url = sc.nextLine();

            if (url.isEmpty() || url.length() == 0) {
                System.out.println("Please enter valid URL!!!");
                continue;
            }

            String[] arr = url.split(" ");
            urlList = new ArrayList<>();
            for (int i = 0; i < arr.length; i++) {
                if(arr[i].startsWith("http://")) {
                    url1 = arr[i];
                }
                urlList.add(arr[i]);
            }

            String hostName = new URL(url1).getHost();
            int hostPort = new URL(url1).getPort();
            SocketAddress routerAddress = new InetSocketAddress(routerHost, ROUTER_PORT);
            InetSocketAddress serverAddress = new InetSocketAddress(hostName, hostPort);
            proceedConnection(routerAddress, serverAddress);
            runClient(routerAddress, serverAddress, url);
        }
    }

    private static void proceedConnection(SocketAddress routerAddress, InetSocketAddress serverAddress) throws Exception {

        try (DatagramChannel channel = DatagramChannel.open()) {
            String msg = "Please Confirm I am Client";
            SEQ_NO++;
            Packet p = new Packet.Builder().setType(0).setSequenceNumber(SEQ_NO)
                    .setPortNumber(serverAddress.getPort()).setPeerAddress(serverAddress.getAddress())
                    .setPayload(msg.getBytes()).create();
            channel.send(p.toBuffer(), routerAddress);
            System.out.println("CLient has sended Msg..");

            channel.configureBlocking(false);
            Selector selector = Selector.open();
            channel.register(selector, OP_READ);

            selector.select(TIME_OUT);

            Set<SelectionKey> keys = selector.selectedKeys();
            if (keys.isEmpty()) {
                System.out.println("Opps!! Timeover");
                System.out.println("Retrying...");
                resend(channel, p, routerAddress);
            }
            ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
            Packet resp = Packet.fromBuffer(buf);
            String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);
            System.out.println(payload.trim() + "Got response from server");
            receivedPackets.add(resp.getSequenceNumber());
            keys.clear();
        }
    }
    private static void resend(DatagramChannel channel, Packet p, SocketAddress routerAddress) throws IOException {
        channel.send(p.toBuffer(), routerAddress);
        System.out.println(new String(p.getPayload()));
        if (new String(p.getPayload()).equals("Got the Message")) {
            ACK_COUNT++;
        }
        channel.configureBlocking(false);
        Selector selector = Selector.open();
        channel.register(selector, OP_READ);
        selector.select(TIME_OUT);

        Set<SelectionKey> keys = selector.selectedKeys();
        if (keys.isEmpty() && ACK_COUNT < 10) {
            System.out.println("Opps!! Timeover");
            System.out.println("Retrying...");
            resend(channel, p, routerAddress);
        } else {
            return;
        }
    }

    private static void runClient(SocketAddress routerAddr, InetSocketAddress serverAddr, String msg)
            throws IOException {
        String dir = System.getProperty("user.dir");
        try (DatagramChannel channel = DatagramChannel.open()) {
            SEQ_NO++;
            Packet p = new Packet.Builder().setType(0).setSequenceNumber(SEQ_NO)
                    .setPortNumber(serverAddr.getPort()).setPeerAddress(serverAddr.getAddress())
                    .setPayload(msg.getBytes()).create();
            channel.send(p.toBuffer(), routerAddr);
            System.out.println("Sending the request to the Router");
            channel.configureBlocking(false);
            Selector selector = Selector.open();
            channel.register(selector, OP_READ);
            selector.select(TIME_OUT);
            Set<SelectionKey> keys = selector.selectedKeys();
            if (keys.isEmpty()) {
                System.out.println("Opps!! Timeover");
                System.out.println("Retrying...");
                resend(channel, p, routerAddr);
            }
            ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
            SocketAddress router = channel.receive(buf);
            buf.flip();
            Packet resp = Packet.fromBuffer(buf);
            String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);
            if (!receivedPackets.contains(resp.getSequenceNumber())) {
                receivedPackets.add(resp.getSequenceNumber());
                if (msg.contains("Content-Disposition:Ass3")) {
                    String[] responseArray = payload.split("\\|");
                    File file = new File(dir + "/Ass3/" + responseArray[1].trim());
                    System.out.println("FILENAME::::::::::::::::::"+responseArray[1].trim());
                    file.createNewFile();
                    FileWriter fw = new FileWriter(file);
                    BufferedWriter bw = new BufferedWriter(fw);
                    PrintWriter pw = new PrintWriter(bw);
                    pw.print(responseArray[2]);
                    pw.flush();
                    pw.close();
                    System.out.println(responseArray[0]);
                    System.out.println("File is saved as Ass3");
                } else {
                    System.out.println("\n*********************************");
                    System.out.println(payload);
                }
                SEQ_NO++;
                Packet pAck = new Packet.Builder().setType(0).setSequenceNumber(SEQ_NO)
                        .setPortNumber(serverAddr.getPort()).setPeerAddress(serverAddr.getAddress())
                        .setPayload("Got the Message".getBytes()).create();
                channel.send(pAck.toBuffer(), routerAddr);
                channel.configureBlocking(false);
                selector = Selector.open();
                channel.register(selector, OP_READ);
                selector.select(TIME_OUT);
                keys = selector.selectedKeys();
                if (keys.isEmpty()) {
                    resend(channel, pAck, router);
                }
                buf.flip();
                System.out.println("Connection closed");
                keys.clear();
                SEQ_NO++;
                Packet pClose = new Packet.Builder().setType(0).setSequenceNumber(SEQ_NO)
                        .setPortNumber(serverAddr.getPort()).setPeerAddress(serverAddr.getAddress())
                        .setPayload("Ok".getBytes()).create();
                channel.send(pClose.toBuffer(), routerAddr);
            }
        }
    }
}