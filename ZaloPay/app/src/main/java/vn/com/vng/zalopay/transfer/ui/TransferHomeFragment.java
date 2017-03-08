package vn.com.vng.zalopay.transfer.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;
import vn.com.vng.zalopay.BuildConfig;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.appresources.ResourceHelper;
import vn.com.vng.zalopay.domain.model.Person;
import vn.com.vng.zalopay.domain.model.RecentTransaction;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.listener.ZPWOnSweetDialogListener;

/**
 * A fragment representing a list of Items.
 * <p>
 */
public class TransferHomeFragment extends BaseFragment implements
        TransferRecentAdapter.OnClickTransferRecentListener, ITransferHomeView {


    @Inject
    TransferHomePresenter presenter;

    private TransferRecentAdapter mAdapter;

    @BindView(R.id.tvTileTransactionRecent)
    View mTvTileTransactionRecent;

    @BindView(R.id.list)
    RecyclerView mList;

    @BindView(R.id.viewSeparate)
    View viewSeparate;

    @BindView(R.id.layoutIntroduction)
    View layoutIntroduction;

    @BindView(R.id.imgIntroduction)
    ImageView imgIntroduction;

    @OnClick(R.id.layoutTransferAccZaloPay)
    public void onClickTransferAccZaloPay() {
        navigator.startZaloContactActivity(this);
    }

    @OnClick(R.id.layoutTransferViaAccount)
    public void onClickTransferViaAccountName() {
        navigator.startTransferViaAccountName(this);
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TransferHomeFragment() {
    }

    @SuppressWarnings("unused")
    public static TransferHomeFragment newInstance() {
        TransferHomeFragment fragment = new TransferHomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_transfer_home;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new TransferRecentAdapter(getContext(), this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.attachView(this);

        mList.setLayoutManager(new LinearLayoutManager(getContext()));
        mList.setHasFixedSize(true);
        mList.setAdapter(mAdapter);


        mTvTileTransactionRecent.setVisibility(View.GONE);
        mList.setVisibility(View.GONE);
        layoutIntroduction.setVisibility(View.VISIBLE);

        loadAnimationFromResource();
    }

    private void loadAnimationFromResource() {
        AnimationDrawable animationDrawable = null;
        try {
            Drawable frame1 = Drawable.createFromPath(ResourceHelper.getResource(getContext(),
                    BuildConfig.ZALOPAY_APP_ID, getString(R.string.ic_chuyentien_ani_1)));
            Drawable frame2 = Drawable.createFromPath(ResourceHelper.getResource(getContext(),
                    BuildConfig.ZALOPAY_APP_ID, getString(R.string.ic_chuyentien_ani_2)));
            Drawable frame3 = Drawable.createFromPath(ResourceHelper.getResource(getContext(),
                    BuildConfig.ZALOPAY_APP_ID, getString(R.string.ic_chuyentien_ani_3)));
            Drawable frame4 = Drawable.createFromPath(ResourceHelper.getResource(getContext(),
                    BuildConfig.ZALOPAY_APP_ID, getString(R.string.ic_chuyentien_ani_4)));

            if (frame1 == null || frame2 == null || frame3 == null || frame4 == null) {
                return;
            }
            animationDrawable = new AnimationDrawable();
            animationDrawable.addFrame(frame1, 1000);
            animationDrawable.addFrame(frame2, 100);
            animationDrawable.addFrame(frame3, 100);
            animationDrawable.addFrame(frame4, 2000);
        } catch (Exception e) {
            Timber.e(e, "Load animation from resource throw exception.");
        }

        if (animationDrawable == null) {
            return;
        }
        animationDrawable.setOneShot(false);
        imgIntroduction.setBackground(animationDrawable);
        animationDrawable.start();
        Timber.d("Load & start animation from resource successfully.");
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.resume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDestroyView() {
        presenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
    public void showError(String message) {
        showErrorDialog(message);
    }


    @Override
    public void setData(List<RecentTransaction> list) {
        mAdapter.setData(list);
        checkIfEmpty();
    }

    private void checkIfEmpty() {
        if (mAdapter.getItemCount() > 0) {
            mTvTileTransactionRecent.setVisibility(View.VISIBLE);
            mList.setVisibility(View.VISIBLE);
            viewSeparate.setVisibility(View.VISIBLE);
            layoutIntroduction.setVisibility(View.GONE);
            imgIntroduction.clearAnimation();
        } else {
            mTvTileTransactionRecent.setVisibility(View.GONE);
            mList.setVisibility(View.GONE);
            viewSeparate.setVisibility(View.GONE);
            layoutIntroduction.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onGetProfileSuccess(Person person, String zaloPayName) {
        RecentTransaction item = new RecentTransaction();
        item.avatar = person.avatar;
        item.zaloPayId = person.zaloPayId;
        item.displayName = person.displayName;
        item.phoneNumber = String.valueOf(person.phonenumber);
        item.zaloPayName = zaloPayName;
        onItemRecentClick(item);
    }

    @Override
    public void onItemRecentClick(RecentTransaction item) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.ARG_TRANSFERRECENT, item);
        navigator.startTransferActivity(this, bundle);
        ZPAnalytics.trackEvent(ZPEvents.MONEYTRANSFER_CHOOSERECENTTRANSACTION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_TRANSFER) {
            if (resultCode == Activity.RESULT_OK) {
                getActivity().finish();
                return;
            }
        }
        if (requestCode == Constants.REQUEST_CODE_TRANSFER_VIA_ZALOPAYID) {
            if (resultCode == Activity.RESULT_OK) {
                getActivity().finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
