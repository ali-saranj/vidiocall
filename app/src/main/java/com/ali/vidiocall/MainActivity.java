package com.ali.vidiocall;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    CardView btn_server,btn_client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_server = findViewById(R.id.btn_server);
        btn_client = findViewById(R.id.btn_claint);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.CAMERA},
                1);

        btn_client.setOnClickListener(onClickClient());
        btn_server.setOnClickListener(onClickServer());

    }

    private View.OnClickListener onClickServer() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,android.R.anim.fade_in));
                startActivity(new Intent(MainActivity.this,ServerActivity.class));
            }
        };
    }

    private View.OnClickListener onClickClient() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,android.R.anim.fade_in));
                startActivity(new Intent(MainActivity.this,ClientActivity.class));
            }
        };
    }
}