package vn.com.zalopay.wallet.voucher;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.design.widget.BottomSheetBehavior;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.zalopay.ui.widget.IUIBottomSheetBuilder;
import com.zalopay.ui.widget.UIBottomSheetDialog;

import java.lang.ref.WeakReference;

import timber.log.Timber;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.repository.ResourceManager;

import static android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN;
import static vn.com.zalopay.wallet.helper.FontHelper.applyFont;

/*
 * Created by chucvv on 8/2/17.
 */

public class VoucherRender implements UIBottomSheetDialog.IRender {
    IUIBottomSheetBuilder mBuilder;
    WeakReference<Context> mContext;
    EditText mInputView;

    VoucherRender(IUIBottomSheetBuilder builder) {
        this.mBuilder = builder;
    }

    public static IVoucherDialogBuilder getBuilder() {
        return new VoucherDialogBuilder();
    }

    public void showKeyBoard() {
        new Handler().postDelayed(() -> {
            try {
                SdkUtils.focusAndSoftKeyboard((Activity) getContext(), mInputView);
            } catch (Exception e) {
                Timber.d(e);
            }
        }, 100);
    }

    public void setError(String error) {
        try {
            View view = getView();
            if (view == null) {
                return;
            }
            TextView error_textview = (TextView) view.findViewById(R.id.error_textview);
            error_textview.setText(error);
            boolean hasError = !TextUtils.isEmpty(error);
            error_textview.setVisibility(hasError ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    public void clearError() {
        try {
            View view = getView();
            if (view == null) {
                return;
            }
            TextView error_textview = (TextView) view.findViewById(R.id.error_textview);
            error_textview.setText(null);
            error_textview.setVisibility(View.GONE);
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    public void showLoading() {
        try {
            View view = getView();
            if (view == null) {
                return;
            }
            View loadingView = view.findViewById(R.id.loading_view);
            loadingView.setVisibility(View.VISIBLE);
            clearError();
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    public void hideLoading() {
        try {
            View view = getView();
            if (view == null) {
                return;
            }
            View loadingView = view.findViewById(R.id.loading_view);
            loadingView.setVisibility(View.INVISIBLE);

        } catch (Exception e) {
            Timber.d(e);
        }
    }

    private void onVoucherCodeInputComplete(String voucherCode) {
        if (mBuilder == null) {
            return;
        }
        try {
            if (TextUtils.isEmpty(voucherCode)) {
                String emptyWarning = getContext()
                        .getResources()
                        .getString(R.string.sdk_voucher_code_empty_mess);
                setError(emptyWarning);
                return;
            }
            IInteractVoucher interactVoucher = ((IVoucherDialogBuilder) mBuilder).getInteractListener();
            if (interactVoucher != null) {
                interactVoucher.onVoucherInfoComplete(voucherCode);
            }
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    Context getContext() throws Exception {
        if (mContext == null || mContext.get() == null) {
            throw new IllegalStateException("Context is null");
        }
        return mContext.get();
    }

    @Override
    public void render(Context context) throws Exception {
        try {
            if (mBuilder == null) {
                return;
            }
            View view = mBuilder.getView();
            if (view == null) {
                return;
            }
            mContext = new WeakReference<>(context);
            View closeView = view.findViewById(R.id.cancel_img);
            if (closeView != null) {
                closeView.setOnClickListener(view1 -> {
                    try {
                        if (mBuilder == null) {
                            return;
                        }
                        OnDismiss();
                    } catch (Exception e) {
                        Timber.d(e);
                    }
                });
            }
            View use_vouchercode_ll = view.findViewById(R.id.use_vouchercode_ll);
            ImageView use_vouchercode_img = (ImageView) view.findViewById(R.id.use_vouchercode_img);
            TextView useVoucherView = (TextView) view.findViewById(R.id.use_vouchercode_textview);
            mInputView = (EditText) view.findViewById(R.id.vouchercode_input_edittext);
            TextView vouchercode_input_hint = (TextView) view.findViewById(R.id.vouchercode_input_hint);

            ResourceManager.loadImageIntoView(use_vouchercode_img, RS.drawable.ic_next_blue_disable);
            use_vouchercode_ll.setOnClickListener(view12 -> onVoucherCodeInputComplete(mInputView.getText().toString()));

            mInputView.setOnEditorActionListener((textView, actionId, keyEvent) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                    onVoucherCodeInputComplete(mInputView.getText().toString());
                }
                return false;
            });
            mInputView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    try {
                        String text = editable != null ? editable.toString() : null;
                        if (TextUtils.isEmpty(text)) {
                            useVoucherView.setTextColor(getContext().getResources().getColor(R.color.text_color));
                            ResourceManager.loadImageIntoView(use_vouchercode_img, RS.drawable.ic_next_blue_disable);
                        } else {
                            useVoucherView.setTextColor(getContext().getResources().getColor(R.color.color_primary));
                            ResourceManager.loadImageIntoView(use_vouchercode_img, RS.drawable.ic_next_blue);
                        }
                    } catch (Exception e) {
                        Timber.d(e);
                    }
                }
            });
            applyFont(mInputView, GlobalData.getStringResource(RS.string.sdk_font_medium));
            applyFont(vouchercode_input_hint, GlobalData.getStringResource(RS.string.sdk_font_medium));
            showKeyBoard();
        } catch (Exception e) {
            Timber.d(e, "Exception render voucher input");
        }
    }

    @Override
    public View getView() throws Exception {
        return mBuilder != null ? mBuilder.getView() : null;
    }

    @Override
    public void OnDismiss() throws Exception {
        if (mBuilder == null) {
            return;
        }
        try {
            View parent = getView() != null ? (View) getView().getParent() : null;
            if (parent != null) {
                BottomSheetBehavior mBottomSheetBehavior = BottomSheetBehavior.from(parent);
                mBottomSheetBehavior.setState(STATE_HIDDEN);
            }
            IInteractVoucher interactVoucher = ((IVoucherDialogBuilder) mBuilder).getInteractListener();
            if (interactVoucher != null) {
                interactVoucher.onClose();
            }
        } catch (Exception e) {
            Timber.d(e);
        }
    }
}
