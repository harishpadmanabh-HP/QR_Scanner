package com.example.sics.qr_scanner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {
    TextView result;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        result=findViewById(R.id.result );
        sharedPreferences=getApplicationContext().getSharedPreferences("pref",MODE_PRIVATE);
        String res=sharedPreferences.getString("qrcode","");
        Log.e("INSIDE_ENCODE",res);

        result.setText(res);
    }
}
