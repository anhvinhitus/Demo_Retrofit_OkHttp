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

import java.util.List;

import butterknife.ButterKnife;
import vn.com.vng.zalopay.R;

/**
 * Created by khattn on 2/21/17.
 */

public class ItemBottomSheetDialogFragment extends BottomSheetDialogFragment implements
        WebAppBottomSheetAdapter.OnClickItemListener {
    private final static int COLUMN_COUNT = 5;

    RecyclerView mRecyclerView;
    private WebAppBottomSheetAdapter mAdapter;

    public interface OnClickListener {
        void onClickCopyURL();

        void onClickRefresh();

        void onClickOpenInBrowser();

        void onClickShareOnZalo();
    }

    public static ItemBottomSheetDialogFragment newInstance() {
        Bundle args = new Bundle();
        ItemBottomSheetDialogFragment fragment = new ItemBottomSheetDialogFragment();
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

        ButterKnife.bind(this, contentView);
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.recyclerview);

        initRecyclerView();

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

    private ItemBottomSheetDialogFragment.OnClickListener listener;

    public void setOnClickListener(ItemBottomSheetDialogFragment.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDestroyView() {
        this.listener = null;
        super.onDestroyView();
    }

    private void initRecyclerView() {
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
                listener.onClickCopyURL();
                break;
            case WebAppBottomSheetItemUtil.REFRESH:
                listener.onClickRefresh();
                break;
            case WebAppBottomSheetItemUtil.OPEN_IN_BROWSER:
                listener.onClickOpenInBrowser();
                break;
            case WebAppBottomSheetItemUtil.SHARE_ON_ZALO:
                listener.onClickShareOnZalo();
                break;
        }
    }
}