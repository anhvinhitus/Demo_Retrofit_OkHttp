package vn.com.vng.zalopay.ui.fragment.tabmain;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.navigation.Navigator;
import vn.com.vng.zalopay.ui.activity.MiniApplicationActivity;

/**
 * Created by AnhHieu on 4/11/16.
 */
public class ZaloPayFragment extends BaseMainFragment {

    public static ZaloPayFragment newInstance() {
        Bundle args = new Bundle();
        ZaloPayFragment fragment = new ZaloPayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onScreenVisible() {
    }

    @Inject
    Navigator navigator;

    @Inject
    User user;

    /* Advertisement START */
    @Bind(R.id.imgAdsBanner)
    ImageView mImgAdsBanner;

    @Bind(R.id.layoutAdsSub)
    View mLayoutAdsSub;

    @Bind(R.id.tvAdsSubContent)
    TextView mTvAdsSubContent;
    /* Advertisement END */

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @Override
    protected void setupFragmentComponent() {
        AndroidApplication.instance().getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_zalopay;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showAdsBanner("http://dangky3g.com.vn/wp-content/uploads/2015/09/vinaphone-khuyen-mai-25-9.png");
        showAdsSub("Mobi khuyến mại <b>50%. Nạp ngay hôm nay!");
        initReactNativeApps();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @NonNull
    @OnClick(R.id.btn_transfer)
    public void onClickTransfer(View v) {
        Timber.d("Transfer");
    }

    @NonNull
    @OnClick(R.id.btn_recharge_game)
    public void onClickRechargeGame(View v) {
        Timber.d("Recharge.Game");
//        gotoRechargeGame();
    }

//    private void gotoRechargeGame() {
//        Intent intent = new Intent(getActivity(), BalanceTopupActivity.class);
//        startActivity(intent);
//    }

    @OnClick(R.id.btn_recharge_phone)
    public void onClickRechargePhone(View view) {
        Timber.d("Recharge.Phone");
        gotoRechargePhoneActivity();
    }

    @OnClick(R.id.btn_lixi)
    public void onClickLixi(View v) {
        Timber.d("Lixi");
        Intent intent = new Intent(this.getContext(), MiniApplicationActivity.class);
        this.getActivity().startActivity(intent);
    }

    @OnClick(R.id.btn_transfer)
    public void onTransferMoneyClick(View view) {
        gotoTransferActivity();
    }

    private void gotoTransferActivity() {
    }

    private void gotoRechargePhoneActivity() {
//        Intent intent = new Intent(getActivity(), BuyTelCardActivity.class);
//        intent.putExtra(vn.com.vng.zalopay.scratchcard.network.Constants.TEL_CARD_TYPE, TelCardUtil.VIETTEL);
//        startActivity(intent);
    }

    @OnClick(R.id.others)
    public void onLayoutOthersClick() {
//        ShaUtils.getSha();
    }

    /* Show|Hide Banner START */
    public void showAdsBanner(String url) {
        if (TextUtils.isEmpty(url)) {
            hideAdsBanner();
        } else {
            Glide.with(this).load(url).asBitmap().into(mImgAdsBanner);
            mImgAdsBanner.setVisibility(View.VISIBLE);
        }
    }

    public void hideAdsBanner() {
        if (mImgAdsBanner!=null) {
            mImgAdsBanner.setVisibility(View.GONE);
        }
    }

    public void showAdsSub(String content) {
        if (TextUtils.isEmpty(content)) {
            hideAdsBanner();
        } else {
            mTvAdsSubContent.setText(Html.fromHtml(content));
            mLayoutAdsSub.setVisibility(View.VISIBLE);
        }
    }

    public void hideAdsSub() {
        if (mLayoutAdsSub!=null) {
            mLayoutAdsSub.setVisibility(View.GONE);
        }
    }
    /* Show|Hide Banner END */


    private void initReactNativeApps() {

    }
}
