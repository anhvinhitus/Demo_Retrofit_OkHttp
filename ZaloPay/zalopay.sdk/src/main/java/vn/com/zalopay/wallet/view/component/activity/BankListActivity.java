package vn.com.zalopay.wallet.view.component.activity;

import android.text.Html;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.listener.ZPWOnCloseDialogListener;
import vn.com.zalopay.wallet.view.adapter.CardSupportAdapter;
import vn.com.zalopay.wallet.view.adapter.CreditCardSupportGridViewAdapter;

public class BankListActivity extends BasePaymentDialogActivity {

    protected static WeakReference<ZPWOnCloseDialogListener> mCloseCardSupportDialogListener;
    protected static WeakReference<CardSupportAdapter> mCardSupportGridViewAdapter;
    protected GridView mGridViewBank;
    protected TextView txtLabel;
    protected View mRippleButtonSelectBank;

    public static void setCloseDialogListener(ZPWOnCloseDialogListener pListener) {
        BankListActivity.mCloseCardSupportDialogListener = new WeakReference<ZPWOnCloseDialogListener>(pListener);
    }

    public ZPWOnCloseDialogListener getListener() {
        return BankListActivity.mCloseCardSupportDialogListener.get();
    }

    public CardSupportAdapter getAdapter() {
        return BankListActivity.mCardSupportGridViewAdapter.get();
    }

    public static void setAdapter(CardSupportAdapter pAdapter) {
        BankListActivity.mCardSupportGridViewAdapter = new WeakReference<CardSupportAdapter>(pAdapter);
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
        if (getAdapter() != null) {
            mGridViewBank.setAdapter(getAdapter());
        }
        //set text label
        if (GlobalData.isLinkCardChannel()) {
            txtLabel.setText(Html.fromHtml(GlobalData.getStringResource(RS.string.zpw_string_title_select_card)));
        } else if (getAdapter() instanceof CreditCardSupportGridViewAdapter) {
            txtLabel.setText(Html.fromHtml(GlobalData.getStringResource(RS.string.zpw_string_title_select_card)));
        } else {
            txtLabel.setText(GlobalData.getStringResource(RS.string.zpw_string_title_select_bank));
        }

    }
}



