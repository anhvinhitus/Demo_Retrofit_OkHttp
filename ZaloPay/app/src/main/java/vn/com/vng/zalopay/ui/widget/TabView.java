package vn.com.vng.zalopay.ui.widget;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.app.TabMainInformation;

/**
 * Created by AnhHieu on 4/12/16.
 */
public class TabView extends LinearLayout {


    @Bind(R.id.custom_tab_notification_mark)
    View mMarkView;

    @Bind(R.id.custom_tab_icon)
    ImageView mIconView;

    @Bind(R.id.custom_tab_title)
    TextView mTitleView;

    public TabView(Context context) {
        super(context);
        initView(context);
    }


    private void initView(Context context) {
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.tab_icon_notification, this, false);
        addView(view);
        ButterKnife.bind(this, view);
    }


    public void setIcon(int resId) {
        mIconView.setImageDrawable(ContextCompat.getDrawable(getContext(), resId));
    }

    public void setTitle(int resId) {
        mTitleView.setText(resId);
    }

    public void hideNotification() {
        mMarkView.setVisibility(View.GONE);
    }

    public void showNotification() {
        mMarkView.setVisibility(View.VISIBLE);
    }

    public void setTabInformation(TabMainInformation tab) {
        setIcon(tab.iconResId);
        setTitle(tab.titleResId);
    }

}
