package com.example.anhvinh.demo_retrofit_okhttp.View.DetailItem;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.anhvinh.demo_retrofit_okhttp.App.AppApplication;
import com.example.anhvinh.demo_retrofit_okhttp.R;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailItemFragment extends Fragment implements View.OnClickListener {

    private TextView tv_name;
    private TextView tv_population;
    private TextView tv_rank;
    private ImageView iv_flag;
    private Button bn_back;

    @Inject
    Context mContext;

    public DetailItemFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detail_item, container, false);
        init(view);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppApplication.getAppComponent().inject(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bn_back.setOnClickListener(this);
        setData();
    }

    public void init(View view) {
        tv_name = (TextView) view.findViewById(R.id.tv_country_large);
        tv_population = (TextView) view.findViewById(R.id.tv_population_large);
        tv_rank = (TextView) view.findViewById(R.id.tv_rank_large);
        iv_flag = (ImageView) view.findViewById(R.id.img_flag_large);
        bn_back = (Button) view.findViewById(R.id.btn_back);
    }

    public void setData() {
        Bundle bundle = getArguments();
        tv_name.setText(bundle.getString("NAME"));
        tv_population.setText(bundle.getString("POPULATION"));
        tv_rank.setText(bundle.getString("RANK"));
        Picasso.with(mContext)
                .load(bundle.getString("FLAG"))
                .into(iv_flag);
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.btn_back: {
                getActivity().onBackPressed();
                break;
            }
        }
    }
}
