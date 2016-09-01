package vn.com.vng.zalopay.transfer.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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

/**
 * Created by AnhHieu on 8/25/16.
 * QR Code for receiving money
 */
public class MyQRCodeFragment extends BaseFragment implements IReceiveMoneyView {

    @Inject
    ReceiveMoneyPresenter mPresenter;

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
        mAdapter = new PersonTransferAdapter(getContext(), userConfig.getCurrentUser());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated");
        mListView.setLayoutManager(new LinearLayoutManager(getContext()));
        mListView.setAdapter(mAdapter);
        mPresenter.setView(this);
        mPresenter.onViewCreated();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setAmount(0);
        setNote("");
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
        if (getHeaderView() != null) {
            getHeaderView().setAmount(amount);
        }
    }

    private void setNote(String message) {
        if (getHeaderView() != null) {
            getHeaderView().setNote(message);
        }
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
        mPresenter.destroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mPresenter.destroy();
        super.onDestroy();
    }

    @Override
    public void setQrImage(Bitmap image) {
        if (getHeaderView() != null) {
            getHeaderView().setQrImage(image);
        }
    }

    @Override
    public void setUserInfo(String displayName, String avatar) {
        if (getHeaderView() != null) {
            getHeaderView().setUserInfo(displayName, avatar);
        }
    }

    @Override
    public void displayWaitForMoney() {
        if (getHeaderView() != null) {
            getHeaderView().displayWaitForMoney();
        }
    }

    @Override
    public void displayReceivedMoney(long amount) {
        if (getHeaderView() != null) {
            getHeaderView().displayReceivedMoney(amount);
        }
    }

    @Override
    public void setReceiverInfo(String uid, String displayName, String avatar, int state, long amount) {
        addItem(uid, displayName, avatar, state, amount);
        if (mAdapter.getItemCount() >= 3) {
            if (getHeaderView() != null) {
                getHeaderView().showTotalView();
            }
        }
    }


    @Override
    public void showLoading() {
        showProgressDialog();
    }

    @Override
    public void hideLoading() {
        hideProgressDialog();
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

    private void addItem(String uid, String name, String avatar, int state, long amount) {
        PersonTransfer item = new PersonTransfer();
        item.avatar = avatar;
        item.zaloPayId = uid;
        item.state = state;
        item.displayName = name;
        item.amount = amount;
        mAdapter.insert(item, 1);
    }

    public PersonTransferAdapter.HeaderViewHolder getHeaderView() {
        try {
            return (PersonTransferAdapter.HeaderViewHolder) mListView.findViewHolderForAdapterPosition(0);
        } catch (Exception e) {
            Timber.d(e, "getHeaderView");
        }
        return null;
    }


}
