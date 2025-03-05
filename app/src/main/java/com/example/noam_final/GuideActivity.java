package com.example.noam_final;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GuideActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvResult;
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
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
            is = getResources().openRawResource(R.raw.guide);
            isr = new InputStreamReader(is, "UTF8");

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