package vn.com.vng.zalopay.webbottomsheetdialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.internal.di.components.UserComponent;

/**
 * Created by khattn on 2/21/17.
 *
 */

public class WebBottomSheetDialogFragment extends BottomSheetDialogFragment implements
        WebBottomSheetAdapter.OnClickItemListener, IWebBottomSheetView {
    private final static int COLUMN_COUNT = 5;

    private RecyclerView mRecyclerView;
    private WebBottomSheetAdapter mAdapter;
    private String mCurrentUrl;

    @Inject
    WebBottomSheetPresenter mPresenter;

    public static WebBottomSheetDialogFragment newInstance() {
        Bundle args = new Bundle();
        WebBottomSheetDialogFragment fragment = new WebBottomSheetDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.bottom_sheet_webapp, null);
        dialog.setContentView(contentView);

        mCurrentUrl = getArguments().getString("currenturl");

        initRecyclerView(contentView);
        setIntroText(contentView);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

        setupFragmentComponent();
        mPresenter.attachView(this);
    }

    private void setupFragmentComponent() {
        UserComponent userComponent = AndroidApplication.instance().getUserComponent();
        if (userComponent != null) {
            userComponent.inject(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    private void setIntroText(View view) {
        String description = String.format(getString(R.string.webapp_intro_bottomsheet),
                Strings.getDomainName(mCurrentUrl));
        TextView introText = (TextView) view.findViewById(R.id.tv_intro);
        introText.setText(description);
    }

    private void initRecyclerView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mAdapter = new WebBottomSheetAdapter(getContext(), this);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), COLUMN_COUNT));
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setFocusable(false);

        setData();
    }

    private void setData() {
        List<WebBottomSheetItem> list = WebBottomSheetItemUtil.getMenuItems();
        mAdapter.insertItems(list);
    }

    @Override
    public void closeDialog() {
        dismiss();
    }

    @Override
    public void onClickItem(int position) {
        int id = mAdapter.getItem(position).id;
        switch (id) {
            case WebBottomSheetItemUtil.COPY_URL:
                mPresenter.handleClickCopyURL(getContext(), mCurrentUrl);
                break;
            case WebBottomSheetItemUtil.REFRESH:
                mPresenter.handleClickRefreshWeb();
                break;
            case WebBottomSheetItemUtil.OPEN_IN_BROWSER:
                mPresenter.handleClickOpenInBrowser(getContext(), mCurrentUrl);
                break;
            case WebBottomSheetItemUtil.SHARE_ON_ZALO:
                mPresenter.handleClickShareOnZalo(getContext(), mCurrentUrl);
                break;
        }
    }
}