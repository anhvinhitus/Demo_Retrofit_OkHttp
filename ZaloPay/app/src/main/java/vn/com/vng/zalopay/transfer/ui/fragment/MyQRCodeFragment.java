package vn.com.vng.zalopay.transfer.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import javax.inject.Inject;

import butterknife.BindView;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.PersonTransfer;
import vn.com.vng.zalopay.transfer.ui.activities.SetAmountActivity;
import vn.com.vng.zalopay.transfer.ui.adapter.PersonTransferAdapter;
import vn.com.vng.zalopay.transfer.ui.presenter.ReceiveMoneyPresenter;
import vn.com.vng.zalopay.transfer.ui.view.IReceiveMoneyView;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.CurrencyUtil;

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

    @BindView(R.id.tvMessageSender)
    TextView tvMessageSenderView;

    @BindView(R.id.avatarSender)
    ImageView imageAvatarSenderView;

    @BindView(R.id.layoutSender)
    View layoutUserTransfer;

    @BindView(R.id.tvMoney)
    TextView mMoneyChangeSuccess;

    @Inject
    ReceiveMoneyPresenter mPresenter;

    @BindView(R.id.container)
    ViewGroup mContainerView;

    @BindView(R.id.listview)
    RecyclerView mListView;

    PersonTransferAdapter mAdapter;

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
        mAdapter = new PersonTransferAdapter(getContext());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated");
        mListView.setHasFixedSize(true);
        mListView.setLayoutManager(new LinearLayoutManager(getContext()));
        mListView.setAdapter(mAdapter);
        mListView.setNestedScrollingEnabled(false);
        mListView.getLayoutManager().setAutoMeasureEnabled(true);

        mMyQrCodeView.setImageResource(R.color.silver);
        mPresenter.setView(this);
        mPresenter.onViewCreated();
        setAmount(0);
        setNote("");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
                        mPresenter.updateQRWithAmount(amount, message);
                    }

                    break;
            }
        }
    }

    private void setAmount(long amount) {
        tvAmountView.setText(CurrencyUtil.spanFormatCurrency(amount));
        tvAmountView.setVisibility(amount <= 0 ? View.GONE : View.VISIBLE);
    }

    private void setNote(String message) {
        tvNoteView.setText(message);
        tvNoteView.setVisibility(TextUtils.isEmpty(message) ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.receiver_money, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        if (itemId == R.id.action_amount) {
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
        layoutSuccess.removeCallbacks(mRunnable);
        mPresenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }

    private void loadImage(ImageView imageView, String url) {
        Glide.with(getActivity()).load(url)
                .placeholder(R.color.silver)
                .error(R.drawable.ic_avatar_default)
                .centerCrop()
                .dontAnimate()
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
       /* layoutQrcode.setVisibility(View.INVISIBLE);
        layoutSuccess.setVisibility(View.VISIBLE);*/
    }

    @Override
    public void setReceiverInfo(String zaloPayId, String displayName, String avatar) {
        setTransferUserInfo(String.format("%s đang chuyển tiền ...", displayName), avatar);
    }

    @Override
    public void setReceivedMoney(String zaloPayId, String displayName, String avatar, long amount) {
        setTransferUserInfo(String.format("%s đã chuyển tiền thành công.", displayName), avatar);
        setResult(true, amount);
        addAdapter(displayName, avatar, amount);
        layoutSuccess.postDelayed(mRunnable, 5000);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            layoutQrcode.setVisibility(View.VISIBLE);
            layoutSuccess.setVisibility(View.GONE);


            /*
            layoutSuccess.animate()
                    .translationY(0)
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            layoutSuccess.setVisibility(View.GONE);
                        }
                    });*/
        }
    };

    @Override
    public void setReceivedMoneyFail(String zaloPayId, String displayName, String avatar) {
        // setResult(false, 0);
        setTransferUserInfo(String.format("%s đã chuyển tiền thất bại.", displayName), avatar);
    }

    @Override
    public void setReceivedMoneyCancel(String zaloPayId, String displayName, String avatar) {
        setTransferUserInfo(String.format("%s đã huỷ chuyển tiền.", displayName), avatar);
        //setResult(false, 0);
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

    private void setTransferUserInfo(String message, String avatar) {
        if (layoutUserTransfer != null) {
            layoutUserTransfer.setVisibility(View.VISIBLE);
        }

        if (imageAvatarSenderView != null) {
            loadImage(imageAvatarSenderView, avatar);
        }

        if (tvMessageSenderView != null) {
            tvMessageSenderView.setText(message);
        }
    }

    private void setResult(boolean success, long amount) {
      /*  if (layoutQrcode != null) {
            layoutQrcode.setVisibility(View.INVISIBLE);
        }*/

        if (layoutSuccess != null) {
            layoutSuccess.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.simple_grow);
            layoutSuccess.startAnimation(animation);
        }

        if (mMoneyChangeSuccess != null) {
            mMoneyChangeSuccess.setTextColor(ContextCompat.getColor(getContext(), success ? R.color.green : R.color.red));
            mMoneyChangeSuccess.setText(CurrencyUtil.spanFormatCurrency(amount));
            mMoneyChangeSuccess.setCompoundDrawablesWithIntrinsicBounds(success ? R.drawable.ic_thanhcong_24dp : R.drawable.ic_thatbai_24dp, 0, 0, 0);
        }
    }

    private void addAdapter(String name, String avatar, long amount) {
        PersonTransfer item = new PersonTransfer();
        item.avatar = avatar;
        item.displayName = name;
        item.amount = amount;
        mAdapter.insert(item, 0);
        mContainerView.requestLayout();
    }
}
