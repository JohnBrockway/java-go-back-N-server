import java.io.*;
import java.net.*;
import java.util.*;

class ListeningThread extends Thread {
    
    private Thread t;
    private DatagramSocket socket;
    private List<Integer> listOfACKs;

    public ListeningThread(DatagramSocket socket) {
        this.socket = socket;
        listOfACKs = new ArrayList<Integer>();
    }

    public void start() {
        if (t == null) {
            t = new Thread(this, "Listener");
            t.start();
        }
    }

    public void run() {
        try {
            Packet ack = waitForInput();
            while(ack.getType() != Sender.eotFileType) {
                listOfACKs.add(ack.getSeqNum());
                Sender.base = moduloBaseUpdate(Sender.base, ack.getSeqNum(), Sender.windowSize, Sender.seqNumModulo);
                ack = waitForInput();
            }
            writeACKLogFile(listOfACKs);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            socket.close();
        }
    }

    // Helper function to handle waiting for and receiving an incoming ACK
    public Packet waitForInput() throws Exception {
        byte[] msgAsBytes = new byte[512];
        DatagramPacket msgPacket = new DatagramPacket(msgAsBytes, msgAsBytes.length);
        socket.receive(msgPacket);
        return Packet.parseUDPdata(msgPacket.getData());
    }

    // Writes the ack.log file
    public void writeACKLogFile(List<Integer> listOfACKs) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(Sender.ackLogFileName));
        for (Integer i : listOfACKs) {
            writer.write(Integer.toString(i));
            writer.newLine();
        }
        writer.close();
    }

    // Helper function to update the send window base cleanly based on an incoming ACK
    public int moduloBaseUpdate(int base, int incoming, int windowSize, int modulo) {
        for (int i = 0 ; i < windowSize ; i++) {
            // Check if the newly ACKed packet's sequence number is in the window
            if ((base + i) % modulo == incoming) {
                return base + i + 1;
            }
        }
        
        // Default return is the existing base (i.e. if this was an older ACK, don't decrement base)
        return base;
    }
}