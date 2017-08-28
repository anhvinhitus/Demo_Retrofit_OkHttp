package com.example.anhvinh.demo_retrofit_okhttp.View.DetailItem;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.example.anhvinh.demo_retrofit_okhttp.R;

public class DetaiItemActivity extends AppCompatActivity {

    private Fragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detai_item);
        addFragment();
    }

    public void addFragment() {
        fragment = new DetailItemFragment();
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        Bundle bundle = new Bundle(getData());
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction().add(R.id.frame_detail, fragment).commit();
    }

    public Bundle getData() {
        return getIntent().getExtras();
    }
}
