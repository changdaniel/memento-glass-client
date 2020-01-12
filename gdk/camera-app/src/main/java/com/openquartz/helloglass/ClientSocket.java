package com.openquartz.helloglass;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class ClientSocket {

    private Socket socket;

    private static final int SERVERPORT = 8088;
    //private static final String SERVER_IP = "10.105.249.164"; //Daniel
    //private static final String SERVER_IP = "172.31.34.73"; //EC2 Private
    private static final String SERVER_IP = "3.134.84.232"; //EC2 Public
    public ClientSocket(){

        new Thread(new ClientThread()).start();
    }

    public void sendBytes(byte[] bytes) throws IOException{

        int start=0;
        int len=bytes.length;
        if (len < 0)
            throw new IllegalArgumentException("Negative length not allowed");
        if (start < 0 || start >= bytes.length)
            throw new IndexOutOfBoundsException("Out of bounds: " + start);

        OutputStream out = socket.getOutputStream();
        DataOutputStream dos = new DataOutputStream(out);

        //dos.writeInt(len);
        String headerString = "SIZE" + String.valueOf(len);
        byte[] headerBytes = headerString.getBytes("UTF-8");
        dos.write(headerBytes,0, headerBytes.length);
//        if (len > 0) {
//            dos.write(bytes, start, len);
//        }

    }

    class ClientThread implements Runnable {

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

                socket = new Socket(serverAddr, SERVERPORT);

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

    }
}
