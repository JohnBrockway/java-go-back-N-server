import java.io.*;
import java.net.*;
import java.util.*;

public class Sender {

    private static final int maxDataLength = 500;
    public static final int windowSize = 10;
    public static final int seqNumModulo = 32;  
    public static final int eotFileType = 2;
    public static final String ackLogFileName = "ack.log";
    public static final String sendlogFileName = "seqnum.log";  

    public static volatile int base = 0;
    public static volatile int nextSeqNum = 0;

    public static void main(String[] args) throws Exception {
        InetAddress destAddress = InetAddress.getByName(args[0]);
        int destPort = Integer.parseInt(args[1]);
        int listeningPort = Integer.parseInt(args[2]);
        String sourceFile = args[3];

        List<Packet> listOfPackets = readFile(sourceFile);

        DatagramSocket sendSocket = new DatagramSocket();
        DatagramSocket receiveSocket = new DatagramSocket(listeningPort);

        ListeningThread listener = new ListeningThread(receiveSocket);
        SendingThread sender = new SendingThread(sendSocket, listOfPackets, destAddress, destPort);
        listener.start();
        sender.start();
        listener.join();
        sender.join();
    }

    // Read the input file and return an ordered list of packets representing the data it contained
    public static List<Packet> readFile(String filename) throws Exception {
        List<Packet> listOfPackets = new ArrayList<Packet>();

        BufferedReader br = new BufferedReader(new FileReader(filename));
        char[] inputBuf = new char[maxDataLength];
        int seqNum = 0;
        int readResult = br.read(inputBuf, 0, maxDataLength);
        while(readResult != -1) {
            listOfPackets.add(Packet.createPacket(seqNum, new String(inputBuf, 0, readResult)));
            seqNum++;
            readResult = br.read(inputBuf, 0, maxDataLength);
        }
        br.close();

        return listOfPackets;
    }
}