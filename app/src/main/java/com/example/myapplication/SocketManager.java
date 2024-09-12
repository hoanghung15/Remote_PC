package com.example.myapplication;

import java.io.OutputStream;
import java.net.Socket;

public class SocketManager {
    private static Socket socket;
    private static OutputStream outputStream;

    public static void setSocket(Socket s) {
        socket = s;
        try {
            outputStream = socket.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Socket getSocket() {
        return socket;
    }

    public static OutputStream getOutputStream() {
        return outputStream;
    }
}

