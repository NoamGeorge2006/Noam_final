package com.example.noam_final;

import android.os.Bundle;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.widget.ImageView;

public class MenuActivity extends AppCompatActivity {
    private TextView tVcredit, tVguide;
    private ImageView creditImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        tVcredit = findViewById(R.id.credit);
        tVguide = findViewById(R.id.guide);
        creditImage = findViewById(R.id.creditImage);
    }

    private void loadFileIntoTextView(int rawResourceId, TextView textView) {
        StringBuilder sB = new StringBuilder();
        try (InputStream is = getResources().openRawResource(rawResourceId);
             InputStreamReader iSR = new InputStreamReader(is);
             BufferedReader bR = new BufferedReader(iSR)) {

            String line;
            while ((line = bR.readLine()) != null) {
                sB.append(line).append('\n');
            }
            textView.setText(sB.toString());
        } catch (Exception e) {
            e.printStackTrace();
            textView.setText("Error reading file");
        }
    }


    public void onShowCreditClicked(View view) {
        tVguide.setVisibility(View.GONE);
        tVcredit.setVisibility(View.VISIBLE);
        creditImage.setVisibility(View.VISIBLE);
        loadFileIntoTextView(R.raw.credit, tVcredit);
    }

    public void onShowGuideClicked(View view) {
        tVcredit.setVisibility(View.GONE);
        tVguide.setVisibility(View.VISIBLE);
        creditImage.setVisibility(View.GONE);
        loadFileIntoTextView(R.raw.guide, tVguide);
    }
}
