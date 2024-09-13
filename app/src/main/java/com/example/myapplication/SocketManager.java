package com.example.myapplication;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketManager {
    private static Socket socket;
    private static OutputStream outputStream;
    private static InputStream inputStream;

    public static synchronized void setSocket(Socket s) {
        try {
            // Đóng socket và inputStream, outputStream cũ nếu có
            if (socket != null) {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                socket.close();
            }

            socket = s;
            if (socket != null) {
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized Socket getSocket() {
        return socket;
    }

    public static synchronized OutputStream getOutputStream() {
        return outputStream;
    }

    public static synchronized InputStream getInputStream() {
        return inputStream;
    }

    // Đóng socket, inputStream và outputStream khi không cần thiết
    public static synchronized void close() {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
