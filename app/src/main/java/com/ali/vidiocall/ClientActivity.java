package com.ali.vidiocall;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ClientActivity extends AppCompatActivity {

    TextView tv_connect;
    EditText edt_ip, edt_port;
    ImageView image, btn_connect;
    Button btn_send;

    Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        findId();

        btn_connect.setOnClickListener(view -> {
            view.startAnimation(AnimationUtils.loadAnimation(ClientActivity.this, android.R.anim.fade_in));

            if (!(edt_ip.getText().toString().isEmpty() || edt_port.getText().toString().isEmpty())) {
                new Thread(new thred1()).start();
            } else {
                Toast.makeText(ClientActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });

        btn_send.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            try {
                startActivityForResult(intent, 1);
            } catch (ActivityNotFoundException e) {
                // display error state to the user
            }
        });

    }

    private class thred1 implements Runnable {

        @Override
        public void run() {
            try {
                socket = new Socket(edt_ip.getText().toString(), Integer.parseInt(edt_port.getText().toString()));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
           new Thread(new thredsend(data)).start();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class thredsend implements Runnable{
        Intent data;
        thredsend(Intent data){this.data = data;}
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

    private void findId() {
        tv_connect = findViewById(R.id.tv_connect_client);
        edt_ip = findViewById(R.id.edt_ip_client);
        edt_port = findViewById(R.id.edt_port_client);
        btn_send = findViewById(R.id.btn_image_client);
        btn_connect = findViewById(R.id.btn_connect_client);
        image = findViewById(R.id.image_client);
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