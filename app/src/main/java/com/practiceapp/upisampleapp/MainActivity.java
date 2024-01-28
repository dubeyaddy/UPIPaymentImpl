package com.practiceapp.upisampleapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.practiceapp.upisampleapp.R;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    EditText nametxt,upiIdtxt,amttxt,msgtxt,tnIdtxt,refIdtxt,codetxt;
    Button paybtn;
    final int PAY_REQUEST = 123;
    String GOOGLE_PAY_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nametxt =findViewById(R.id.et_name);
        upiIdtxt =findViewById(R.id.et_upiId);
        amttxt =findViewById(R.id.et_amt);
        msgtxt= findViewById(R.id.et_msg);
        tnIdtxt =findViewById(R.id.et_tnid);
        refIdtxt =findViewById(R.id.et_refId);
        codetxt= findViewById(R.id.et_merchantCode);
        paybtn=findViewById(R.id.bt_pay);

        paybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name= nametxt.getText().toString();
                String upiId= upiIdtxt.getText().toString();
                String amt = amttxt.getText().toString();
                String msg= msgtxt.getText().toString();
                String tnid= tnIdtxt.getText().toString();
                String refid= refIdtxt.getText().toString();
                String merchantCode= codetxt.getText().toString();

                if (name.isEmpty() || upiId.isEmpty()){
                    Toast.makeText(MainActivity.this,"Name and UPI id required",Toast.LENGTH_SHORT).show();
                }else {
                    try {
                        PayUsingUPI(name,upiId,amt,msg,tnid,refid,merchantCode);
                    }catch (Exception upi){
                        Toast.makeText(MainActivity.this,"Task failed",Toast.LENGTH_SHORT).show();
//                        Intent intent= new Intent(MainActivity.this,MainActivity.class);
//                        startActivity(intent);
                    }
                }
            }
        });
    }
    public void PayUsingUPI(String name,String upiId,String amt,String msg,String tnid,String refid,String merchantCode){
        try {

//            Uri uri = new Uri.Builder()
            Uri uri= Uri.parse("upi://pay").buildUpon()
//                    .scheme("upi")
//                    .authority("pay")
                    .appendQueryParameter("pa", upiId)
                    .appendQueryParameter("pn", name)
                    .appendQueryParameter("tn", msg)
                    .appendQueryParameter("tid", tnid)
                    .appendQueryParameter("tr", refid)
                    .appendQueryParameter("mc", merchantCode)
                    .appendQueryParameter("cu", "INR")
                    .appendQueryParameter("am", amt)
                    .build();

            Intent upiIntent = new Intent(Intent.ACTION_VIEW);
            upiIntent.setData(uri);
            Intent chooser = Intent.createChooser(upiIntent, "Pay");
            if (chooser.resolveActivity(getPackageManager()) != null) {
                upiIntent.setPackage(GOOGLE_PAY_PACKAGE_NAME);
                startActivityForResult(upiIntent, PAY_REQUEST);
            } else {
                Toast.makeText(this, "No UPI App found", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e) {
            Log.e("UPIpay", e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode== PAY_REQUEST){
            if (isInternetAvailable(MainActivity.this)){
                if (data==null){
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    String temp= "nothing";
                    Toast.makeText(this, "Transaction not complete", Toast.LENGTH_SHORT).show();
                }else {
                    String text = data.getStringExtra("response");
                    ArrayList<String> dataList= new ArrayList<>();
                    dataList.add(text);

                    upiPaymentCheck(text);
                }
            }
        }
    }

    void upiPaymentCheck(String data){
        String str= data;
        String paymentcancel="";
        String status="";
        String response[]= str.split("&");

        for (int i=0;i<response.length;i++){
            String equalstr[]= response[i].split("");
            if (equalstr.length >= 2){
                if (equalstr[0].toLowerCase().equals("Status".toLowerCase())){
                    status= equalstr[1].toLowerCase();
                }
            }else {
                paymentcancel= "Payment Cancelled";
            }
            if (status.equals("success")){
                Toast.makeText(this,"Transaction Successfull",Toast.LENGTH_SHORT).show();
            }else if ("Payment Cancelled".equals(paymentcancel)){
                Toast.makeText(this,"Payment Cancelled",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this,"Transaction failed",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static boolean isInternetAvailable(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null){
            NetworkInfo networkInfo= connectivityManager.getActiveNetworkInfo();
            if (networkInfo.isConnected() && networkInfo.isConnectedOrConnecting() && networkInfo.isAvailable()){
                return true;
            }

        }
        return false;
    }
}