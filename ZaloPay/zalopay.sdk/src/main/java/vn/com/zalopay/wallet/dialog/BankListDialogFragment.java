package vn.com.zalopay.wallet.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.view.adapter.CardSupportAdapter;

/**
 * Created by lytm on 14/07/2017.
 */

public class BankListDialogFragment extends BaseDialogFragment implements View.OnClickListener {
    public static final String TAG = "BankListDialogFragment";
    protected WeakReference<CardSupportAdapter> mCardSupportGridViewAdapter;
    protected GridView mGridViewBank;
    protected View mRippleButtonSelectBank;
    protected TextView txtLabel;

    public CardSupportAdapter getAdapter() throws Exception {
        if (mCardSupportGridViewAdapter.get() == null) {
            throw new IllegalAccessException("mCardSupportGridViewAdapter is null");
        }
        return mCardSupportGridViewAdapter.get();
    }

    public void setAdapter(CardSupportAdapter pAdapter) {
        mCardSupportGridViewAdapter = new WeakReference<>(pAdapter);
    }

    @Override
    public BankListDialogFragment newInstance() {
        Bundle args = new Bundle();
        BankListDialogFragment fragment = new BankListDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getWidthLayout() {
        return 0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getString(R.string.confirm));
        getDialog().setCanceledOnTouchOutside(false);
        View v = inflater.inflate(getLayout(), container, false);
        initViews(v);
        return v;
    }

    @Override
    protected void initData() {
        try {
            if (getAdapter() != null) {
                mGridViewBank.setAdapter(getAdapter());
            }
            if (GlobalData.transtype() == TransactionType.LINK) {
                txtLabel.setText(Html.fromHtml(GlobalData.getAppContext().getResources().getString(R.string.sdk_support_banklist_link_title)));
            } else {
                txtLabel.setText(GlobalData.getAppContext().getString(R.string.sdk_support_banklist_title));
            }
        } catch (Exception e) {
            Timber.d("get card support Adapter: [%s]", e);
        }
    }

    @Override
    protected void initViews(View v) {
        mGridViewBank = (GridView) v.findViewById(R.id.gridViewCard);
        mRippleButtonSelectBank = v.findViewById(R.id.rippleButtonSelectBank);
        txtLabel = (TextView) v.findViewById(R.id.cardsupport_label);
        mRippleButtonSelectBank.setOnClickListener(this);
    }

    @Override
    protected int getLayout() {
        return R.layout.screen__bank__list;
    }

    @Override
    protected int getLayoutSize() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return (int) (metrics.heightPixels * 0.70);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.rippleButtonSelectBank) {
            this.dismiss();
        }
    }
}
