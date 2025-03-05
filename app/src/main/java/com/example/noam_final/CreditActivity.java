package com.example.noam_final;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CreditActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tvResult;
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit);
        init();
        readFile();
    }

    private void init(){
        ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(this);
        tvResult = findViewById(R.id.tvResult);
    }
    private void readFile()  {
        InputStream is = null;
        InputStreamReader isr = null;
        try
        {
            is = getResources().openRawResource(R.raw.credit);
            isr = new InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8);

            BufferedReader reader = new BufferedReader(isr);
            String strResult = ""; // ignore first line containing headers
            String strLine = "";

            while ((strLine = reader.readLine()) != null)
            {
                strResult += strLine;
            }
            tvResult.setText(strResult);
            reader.close();
            isr.close();
            is.close();

        } catch (Exception e)
        {
            Log.e("ReadFromFile", "Error reading from file: persons.txt");
            e.printStackTrace();
        }
    }
    @Override
    public void onClick(View view) {
        if (view == ivBack) {
            finish();
        }
    }
}