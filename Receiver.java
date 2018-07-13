import java.io.*;
import java.net.*;
import java.util.*;

public class Receiver {

    public static final String logFileName = "arrival.log";
    public static final int eotFileType = 2;
    public static final int seqNumModulo = 32;

    public static void main(String[] args) throws Exception {
        InetAddress destAddress = InetAddress.getByName(args[0]);
        int destPort = Integer.parseInt(args[1]);
        int listeningPort = Integer.parseInt(args[2]);
        String destFile = args[3];

        int expectedSeqNum = 0;
        List<Packet> listOfPackets = new ArrayList<Packet>();
        List<Integer> arrivals = new ArrayList<Integer>();

        DatagramSocket socket = new DatagramSocket(listeningPort);
        
        Packet pkt = waitForInput(socket);
        while (pkt.getType() != eotFileType) {
            
            arrivals.add(pkt.getSeqNum());

            if (pkt.getSeqNum() == expectedSeqNum % seqNumModulo) {
                listOfPackets.add(pkt);
                expectedSeqNum++;
            }

            // Don't send ACK if first packet is lost
            if (expectedSeqNum != 0) {
                // Send ACK for last correctly received packet (i.e. the one prior to the expected packet)
                sendACK(socket, destAddress, destPort, expectedSeqNum - 1);
            }

            pkt = waitForInput(socket);
        }

        Packet eot = Packet.createEOT(-1);
        byte[] eotPacket = eot.getUDPdata();
        DatagramPacket eotMsgPacket = new DatagramPacket(eotPacket, eotPacket.length, destAddress, destPort);
        socket.send(eotMsgPacket);
        socket.close();

        writeDataFile(destFile, listOfPackets);
        writeLogFile(logFileName, arrivals);
    }

    // Write the tranferred data to the specified output file
    public static void writeDataFile(String filename, List<Packet> listOfPackets) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (Packet p : listOfPackets) {
            writer.write(new String(p.getData()));
        }
        writer.close();
    }

    // Write the arrival.log file given the list of arrivals
    public static void writeLogFile(String filename, List<Integer> arrivals) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (Integer i : arrivals) {
            writer.write(Integer.toString(i));
            writer.newLine();
        }
        writer.close();
    }

    // Helper method to send an ACK packet
    public static void sendACK(DatagramSocket socket, InetAddress destAddress, int destPort, int seqNum) throws Exception {
        Packet ack = Packet.createACK(seqNum);
        byte[] returnPacket = ack.getUDPdata();
        DatagramPacket returnMsgPacket = new DatagramPacket(returnPacket, returnPacket.length, destAddress, destPort);
        socket.send(returnMsgPacket);
    }

    // Helper method to abstract waiting for input to the port
    public static Packet waitForInput(DatagramSocket socket) throws Exception {
        byte[] msgAsBytes = new byte[512];
        DatagramPacket msgPacket = new DatagramPacket(msgAsBytes, msgAsBytes.length);
        socket.receive(msgPacket);
        return Packet.parseUDPdata(msgPacket.getData());
    }
}