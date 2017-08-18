package vn.com.zalopay.wallet.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zalopay.ui.widget.dialog.listener.OnCloseDialogListener;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.GlobalData;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.view.adapter.BankSupportAdapter;
import vn.com.zalopay.wallet.view.adapter.RecyclerTouchListener;

/*
 * Created by lytm on 14/07/2017.
 */

public class BankListDialogFragment extends BaseDialogFragment implements View.OnClickListener {
    protected static WeakReference<OnCloseDialogListener> mCloseDialogListener;
    protected WeakReference<BankSupportAdapter> mBankSupportAdapter;
    protected View mSelectBankBtn;
    protected TextView txtLabel;
    private RecyclerView mCardSupportRecyclerView;

    public BankSupportAdapter getAdapter() throws Exception {
        if (mBankSupportAdapter.get() == null) {
            throw new IllegalAccessException("mBankSupportAdapter is null");
        }
        return mBankSupportAdapter.get();
    }

    public void setAdapter(BankSupportAdapter pAdapter) {
        mBankSupportAdapter = new WeakReference<>(pAdapter);
    }

    public void setCloseCardSupportDialog(OnCloseDialogListener pListener) {
        mCloseDialogListener = new WeakReference<>(pListener);
    }

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
    protected void getArgument() {

    }

    @Override
    public void onDestroyView() {
        if (mCloseDialogListener != null && mCloseDialogListener.get() != null) {
            mCloseDialogListener.get().onCloseCardSupportDialog();
        }
        super.onDestroyView();
    }

    @NonNull
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
            int SPAN_COUNT = 2;
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), SPAN_COUNT);
            mCardSupportRecyclerView.setHasFixedSize(true);
            mCardSupportRecyclerView.setLayoutManager(gridLayoutManager);
            mCardSupportRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mCardSupportRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity().getApplicationContext(), mCardSupportRecyclerView));
            mCardSupportRecyclerView.setAdapter(getAdapter());

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
        mCardSupportRecyclerView = (RecyclerView) v.findViewById(R.id.list_card_view);
        mSelectBankBtn = v.findViewById(R.id.select_bank_btn);
        txtLabel = (TextView) v.findViewById(R.id.cardsupport_label);
        mSelectBankBtn.setOnClickListener(this);
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
        if (v.getId() == R.id.select_bank_btn) {
            this.dismiss();
        }
    }

}
