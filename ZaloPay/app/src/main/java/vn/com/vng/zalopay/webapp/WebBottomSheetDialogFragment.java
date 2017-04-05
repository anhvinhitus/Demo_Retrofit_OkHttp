package vn.com.vng.zalopay.webapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.internal.di.components.UserComponent;
import vn.com.vng.zalopay.navigation.Navigator;

/**
 * Created by khattn on 2/21/17.
 *
 */

public class WebBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface OnClickListener {
        void handleClickRefreshWeb();
    }

    private String mCurrentUrl;

    @Inject
    Navigator mNavigator;

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
        ButterKnife.bind(this, contentView);

        mCurrentUrl = getArguments().getString("currenturl");
        setIntroText(contentView);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

        setupFragmentComponent();
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
    }

    private void setIntroText(View view) {
        String description = String.format(getString(R.string.webapp_intro_bottomsheet),
                Strings.getDomainName(mCurrentUrl));
        TextView introText = (TextView) view.findViewById(R.id.tv_intro);
        introText.setText(description);
    }

    private OnClickListener listener;

    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    @OnClick(R.id.layoutShareOnZalo)
    public void handleClickShareOnZalo() {
        mNavigator.shareWebOnZalo(getContext(), mCurrentUrl);
    }

    @OnClick(R.id.layoutCopyURL)
    public void handleClickCopyURL() {
        setClipboard(getContext(), mCurrentUrl);
        Toast.makeText(getContext(), getContext().getResources().getText(R.string.copy_clipboard), Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.layoutRefresh)
    public void handleClickRefreshWeb() {
        if(listener == null) {
            return;
        }
        listener.handleClickRefreshWeb();
    }

    @OnClick(R.id.layoutOpenInBrowser)
    public void handleClickOpenInBrowser() {
        mNavigator.openWebInBrowser(getContext(), mCurrentUrl);
    }

    private void setClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied URL", text);
        clipboard.setPrimaryClip(clip);
    }
}