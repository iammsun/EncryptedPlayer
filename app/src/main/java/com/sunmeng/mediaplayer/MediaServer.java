package com.sunmeng.mediaplayer;

import android.text.TextUtils;
import android.util.Log;

import com.sunmeng.mediaplayer.downloader.IEncrypt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * Created by sunmeng on 16/8/6.
 */
public class MediaServer extends Thread {

    private static final String TAG = "MediaServer";

    private static final int BUFFER_SIZE = 10 * 1024 * 1024;

    public static final int PORT = 12345;

    private ServerSocket serverSocket;
    private int mPort;
    private final IEncrypt encrypt;

    private static MediaServer instance;

    public static void init(IEncrypt encrypt) {
        if (instance == null) {
            synchronized (MediaServer.class) {
                if (instance == null) {
                    instance = new MediaServer(encrypt);
                }
            }
        }
    }

    public static MediaServer getInstance() {
        return instance;
    }

    private MediaServer(IEncrypt encrypt) {
        this.encrypt = encrypt;
    }

    private class ProcessThread extends Thread {

        private final Socket socket;

        public ProcessThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            FileInputStream inputStream = null;
            try {
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter out = new PrintWriter(outputStream, true);

                // get request head
                BufferedReader br = new BufferedReader(new InputStreamReader(socket
                        .getInputStream()));
                String request;
                String filePath = null;
                long seekBegin = 0;
                long seekEnd = 0;
                while (!TextUtils.isEmpty(request = br.readLine())) {
                    Log.d(TAG, "request HEAD: " + request);
                    String[] head = request.split("\\s+");
                    if ("GET".equalsIgnoreCase(head[0])) {
                        filePath = head[1];
                    } else if ("Range:".equalsIgnoreCase(head[0])) {
                        try {
                            seekBegin = Long.parseLong(request.substring(request.lastIndexOf("=")
                                    + 1, request.lastIndexOf("-")));
                        } catch (NumberFormatException e) {
                        }
                        try {
                            seekEnd = Long.parseLong(request.substring(request.lastIndexOf("-") +
                                    1));
                        } catch (NumberFormatException e) {
                        }
                    }
                }
                File target;
                if (filePath == null || !(target = new File(filePath)).exists()) {
                    out.println("HTTP/1.1 404 Not found");
                    out.println();
                    return;
                }
                boolean encrypted = false;
                EncryptedFile encryptedFile = null;
                if (encrypt != null) {
                    encryptedFile = new EncryptedFile(encrypt, target);
                    encrypted = encryptedFile.isEncrypted();
                }
                long fileLen = (encrypted ? encryptedFile.length() : target.length());
                if (seekBegin > 0) {
                    if (seekEnd == 0) {
                        seekEnd = fileLen - 1;
                    }
                    responseHead(out, "HTTP/1.1 206 Partial");
                    responseHead(out, "Content-Range:" + seekBegin + "-" + seekEnd + "/" + fileLen);
                    responseHead(out, "Content-Length:" + Math.max(0, (seekEnd - seekBegin + 1)));
                } else {
                    responseHead(out, "HTTP/1.1 200 OK");
                    responseHead(out, "Content-Length:" + fileLen);
                }
                responseHead(out, "Content-Type:application/octet-stream");
                responseHead(out, "Last-Modified:" + new Date(target.lastModified()));
                responseHead(out, "Accept-Ranges:bytes");
                out.println();

                // response data
                inputStream = new FileInputStream(target);
                if (encrypted) {
                    inputStream.skip(encrypt.getSignature().getBytes().length);
                }
                if (seekBegin > 0) {
                    inputStream.skip(seekBegin);
                }
                byte[] buffer = new byte[BUFFER_SIZE];
                int length;
                long read = seekBegin;
                while ((length = inputStream.read(buffer)) != -1) {
                    if (encrypted) {
                        buffer = encrypt.decrypt(buffer, 0, length);
                    }
                    if (seekBegin != 0 && (read + length) > (seekEnd + 1)) {
                        outputStream.write(buffer, 0, (int) (seekEnd + 1 - read));
                        break;
                    } else {
                        outputStream.write(buffer, 0, length);
                    }
                    read += length;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Utils.close(inputStream);
                Utils.close(socket);
            }
        }

        private void responseHead(PrintWriter printWriter, String head) {
            Log.d(TAG, "response HEAD: " + head);
            printWriter.println(head);
        }
    }

    public synchronized int getPort() {
        return mPort;
    }

    @Override
    public synchronized void start() {
        int port = PORT;
        while (true) {
            try {
                serverSocket = new ServerSocket(port);
                synchronized (this) {
                    mPort = port;
                }
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            port++;
        }
        super.start();
    }

    private void loop() {
        Log.d(TAG, "media server is started......");
        while (!Thread.interrupted()) {
            Socket client = null;
            try {
                client = serverSocket.accept();
            } catch (IOException e) {
            }
            if (client != null) {
                new ProcessThread(client).start();
            }
        }
    }

    @Override
    public void run() {
        loop();
        Utils.close(serverSocket);
    }
}
