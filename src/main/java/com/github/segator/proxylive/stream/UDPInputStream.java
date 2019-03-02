package com.github.segator.proxylive.stream;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.regex.Pattern;

public class UDPInputStream extends VideoInputStream {
    private final InetAddress server;
    private final int port;
    private final byte[] buffer;
    private byte[] currentPacket;
    private int currentPacketSize;
    private int currentPacketPosition=0;
    private DatagramSocket clientSocket;


    public UDPInputStream(String url) throws  IOException {
        url = url.split(Pattern.quote("udp://"))[1];
        server = InetAddress.getByName(url.split(Pattern.quote(":"))[0]);
        port = new Integer(url.split(Pattern.quote(":"))[1]);
        buffer  = new byte[131072];
    }

    private void initializeConnection() throws IOException {
        //clientSocket = new DatagramSocket();
        clientSocket = new MulticastSocket(port);
        ((MulticastSocket) clientSocket).joinGroup(server);
        clientSocket.setSoTimeout(5000);
        //clientSocket.connect(server,port);
    }

    public boolean connect() throws IOException {
        initializeConnection();
        return true;
    }

    public boolean isConnected() {
        return clientSocket.isBound();
    }



    @Override
    public int read() throws IOException {
        return 0;
    }



    @Override
    public int read(byte b[]) throws IOException {
        if(currentPacket==null || currentPacketPosition==currentPacketSize) {
            DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length, server, port);
            clientSocket.receive(inPacket);
            currentPacket = inPacket.getData();
            currentPacketSize = inPacket.getLength();
            if(inPacket.getOffset()!=0){
                System.out.println("asdsad");
            }
            currentPacketPosition=0;
        }
        int remainingBytes=currentPacketSize-currentPacketPosition;
        int toRead=b.length;
        if(b.length>remainingBytes){
            toRead=remainingBytes;
        }
        int i=0;
        for (; i < toRead; i++) {
            b[i] = currentPacket[currentPacketPosition];
            currentPacketPosition++;
        }
        return i;
    }

    public void close() throws IOException {
        if (isConnected()) {
            clientSocket.close();
        } else {
            throw new IOException("The Stream of udp://" + server + ":"+port + " is not connected");
        }
    }
}