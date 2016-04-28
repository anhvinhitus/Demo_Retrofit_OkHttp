package vn.com.vng.zalopay.ui.fragment.tabmain;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.account.ui.activities.LoginZaloActivity;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.balancetopup.ui.activity.BalanceTopupActivity;
import vn.com.vng.zalopay.domain.model.User;
import vn.com.vng.zalopay.navigation.Navigator;

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
        loadAvatarImage(mAvatarView, avatarUrl);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Bind(R.id.avatar)
    ImageView mAvatarView;

    private String avatarUrl = "https://plus.google.com/u/0/_/focus/photos/public/AIbEiAIAAABECI7LguvYhZ7MuAEiC3ZjYXJkX3Bob3RvKig0MDE5NGQ2ODRhNjU5ODJiYTgxNjkwNWU3Njk3MWI5MDA1MGJjZmRhMAGGAaoGCMD24SAz49-T4-e-nZAtIA?sz=96";

    private void loadAvatarImage(ImageView imageView, String url) {
        Glide.with(this).load(url).placeholder(R.color.background).into(imageView);
    }

    @NonNull
    @OnClick(R.id.btn_qr_code)
    public void onClickQrCode(View v) {
        navigator.startQrCodeActivity(getActivity());
    }


    @NonNull
    @OnClick(R.id.btn_card_bank)
    public void onClickCardBank(View v) {
        Timber.d("CardBank");
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
        gotoRechargeGame();
    }

    private void gotoRechargeGame() {
        Intent intent = new Intent(getActivity(), BalanceTopupActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_recharge_phone)
    public void onClickRechargePhone(View view) {
        Timber.d("Recharge.Phone");
        gotoRechargePhoneActivity();
    }

    @OnClick(R.id.btn_lixi)
    public void onClickLixi(View v) {
        Timber.d("Lixi");
    }

    @OnClick(R.id.btn_transfer)
    public void onTransferMoneyClick(View view) {
        gotoTransferActivity();
    }

    @OnClick(R.id.profile)
    public void onProfileClick(View view) {
        signout();
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
        startZMPSDKDemo();
    }

    private void startZMPSDKDemo() {
        Intent intent = new Intent(getActivity(), vn.zing.pay.trivialdrivesample.DemoSDKActivity.class);
        startActivity(intent);
    }

    private void signout() {
        ZaloSDK.Instance.unauthenticate();
        gotoLoginActivity();
    }

    private void gotoLoginActivity() {
        if (getActivity() == null) {
            return;
        }
        Intent intent = new Intent(getActivity(), LoginZaloActivity.class);
        getActivity().startActivity(intent);
        getActivity().finish();
    }
}
