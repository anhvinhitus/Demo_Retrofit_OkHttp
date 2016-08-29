package vn.com.vng.zalopay.transfer.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.transfer.ui.activities.SetAmountActivity;
import vn.com.vng.zalopay.transfer.ui.presenter.ReceiveMoneyPresenter;
import vn.com.vng.zalopay.transfer.ui.view.IReceiveMoneyView;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;

/**
 * Created by AnhHieu on 8/25/16.
 * QR Code for receiving money
 */
public class MyQRCodeFragment extends BaseFragment implements IReceiveMoneyView {

    @BindView(R.id.imageViewQrCode)
    ImageView mMyQrCodeView;
    @BindView(R.id.imageAvatar)
    ImageView mImageAvatarView;
    @BindView(R.id.layoutQrCode)
    View layoutQrcode;
    @BindView(R.id.layoutSuccess)
    View layoutSuccess;
    @BindView(R.id.tvName)
    TextView mNameView;
    @BindView(R.id.imageAvatarLarge)
    ImageView imageAvatarLarge;

    @BindView(R.id.tvAmount)
    TextView tvAmountView;

    @BindView(R.id.tvMessage)
    TextView tvNoteView;

    @Inject
    ReceiveMoneyPresenter mPresenter;

    public static MyQRCodeFragment newInstance() {

        Bundle args = new Bundle();

        MyQRCodeFragment fragment = new MyQRCodeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_my_qr_code;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated");
        mMyQrCodeView.setImageResource(R.color.silver);
        mPresenter.setView(this);
        mPresenter.onViewCreated();
        setAmount(0);
        setNote("");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Timber.d("onActivityCreated");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case 100:
                    if (data != null && data.getExtras() != null) {
                        String message = data.getExtras().getString("message");
                        long amount = data.getExtras().getLong("amount");
                        Timber.d("onActivityResult: message %s amount %s", message, amount);
                        setAmount(amount);
                        setNote(message);
                    }

                    break;
            }
        }
    }

    private void setAmount(long amount) {

        tvAmountView.setText(String.valueOf(amount));
        tvAmountView.setVisibility(amount <= 0 ? View.GONE : View.VISIBLE);
    }

    private void setNote(String message) {
        tvNoteView.setText(message);
        tvNoteView.setVisibility(TextUtils.isEmpty(message) ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_intro, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.action_intro) {
            startActivityForResult(new Intent(getContext(), SetAmountActivity.class), 100);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        mPresenter.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.resume();
    }

    @Override
    public void onDestroyView() {
        mPresenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }

    private void loadImage(final ImageView imageView, String url) {
        Glide.with(this).load(url)
                .placeholder(R.color.silver)
                .error(R.drawable.ic_avatar_default)
                .centerCrop()
                .into(imageView);
    }


    @Override
    public void setQrImage(Bitmap image) {
        if (mMyQrCodeView == null) {
            return;
        }

        if (image != null) {
            mMyQrCodeView.setImageBitmap(image);
        }
    }

    @Override
    public void setUserInfo(String displayName, String avatar) {
        loadImage(mImageAvatarView, avatar);
        loadImage(imageAvatarLarge, avatar);
        mNameView.setText(displayName);
    }

    @Override
    public void displayWaitForMoney() {
        layoutQrcode.setVisibility(View.VISIBLE);
        layoutSuccess.setVisibility(View.GONE);
    }

    @Override
    public void displayReceivedMoney() {
        layoutQrcode.setVisibility(View.INVISIBLE);
        layoutSuccess.setVisibility(View.VISIBLE);
    }

    @Override
    public void showLoading() {
        showProgressDialog();
    }

    @Override
    public void hideLoading() {
        showProgressDialog();
    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {
        Toast.makeText(getContext(), "Sinh mã QR thất bại!", Toast.LENGTH_SHORT).show();
    }
}
