import java.io.*;
import java.net.*;
import java.util.*;

class SendingThread extends Thread {

    private Thread t;
    private DatagramSocket socket;
    private List<Packet> listOfPackets;
    private InetAddress destAddress;
    private int destPort;
    private List<Integer> listOfSeqNums;

    public SendingThread(DatagramSocket socket, List<Packet> listOfPackets, InetAddress destAddress, int destPort) {
        this.socket = socket;
        this.listOfPackets = listOfPackets;
        this.destAddress = destAddress;
        this.destPort = destPort;
        listOfSeqNums = new ArrayList<Integer>();
    }

    public void start() {
        if (t == null) {
            t = new Thread(this, "Sender");
            t.start();
        }
    }

    public void run() {
        try {
            long timerStart = -1;

            // Keep copy of base locally to avoid thread-safety related inconsistencies within an iteration
            int currentBase;
            while ((currentBase = Sender.base) < listOfPackets.size()) {
                // If the timer is expired, send all queued packets and restart the timer
                if (timeout(timerStart)) {
                    timerStart = System.currentTimeMillis();
                    for (int i = currentBase ; i < Sender.nextSeqNum ; i++) {
                        sendData(listOfPackets.get(i));
                    }
                }
                // If 1. The window is not completely utilized and 2. There are more packets to be sent, send another one
                if ((Sender.nextSeqNum < currentBase + Sender.windowSize) && (Sender.nextSeqNum < listOfPackets.size())) {
                    sendData(listOfPackets.get(Sender.nextSeqNum));
                    if (Sender.nextSeqNum == currentBase) {
                        timerStart = System.currentTimeMillis();
                    }
                    Sender.nextSeqNum++;
                }
            }

            // Send EOT packet
            Packet sentPkt = Packet.createEOT(-1);
            byte[] packetData = sentPkt.getUDPdata();
            DatagramPacket msgPacket = new DatagramPacket(packetData, packetData.length, destAddress, destPort);
            socket.send(msgPacket);

            writeSendLogFile(listOfSeqNums);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            socket.close();
        }
    }

    // Helper function to send a data packet
    public void sendData(Packet pkt) throws Exception {
        listOfSeqNums.add(pkt.getSeqNum());
        byte[] packetData = pkt.getUDPdata();
        DatagramPacket msgPacket = new DatagramPacket(packetData, packetData.length, destAddress, destPort);
        socket.send(msgPacket);
    }

    // Helper function to write the seqnum.log file
    public void writeSendLogFile(List<Integer> listOfSeqNums) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(Sender.sendlogFileName));
        for (Integer i : listOfSeqNums) {
            writer.write(Integer.toString(i));
            writer.newLine();
        }
        writer.close();
    }

    // Helper function to compute whether a timeout has occured, each iteration of the loop
    public boolean timeout(long timerStart) {
        if (timerStart == -1) {
            return false;
        }
        else if (System.currentTimeMillis() - timerStart > 100) {
            return true;
        }
        else {
            return false;
        }
    }
}