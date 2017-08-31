package com.example.anhvinh.demo_retrofit_okhttp.View;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anhvinh.demo_retrofit_okhttp.Adapter.WorldAdapter;
import com.example.anhvinh.demo_retrofit_okhttp.App.AppApplication;
import com.example.anhvinh.demo_retrofit_okhttp.Models.Entity.Worldpopulation;
import com.example.anhvinh.demo_retrofit_okhttp.Presenter.List_Country_Interator;
import com.example.anhvinh.demo_retrofit_okhttp.Presenter.List_Country_Presenter;
import com.example.anhvinh.demo_retrofit_okhttp.R;
import com.example.anhvinh.demo_retrofit_okhttp.View.Base.List_Country_View;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity implements List_Country_View, View.OnClickListener {
    public final String EXTRA_LIST_COUNTRY = "extrac_list_country";
    public Boolean isInternet = false;
    // Declare View on Main activity:
    private List<Worldpopulation> listCountry;
    private RecyclerView recyclerView;
    private WorldAdapter adapter;
    private Button btn_reload;
    private TextView tv_error;



    // Inject:
    @Inject List_Country_Presenter presenter;
    @Inject List_Country_Interator view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppApplication.getAppComponent().inject(this);
        isInternet = checkInternetConnection();
        init();

        // Save Instance:
        if (savedInstanceState != null) {
            listCountry = savedInstanceState.getParcelableArrayList(EXTRA_LIST_COUNTRY);
        }
        if (presenter == null) {
            Log.d("Main activity", "Presenter null");
            view.setListCountry(listCountry);
        }
        // Get data"
        presenter.setView(this);
        presenter.getData(false, isInternet);
        btn_reload.setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.saveData();
    }

    @Override
    public void showError() {
        tv_error.setVisibility(View.VISIBLE);
        tv_error.setText("Load Data Error");
        btn_reload.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void showData(List<Worldpopulation> listCountry) {
        tv_error.setVisibility(View.GONE);
        btn_reload.setVisibility(View.GONE);
        this.listCountry = (ArrayList<Worldpopulation>) listCountry;
        adapter = new WorldAdapter(listCountry);
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showNoData() {
        tv_error.setVisibility(View.VISIBLE);
        tv_error.setText("No data to show");
        btn_reload.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void reLoad() {
        tv_error.setVisibility(View.GONE);
        btn_reload.setVisibility(View.GONE);
    }

    private boolean checkInternetConnection() {

        ConnectivityManager connManager =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

        if (networkInfo == null) {
            Toast.makeText(this, "No default network is currently active", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!networkInfo.isConnected()) {
            Toast.makeText(this, "Network is not connected", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!networkInfo.isAvailable()) {
            Toast.makeText(this, "Network not available", Toast.LENGTH_LONG).show();
            return false;
        }
        Toast.makeText(this, "Network OK", Toast.LENGTH_LONG).show();
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_reload:
                presenter.reLoad(isInternet);
                break;
        }
    }

    public void init() {
        // Mapping View to Id:
        recyclerView    = (RecyclerView) findViewById(R.id.rv_listcountry);
        btn_reload      = (Button) findViewById(R.id.btn_reload);
        tv_error        = (TextView) findViewById(R.id.tv_nodata);

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }
}
