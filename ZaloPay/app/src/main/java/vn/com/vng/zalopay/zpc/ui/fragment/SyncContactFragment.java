package vn.com.vng.zalopay.zpc.ui.fragment;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zalopay.ui.widget.IconFont;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.RuntimePermissionFragment;
import vn.com.vng.zalopay.utils.ToastUtil;
import vn.com.vng.zalopay.zpc.ui.presenter.SyncContactPresenter;
import vn.com.vng.zalopay.zpc.ui.view.ISyncContactView;

/**
 * Created by hieuvm on 7/21/17.
 * *
 */

public class SyncContactFragment extends RuntimePermissionFragment implements ISyncContactView {

    @BindView(R.id.countUcb)
    TextView mCountUcbView;
    @BindView(R.id.avatarArrow)
    IconFont mAvatarArrow;
    @BindView(R.id.countZfl)
    TextView mCountZflView;
    @BindView(R.id.btnUpdate)
    Button mBtnUpdate;
    @BindView(R.id.tvTimeUpdate)
    TextView mTimeView;
    @BindView(R.id.tvPermission)
    View mPermissionView;
    @BindView(R.id.layoutContact)
    View mLayoutContact;
    @Inject
    SyncContactPresenter mPresenter;

    public static SyncContactFragment newInstance() {

        Bundle args = new Bundle();

        SyncContactFragment fragment = new SyncContactFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void permissionGranted(int permissionRequestCode, boolean isGranted) {
        if (!isGranted) {
            return;
        }

        switch (permissionRequestCode) {
            case PERMISSION_CODE.READ_CONTACTS:
                mPermissionView.setVisibility(View.GONE);
                mLayoutContact.setEnabled(false);
                mPresenter.syncContact();
                break;
        }
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_sync_contact;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        boolean isReadContact = isPermissionGranted(Manifest.permission.READ_CONTACTS);
        mPermissionView.setVisibility(isReadContact ? View.GONE : View.VISIBLE);
        mLayoutContact.setEnabled(!isReadContact);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPresenter.loadView();
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void showLoading() {
        super.showProgressDialog();
    }

    @Override
    public void hideLoading() {
        super.hideProgressDialog();
    }

    @Override
    public void showError(String message) {
        showErrorDialog(message);
    }

    @Override
    public void hideContactBookCount() {
        mCountUcbView.setVisibility(View.GONE);
    }

    @Override
    public void setContactBookCount(long count) {
        mCountUcbView.setVisibility(View.VISIBLE);
        mCountUcbView.setText(String.valueOf(count));
    }

    @Override
    public void showAvatarArrow() {
        mAvatarArrow.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideAvatarArrow() {
        mAvatarArrow.setVisibility(View.GONE);
    }

    @Override
    public void setFriendListCount(long count) {
        mCountZflView.setText(String.valueOf(count));
    }

    @Override
    public void setLastTimeSyncContact(String timestamp) {
        mTimeView.setText(timestamp);
    }

    @Override
    public void showSyncContactSuccess() {
        ToastUtil.showCustomToast(getContext(), getString(R.string.already_update));
    }

    @OnClick(R.id.btnUpdate)
    public void onViewClicked() {
        if (isPermissionGrantedAndRequest(Manifest.permission.READ_CONTACTS, PERMISSION_CODE.READ_CONTACTS)) {
            mPresenter.syncContact();
        }
    }

    @OnClick(R.id.layoutContact)
    public void onClickLayoutContact(){
        if (isPermissionGrantedAndRequest(Manifest.permission.READ_CONTACTS, PERMISSION_CODE.READ_CONTACTS)) {
            mPresenter.syncContact();
        }
    }
}
