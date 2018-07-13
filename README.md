# java-go-back-n-server

A demonstration of a client-server architecture implemented using Java Sockets. Accounts for random network delay and loss (i.e. if packets are piped through an intermediary program which simulates these conditions) using the [Go-Back-N](https://en.wikipedia.org/wiki/Go-Back-N_ARQ) protocol. The Sender side reads a file, breaks it into 500 character packets, and sends these across the network. The Receiver will accept these packets and reconstruct the original file when all hav been received.

If available, run 'make' to compile the requisite files, or equivalently 'javac Sender.java ListeningThread.java SendingThread.java Receiver.java Packet.java'.

Then, start the receiver's listening loop:
```
java Receiver sender_host sender_port receiver_port output_file
```

Start the sender:
```
java Sender receiver_host receiver_port sender_port source_file
```

* __sender_host__ is the address of the sender (needed to be able to send ACKs), either hostname or IP address.
* __sender_port__ is the port on which the sender will be listening for ACK packets.
* __receiver_host__ is the address of the receiver, either hostname or IP address.
* __receiver_port__ is the port on which the receiver will be listening for data packets.
* __source_file__ is the text file where the data you wish to be sent is stored.
* __output_file__ is the text file where the data that is sent will be output on the receiver side. This file does not have to exist prior to execution; if it does, it will be overwritten.