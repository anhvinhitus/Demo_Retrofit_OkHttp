package vn.com.zalopay.wallet.dialog;

import android.text.Html;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.zalopay.ui.widget.dialog.listener.ZPWOnCloseDialogListener;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.view.adapter.CardSupportAdapter;
import vn.com.zalopay.wallet.view.adapter.CreditCardSupportGridViewAdapter;

public class BankListPopup extends BasePaymentDialogActivity {

    protected static WeakReference<ZPWOnCloseDialogListener> mCloseCardSupportDialogListener;
    protected static WeakReference<CardSupportAdapter> mCardSupportGridViewAdapter;
    protected GridView mGridViewBank;
    protected TextView txtLabel;
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
        txtLabel = (TextView) findViewById(R.id.cardsupport_label);
        mRippleButtonSelectBank = findViewById(R.id.rippleButtonSelectBank);
        mRippleButtonSelectBank.setOnClickListener(view -> onBackPressed());
    }

    @Override
    protected void initData() {
        try {
            if (getAdapter() != null) {
                mGridViewBank.setAdapter(getAdapter());
            }

            if (GlobalData.transtype() == TransactionType.LINK) {
                txtLabel.setText(Html.fromHtml(GlobalData.getStringResource(RS.string.zpw_string_title_select_card)));
            } else if (getAdapter() instanceof CreditCardSupportGridViewAdapter) {
                txtLabel.setText(Html.fromHtml(GlobalData.getStringResource(RS.string.zpw_string_title_select_card)));
            } else {
                txtLabel.setText(GlobalData.getStringResource(RS.string.zpw_string_title_select_bank));
            }
        } catch (Exception e) {
            Timber.d("get card support Adapter: [%s]", e);
        }

    }
}



