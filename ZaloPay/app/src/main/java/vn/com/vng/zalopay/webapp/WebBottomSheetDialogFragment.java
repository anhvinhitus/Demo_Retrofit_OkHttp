package vn.com.vng.zalopay.webapp;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.zalopay.analytics.ZPAnalytics;
import vn.com.zalopay.analytics.ZPEvents;

/**
 * Created by khattn on 2/21/17.
 */

public class WebBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private BottomSheetEventListener mBottomSheetEventListener;
    private String mCurrentUrl;

    public static WebBottomSheetDialogFragment newInstance(Bundle args) {
        WebBottomSheetDialogFragment fragment = new WebBottomSheetDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.bottom_sheet_webapp, container, true);
        ButterKnife.bind(this, contentView);

        mCurrentUrl = getArguments().getString(WebAppConstants.PARAM_CURRENT_URL);
        setIntroText(contentView);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) contentView.getLayoutParams();
        if (params == null) {
            return contentView;
        }

        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior == null) {
            return contentView;
        }

        if (behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

        return contentView;
    }

    public void setBottomSheetEventListener(BottomSheetEventListener listener) {
        this.mBottomSheetEventListener = listener;
    }

    @OnClick(R.id.layoutShareOnZalo)
    void handleClickShareOnZalo() {
        NavigatorHelper.shareWebOnZalo(getContext(), mCurrentUrl);
        ZPAnalytics.trackEvent(ZPEvents.PROMOTION_DETAIL_SHARE_ZALO);
    }

    @OnClick(R.id.layoutCopyURL)
    void handleClickCopyURL() {
        setClipboard(getContext(), mCurrentUrl);
        Toast.makeText(getContext(), getContext().getResources().getText(R.string.copy_clipboard), Toast.LENGTH_SHORT).show();
        ZPAnalytics.trackEvent(ZPEvents.PROMOTION_DETAIL_SHARE_URLCOPY);
    }

    @OnClick(R.id.layoutRefresh)
    void handleClickRefreshWeb() {
        if (mBottomSheetEventListener == null) {
            return;
        }
        mBottomSheetEventListener.onRequestRefreshPage();
        ZPAnalytics.trackEvent(ZPEvents.PROMOTION_DETAIL_SHARE_REFRESH);
    }

    @OnClick(R.id.layoutOpenInBrowser)
    void handleClickOpenInBrowser() {
        NavigatorHelper.openWebInBrowser(getContext(), mCurrentUrl);
        ZPAnalytics.trackEvent(ZPEvents.PROMOTION_DETAIL_SHARE_TOBROWSER);
    }

    private void setClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied URL", text);
        clipboard.setPrimaryClip(clip);
    }

    private void setIntroText(View view) {
        String description = String.format(getString(R.string.webapp_intro_bottomsheet), Strings.getDomainName(mCurrentUrl));
        TextView introText = (TextView) view.findViewById(R.id.tv_intro);
        if (introText == null) {
            return;
        }

        introText.setText(description);
    }

    public interface BottomSheetEventListener {
        /**
         * Called when User choose to refresh current page.
         * Expected behavior: the host invoke webview to reload current url
         */
        void onRequestRefreshPage();
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
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        ZPAnalytics.trackEvent(ZPEvents.PROMOTION_DETAIL_SHARE_TOUCH_CLOSE);
    }
}