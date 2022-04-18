package com.ali.vidiocall;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.GatheringByteChannel;

public class ServerActivity extends AppCompatActivity {

    ImageView image;
    Button btn_image;
    TextView tv_ip, tv_connect;

    final int PORT = 8080;

    ServerSocket serverSocket;
    Socket socket;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        findID();

        try {
            tv_ip.setText("IP : " + getLocalIpAddress() + "  PORT : " + PORT);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        try {
            serverSocket = new ServerSocket(PORT);
            new Thread(new thread1()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        btn_image.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                try {
                    startActivityForResult(intent, 1);
                } catch (ActivityNotFoundException e) {
                    // display error state to the user
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            new Thread(new thredsend(data)).start();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class thredsend implements Runnable {
        Intent data;

        thredsend(Intent data) {
            this.data = data;
        }

        @Override
        public void run() {
            try {
                InputStream stream = getContentResolver().openInputStream(
                        data.getData());
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                stream.close();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] b = baos.toByteArray();

                OutputStream out = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(out);
                dos.writeInt(b.length);
                dos.write(b, 0, b.length);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void findID() {
        image = findViewById(R.id.image_server);
        tv_ip = findViewById(R.id.tv_ip_server);
        btn_image = findViewById(R.id.btn_image_server);
        tv_connect = findViewById(R.id.tv_connect_server);
    }

    private String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
    }

    private class thread1 implements Runnable {

        @SuppressLint("SetTextI18n")
        @Override
        public void run() {

            try {
                socket = serverSocket.accept();
                runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        tv_connect.setText("connect");
//                        new Thread(new CommunicationThread(socket)).start();
                        new Thread(new thread2(socket)).start();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class thread2 implements Runnable {
        Socket socket;

        public thread2(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {

                try {
                    InputStream in = socket.getInputStream();
                    DataInputStream dis = new DataInputStream(in);

                    int len = dis.readInt();
                    byte[] data = new byte[len];
                    if (len > 0) {
                        dis.readFully(data);
                    }
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            image.setImageBitmap(bitmap);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}