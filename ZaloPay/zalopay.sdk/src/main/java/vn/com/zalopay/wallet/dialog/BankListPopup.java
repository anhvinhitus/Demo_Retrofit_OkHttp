package vn.com.zalopay.wallet.dialog;

import android.view.View;
import android.widget.GridView;

import com.zalopay.ui.widget.dialog.listener.ZPWOnCloseDialogListener;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.view.adapter.CardSupportAdapter;

public class BankListPopup extends BasePaymentDialogActivity {

    protected static WeakReference<ZPWOnCloseDialogListener> mCloseCardSupportDialogListener;
    protected static WeakReference<CardSupportAdapter> mCardSupportGridViewAdapter;
    protected GridView mGridViewBank;
    protected View mRippleButtonSelectBank;

    public static void setCloseDialogListener(ZPWOnCloseDialogListener pListener) {
        BankListPopup.mCloseCardSupportDialogListener = new WeakReference<>(pListener);
    }

    public ZPWOnCloseDialogListener getListener() {
        return BankListPopup.mCloseCardSupportDialogListener.get();
    }

    public CardSupportAdapter getAdapter() throws Exception {
        if (BankListPopup.mCardSupportGridViewAdapter.get() == null) {
            throw new IllegalAccessException("mCardSupportGridViewAdapter is null");
        }
        return BankListPopup.mCardSupportGridViewAdapter.get();
    }

    public static void setAdapter(CardSupportAdapter pAdapter) {
        BankListPopup.mCardSupportGridViewAdapter = new WeakReference<>(pAdapter);
    }

    public void onBackPressed() {
        super.onBackPressed();
        if (getListener() != null) {
            getListener().onCloseCardSupportDialog();
        }
    }

    @Override
    protected void getArguments() {
    }

    @Override
    protected int getLayout() {
        return R.layout.screen__bank__list;
    }

    @Override
    protected void initViews() {
        mGridViewBank = (GridView) findViewById(R.id.gridViewCard);
        mRippleButtonSelectBank = findViewById(R.id.rippleButtonSelectBank);
        mRippleButtonSelectBank.setOnClickListener(view -> onBackPressed());
    }

    @Override
    protected void initData() {
        try {
            if (getAdapter() != null) {
                mGridViewBank.setAdapter(getAdapter());
            }
        } catch (Exception e) {
            Timber.d("get card support Adapter: [%s]", e);
        }

    }
}



