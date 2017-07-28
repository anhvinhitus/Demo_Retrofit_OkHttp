package vn.com.vng.zalopay.bank.list;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import com.daimajia.swipe.util.Attributes;
import com.zalopay.ui.widget.dialog.listener.ZPWOnEventConfirmDialogListener;
import com.zalopay.ui.widget.recyclerview.SpacesItemDecoration;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.internal.DebouncingOnClickListener;
import timber.log.Timber;
import vn.com.vng.zalopay.Constants;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.ui.fragment.BaseFragment;
import vn.com.vng.zalopay.utils.AndroidUtils;
import vn.com.vng.zalopay.utils.DialogHelper;

/**
 * Created by hieuvm on 7/10/17.
 * *
 */

public class BankListFragment extends BaseFragment implements IBankListView, BankListAdapter.OnBankListClickListener {

    protected BankListAdapter mAdapter;
    @BindView(R.id.listview)
    RecyclerView mListView;
    @BindView(R.id.link_card_empty_view)
    View mEmptyView;
    @Inject
    BankListPresenter mPresenter;
    RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState != RecyclerView.SCROLL_STATE_SETTLING) {
                return;
            }

            if (mAdapter != null) {
                mAdapter.closeAllItems();
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    };

    public static BankListFragment newInstance(Bundle args) {
        BankListFragment fragment = new BankListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void setupFragmentComponent() {
        getUserComponent().inject(this);
    }

    @Override
    protected int getResLayoutId() {
        return R.layout.fragment_bank_list;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new BankListAdapter(getContext(), this);
        mAdapter.setMode(Attributes.Mode.Single);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.attachView(this);
        mListView.setHasFixedSize(true);
        mListView.addItemDecoration(new SpacesItemDecoration(AndroidUtils.dp(16)));
        mListView.setLayoutManager(new LinearLayoutManager(getContext()));
        mListView.addOnScrollListener(mOnScrollListener);
        mListView.setAdapter(mAdapter);
        setEmptyViewHeight();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPresenter.loadView();
        mPresenter.handleBundle(this, getArguments());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPresenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        if (mAdapter != null) {
            mAdapter.closeAllItems();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        mListView.clearOnScrollListeners();
        mListView.setAdapter(null);
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_bank, menu);
        MenuItem menuItem = menu.findItem(R.id.action_add_more_bank);
        View view = menuItem.getActionView();
        view.setOnClickListener(new DebouncingOnClickListener() {
            @Override
            public void doClick(View v) {
                mPresenter.startBankSupport(BankListFragment.this);
            }
        });
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
    public void setData(List<BankData> val) {
        mAdapter.setData(val);
        checkIfEmpty();
    }

    @Override
    public List<BankData> getData() {
        return mAdapter != null ? mAdapter.getItems() : null;
    }

    @Override
    public void remove(BankData val) {

        int index = mAdapter.indexOf(val);

        if (index < 0) {
            Timber.d("Bank card not found");
            return;
        }

        Timber.d("Remove bank [index:%s]", index);
        mAdapter.closeItem(index);
        mAdapter.remove(index);
        mAdapter.notifyItemRangeChanged(index, mAdapter.getItemCount());
        mAdapter.closeAllItems();
        checkIfEmpty();
    }

    @Override
    public void close(BankData val) {
        if (mAdapter == null) {
            return;
        }

        int index = mAdapter.indexOf(val);
        //  Timber.d("close: [index:%s]", index);

        if (index < 0) {
            return;
        }

        View childView = mListView.getChildAt(index);

        if (childView instanceof BankCardView) {
            ((BankCardView) childView).close();
        }
    }

    @Override
    public void closeAll() {
        if (mAdapter != null) {
            mAdapter.closeAllItems();
        }
    }

    @Override
    public void insert(int position, BankData val) {
        mAdapter.insert(val, position);
        checkIfEmpty();
    }

    private void checkIfEmpty() {
        mEmptyView.setVisibility(mAdapter.getItemCount() > 1 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onClickAddCard() {
        mPresenter.startBankSupport(this);
    }

    @Override
    public void onClickRemoveCard(BankData card, int position) {
        mPresenter.confirmAndRemoveBank(card);
    }

    @Override
    public void showConfirmDialogAfterLinkBank(String message) {
        DialogHelper.showNoticeDialog(getActivity(),
                message,
                getString(R.string.btn_continue),
                getString(R.string.btn_cancel_transaction),
                new ZPWOnEventConfirmDialogListener() {
                    @Override
                    public void onCancelEvent() {
                        finish(Constants.RESULT_END_PAYMENT);
                    }

                    @Override
                    public void onOKEvent() {
                        finish(Activity.RESULT_OK);
                    }
                });
    }

    void finish(int result) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        getActivity().setResult(result);
        getActivity().finish();
    }

    private void setEmptyViewHeight() {
        Window window = getActivity().getWindow();
        if (window == null) {
            return;
        }
        int screenHeight = AndroidUtils.displaySize.y;
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = (int) (screenHeight * 0.5);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.gravity = Gravity.CENTER;
        mEmptyView.setLayoutParams(params);
    }
}
