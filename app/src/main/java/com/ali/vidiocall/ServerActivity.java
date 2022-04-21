package com.ali.vidiocall;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
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

    ImageView image_get;
    TextView tv_ip, tv_connect;
    FrameLayout fl_show_camera;

    final int PORT = 8080;

    Camera camera;
    ShowCamera showCamera;

    ServerSocket serverSocket;
    Socket socket;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        findID();

        showcamera();


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


    }

    private void showcamera() {
        camera = Camera.open();
        showCamera = new ShowCamera(this,camera);
        fl_show_camera.addView(showCamera);
    }


    private class thredsend implements Runnable {

        @Override
        public void run() {
            while (true) {
                camera.takePicture(null, null, pick());
                showcamera();
                try {
                    Thread.sleep(333);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        private Camera.PictureCallback pick() {
            return (bytes, camera) -> {
                try {
                    OutputStream out = socket.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(out);
                    dos.writeInt(bytes.length);
                    dos.write(bytes, 0, bytes.length);
                }catch (IOException e){
                    e.printStackTrace();
                }
            };
        }
    }

    private void findID() {
        image_get = findViewById(R.id.image_show_get_server);
        tv_ip = findViewById(R.id.tv_ip_server);
        fl_show_camera = findViewById(R.id.fm_show_camera_server);
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
                        new Thread(new thread2(socket)).start();
                        new Thread(new thredsend()).start();
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
                            image_get.setImageBitmap(bitmap);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}