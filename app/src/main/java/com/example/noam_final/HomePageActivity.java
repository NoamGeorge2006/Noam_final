package com.example.noam_final;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
    private ImageView menu_icon;
    private ImageView plus_icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        init();
        loadCalendarFragment();
    }

    private void init() {
        menu_icon = findViewById(R.id.menu_icon);
        menu_icon.setOnClickListener(this);
        plus_icon = findViewById(R.id.plus_icon);
        plus_icon.setOnClickListener(this);
    }

    private void loadCalendarFragment() {
        CalendarFragment calendarFragment = new CalendarFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.calendar_container, calendarFragment);
        transaction.commit();
    }

    public void onClick(View v) {
        if (v.getId() == R.id.menu_icon) {
            showPopup(v);
        }

        if (v.getId() == R.id.plus_icon) {
            Intent intent = new Intent(this, AddEventActivity.class);
            startActivity(intent);
        }
    }

    private void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.menu_item);
        popup.show();
    }

    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        Intent t;
        if (id == R.id.credit) {
            t = new Intent(this, CreditActivity.class);
            startActivity(t);
        }
        if (id == R.id.contact) {
            t = new Intent(this, ContactActivity.class);
            startActivity(t);
        }
        if (id == R.id.guide) {
            t = new Intent(this, GuideActivity.class);
            startActivity(t);
        }
        return true;
    }
}
