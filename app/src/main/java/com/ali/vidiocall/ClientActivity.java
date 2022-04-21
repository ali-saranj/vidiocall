package com.ali.vidiocall;


import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientActivity extends AppCompatActivity {

    TextView tv_connect;
    EditText edt_ip, edt_port;
    ImageView image_get, btn_connect;
    FrameLayout fm_show;
    Camera camera;
    ShowCamera showCamera;

    Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        findId();
        showcamera();

        btn_connect.setOnClickListener(view -> {
            view.startAnimation(AnimationUtils.loadAnimation(ClientActivity.this, android.R.anim.fade_in));

            if (!(edt_ip.getText().toString().isEmpty() || edt_port.getText().toString().isEmpty())) {
                new Thread(new thred1()).start();
            } else {
                Toast.makeText(ClientActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void showcamera() {
        camera = Camera.open();
        showCamera = new ShowCamera(this,camera);
        fm_show.addView(showCamera);
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
                        new Thread(new thread2(socket)).start();
                        new Thread(new thredsend()).start();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                    Log.i("ali",bytes.length+"");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                dos.writeInt(bytes.length);
                                dos.write(bytes, 0, bytes.length);
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                    });
                }catch (IOException e){
                    e.printStackTrace();
                }
            };
        }
    }

    private void findId() {
        tv_connect = findViewById(R.id.tv_connect_client);
        edt_ip = findViewById(R.id.edt_ip_client);
        edt_port = findViewById(R.id.edt_port_client);
        btn_connect = findViewById(R.id.btn_connect_client);
        fm_show = findViewById(R.id.fm_show_camera_client);
        image_get = findViewById(R.id.image_show_get_client);
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