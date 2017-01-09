package vn.com.vng.zalopay.requestsupport;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.AppResource;

public class CategoryBottomSheetDialogFragment extends BottomSheetDialogFragment implements
        RequestSupportAdapter.OnClickAppListener {

    private RequestSupportAdapter mAdapter;

    @BindView(R.id.listView)
    RecyclerView mRecyclerView;

    public interface OnClickListener {
        void onClickCancel();
        void onClickAccept();
        void onClickAppListener();
    }

    public static CategoryBottomSheetDialogFragment newInstance() {
        Bundle args = new Bundle();
        CategoryBottomSheetDialogFragment fragment = new CategoryBottomSheetDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new RequestSupportAdapter(getContext(), this);
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
        View contentView = View.inflate(getContext(), R.layout.bottom_sheet_pick_category, null);
        dialog.setContentView(contentView);

        ButterKnife.bind(this, contentView);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setBackgroundColor(Color.WHITE);

        mRecyclerView.setAdapter(mAdapter);

        setData();

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

    @OnClick(R.id.tvCancel)
    public void onClickCancel() {
        dismiss();
    }

    @OnClick(R.id.tvAccept)
    public void onClickAccept() {
        dismiss();
    }

    @Override
    public void onClickAppListener(AppResource app, int position) {

    }

    private CategoryBottomSheetDialogFragment.OnClickListener listener;

    public void setOnClickListener(CategoryBottomSheetDialogFragment.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDestroyView() {
        this.listener = null;
        super.onDestroyView();
    }

    private void setData() {
        AppResource appResource = new AppResource();
        appResource.appname = "Rút tiền";
        mAdapter.insert(appResource, 0);
        AppResource appResource2 = new AppResource();
        appResource2.appname = "Chuyển tiền";
        mAdapter.insert(appResource2, 1);
        AppResource appResource3 = new AppResource();
        appResource3.appname = "Trả tiền";
        mAdapter.insert(appResource3, 2);
        AppResource appResource4 = new AppResource();
        appResource4.appname = "Nhận tiền";
        mAdapter.insert(appResource4, 3);
        AppResource appResource5 = new AppResource();
        appResource5.appname = "Nạp tiền";
        mAdapter.insert(appResource5, 4);
    }
}
