package vn.com.vng.zalopay.bank.ui;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.ui.dialog.BaseDialogFragment;
import vn.com.zalopay.wallet.merchant.entities.ZPCard;

/**
 * Created by longlv on 1/18/17.
 * *
 */

public class ListBankDialog extends BaseDialogFragment {

    public static final String TAG = ListBankDialog.class.getSimpleName();

    public static ListBankDialog newInstance(List<ZPCard> cards) {
        Bundle bundle = new Bundle();
        ListBankDialog fragment = new ListBankDialog();
        bundle.putParcelableArrayList(Constants.ARG_BANK_LIST, (ArrayList<? extends Parcelable>) cards);
        fragment.setArguments(bundle);
        return fragment;
    }

    @BindView(R.id.listBankRecyclerView)
    RecyclerView mRecyclerView;

    @OnClick(R.id.cancel_button)
    public void onClickBtnCancel() {
        dismiss();
    }

    private BankAdapter mAdapter;
    private List<ZPCard> mCards;

    @Override
    protected void setupFragmentComponent() {
        UserComponent userComponent = AndroidApplication.instance().getUserComponent();
        if (userComponent != null) {
            userComponent.inject(this);
        }
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.dialog_list_bank;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    private void initData() {
        if (getArguments() == null) {
            return;
        }
        mCards = getArguments().getParcelableArrayList(Constants.ARG_BANK_LIST);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int columnCount = 2;
        mAdapter = new BankAdapter(getActivity(), mCards, columnCount);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columnCount));
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setFocusable(false);
    }
}
