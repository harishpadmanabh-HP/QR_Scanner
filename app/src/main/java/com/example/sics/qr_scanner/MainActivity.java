package com.example.sics.qr_scanner;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.google.zxing.Result;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView scannerView;//test git
    private int storagepermissioncode=1;
    String str =null;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences=getApplicationContext().getSharedPreferences("pref",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA},storagepermissioncode);

        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
            Toast.makeText(MainActivity.this, "ALREADY GRANTED PERMISSION", Toast.LENGTH_SHORT).show();
        }
        else{
            requeststroagepermission();
        }
    }
    private void requeststroagepermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)){
            new AlertDialog.Builder(this)
                    .setTitle("PERMISSION NEEDED")
                    .setMessage("This permission is needed for scan QR")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA},storagepermissioncode);

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }
        else{
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA},storagepermissioncode);

        }
    }
  @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       if(requestCode==storagepermissioncode){
            if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "PERMISSION GRANTED", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(this, "Scan QR", Toast.LENGTH_SHORT).show();
        }
    }

    public void Scanner(View view) {

        scannerView = new ZXingScannerView(this);
        scannerView.setResultHandler(this);

        setContentView(scannerView);
        scannerView.startCamera();

    }


    @Override
    public void handleResult(Result result) {

      //  final String resultcode = result.getText();
       final String resultcode = result.getText();
Log.e("RESULTCODE",resultcode);
        Toast.makeText(this, ""+resultcode, Toast.LENGTH_SHORT).show();

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Scan result");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
//
//                String as=resultcode.substring(1, resultcode.length()-1);
//
//                String[] ani = as.split(",");
//                int[] A=new int[4000];
//                int k=0;
//
//                for(int i=0;i<ani.length;i++){
//                    try
//                    {
//                        // the String to int conversion happens here
//                        int ab = Integer.parseInt(ani[i].trim());
//
//                        A[i]=ab;
//
//
//                        // print out the value after the conversion
//                        System.out.println("int i = " + A[i]);
//                    }
//                    catch (NumberFormatException nfe)
//                    {
//                        System.out.println("NumberFormatException: " + nfe.getMessage());
//                    }
//                }
//                System.out.println("integer");
//                for(int i=0;i<ani.length;i++){
//                    System.out.println(A[i]);
//                }
//                String msg="";
//                for(int i: A){
//                    str = Character.toString((char)i);
//                    System.out.print(str);
//                    msg=msg+str;
//                    Log.e("ENCODE",str+"");
//                    Log.e("ENCODESTRING",msg+"");
//
////                    editor.putString("qrcode",msg);
////                    Log.e("ENCODEINNN",str);
////                    editor.commit();
//            }
                editor.putString("qrcode",resultcode);
                //Log.e("ENCODEINNN",msg);
                editor.commit();
               // System.out.println(msg);
                Intent iny=new Intent(getApplicationContext(),ResultActivity.class);

                startActivity(iny);
            }
        });



      //  builder.setMessage(str.toString());
        AlertDialog alertdialoge=builder.create();
        alertdialoge.show();






    }

}

