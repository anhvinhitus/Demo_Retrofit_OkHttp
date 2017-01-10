package vn.com.vng.zalopay.requestsupport;

import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.domain.model.AppResource;

public class CategoryBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private List<AppResource> mAppResourceList = new ArrayList<>();

    @BindView(R.id.numberPicker)
    NumberPicker mPicker;

    public interface OnClickListener {
        void onClickCancel();
        void onClickAccept();
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
        mPicker = new NumberPicker(getContext());
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

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

        setData();
        setPicker();
    }

    private void setPicker() {
        setDividerColor(mPicker, R.color.windowBackground);

        String values[] = new String[mAppResourceList.size()];
        for(int i = 0; i < mAppResourceList.size(); i++){
            values[i] = mAppResourceList.get(i).appname;
        }
        mPicker.setMinValue(0);
        mPicker.setMaxValue(mAppResourceList.size() - 1);
        mPicker.setDisplayedValues(values);
        mPicker.setWrapSelectorWheel(false);
    }

    private void setDividerColor(NumberPicker picker, int color) {
        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(getResources().getColor(color));
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
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
        AppResource tmp = new AppResource();
        tmp.appname = "Nạp tiền";
        AppResource tmp1 = new AppResource();
        tmp1.appname = "Rút tiền";
        AppResource tmp2 = new AppResource();
        tmp2.appname = "Chuyển tiền";
        AppResource tmp3 = new AppResource();
        tmp3.appname = "Nhận tiền";
        AppResource tmp4 = new AppResource();
        tmp4.appname = "Liên kết thẻ";

        mAppResourceList.add(tmp);
        mAppResourceList.add(tmp1);
        mAppResourceList.add(tmp2);
        mAppResourceList.add(tmp3);
        mAppResourceList.add(tmp4);
    }
}
