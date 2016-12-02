package vn.com.vng.zalopay.transfer.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.zalopay.ui.widget.edittext.ZPEditText;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.ui.widget.MoneyEditText;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;
import vn.com.zalopay.wallet.merchant.CShareData;

/**
 * Created by AnhHieu on 8/29/16.
 * *
 */
public class SetAmountFragment extends BaseFragment {

    public static SetAmountFragment newInstance() {

        Bundle args = new Bundle();

        SetAmountFragment fragment = new SetAmountFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_set_amount;
    }

    @BindView(R.id.edtAmount)
    MoneyEditText mAmountView;

    @BindView(R.id.edtNote)
    ZPEditText mNoteView;

    @BindView(R.id.btnUpdate)
    View mBtnContinueView;

    @OnTextChanged(value = R.id.edtAmount, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onTextChanged(CharSequence s) {
        mBtnContinueView.setEnabled(mAmountView.isValid());
    }

    @OnClick(R.id.btnUpdate)
    public void onClickUpdate() {
        trackEventSetAmount();

        Intent data = new Intent();
        data.putExtra("amount", mAmountView.getAmount());
        data.putExtra("message", mNoteView.getText().toString());
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initLimitAmount();
        mBtnContinueView.setEnabled(mAmountView.isValid());
    }

    private void initLimitAmount() {
        long mMinAmount = 0;
        long mMaxAmount = 0;
        try {
            mMinAmount = CShareData.getInstance().getMinTranferValue();
            mMaxAmount = CShareData.getInstance().getMaxTranferValue();
        } catch (Exception e) {
            Timber.w(e, "Get min/max deposit from paymentSDK exception: [%s]", e.getMessage());
        }
        if (mMinAmount <= 0) {
            mMinAmount = Constants.MIN_TRANSFER_MONEY;
        }
        if (mMaxAmount <= 0) {
            mMaxAmount = Constants.MAX_TRANSFER_MONEY;
        }
        mAmountView.setMinMaxMoney(mMinAmount, mMaxAmount);
    }

    private void trackEventSetAmount() {
        if (mAmountView.getAmount() > 0 && mNoteView.length() > 0) {
            ZPAnalytics.trackEvent(ZPEvents.RECEIVEMONEY_SETAMOUNTMESSAGE);
        } else if (mAmountView.getAmount() > 0) {
            ZPAnalytics.trackEvent(ZPEvents.RECEIVEMONEY_SETAMOUNT);
        } else if (mNoteView.length() > 0) {
            ZPAnalytics.trackEvent(ZPEvents.RECEIVEMONEY_SETMESSAGE);
        }
    }
}
