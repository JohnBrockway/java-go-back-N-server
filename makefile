
# Specify compiler
JCC = javac

# Specify flags (-g is compile with debugging)
FLAGS = -g

default: Sender.java ListeningThread.java SendingThread.java Receiver.java Packet.java
	$(JCC) $(FLAGS) Sender.java ListeningThread.java SendingThread.java Receiver.java Packet.java

clean:
	$(RM) Sender.class ListeningThread.class SendingThread.class Receiver.class Packet.class