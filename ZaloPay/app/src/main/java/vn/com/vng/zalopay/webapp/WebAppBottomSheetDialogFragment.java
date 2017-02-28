package vn.com.vng.zalopay.webapp;

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

import java.net.URISyntaxException;
import java.util.List;

import vn.com.vng.zalopay.R;

/**
 * Created by khattn on 2/21/17.
 */

public class WebAppBottomSheetDialogFragment extends BottomSheetDialogFragment implements
        WebAppBottomSheetAdapter.OnClickItemListener {
    private final static int COLUMN_COUNT = 5;

    private RecyclerView mRecyclerView;
    private WebAppBottomSheetAdapter mAdapter;
    private WebAppBottomSheetPresenter mPresenter;
    private String mCurrentUrl;

    public static WebAppBottomSheetDialogFragment newInstance() {
        Bundle args = new Bundle();
        WebAppBottomSheetDialogFragment fragment = new WebAppBottomSheetDialogFragment();
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

        mPresenter = new WebAppBottomSheetPresenter(getContext());
        mCurrentUrl = getArguments().getString("currenturl");

        initRecyclerView(contentView);
        setIntroText(contentView);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void setIntroText(View view) {
        try {
            String description = String.format(getString(R.string.webapp_intro_bottomsheet),
                    mPresenter.getDomainName(mCurrentUrl));
            TextView introText = (TextView) view.findViewById(R.id.tv_intro);
            introText.setText(description);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void initRecyclerView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mAdapter = new WebAppBottomSheetAdapter(getContext(), this);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), COLUMN_COUNT));
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setFocusable(false);

        setData();
    }

    private void setData() {
        List<WebAppBottomSheetItem> list = WebAppBottomSheetItemUtil.getMenuItems();
        mAdapter.insertItems(list);
    }

    @Override
    public void onClickItem(int position) {
        int id = mAdapter.getItem(position).id;
        switch (id) {
            case WebAppBottomSheetItemUtil.COPY_URL:
                mPresenter.handleClickCopyURL(mCurrentUrl);
                dismiss();
                break;
            case WebAppBottomSheetItemUtil.REFRESH:
                ((WebAppFragment) getParentFragment()).refreshWeb();
                dismiss();
                break;
            case WebAppBottomSheetItemUtil.OPEN_IN_BROWSER:
                mPresenter.handleClickOpenInBrowser(mCurrentUrl);
                break;
            case WebAppBottomSheetItemUtil.SHARE_ON_ZALO:
                mPresenter.handleClickShareOnZalo(mCurrentUrl);
                break;
        }
    }
}