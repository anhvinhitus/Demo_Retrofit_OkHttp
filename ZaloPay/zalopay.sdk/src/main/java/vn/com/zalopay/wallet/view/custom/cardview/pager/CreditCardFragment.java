package vn.com.zalopay.wallet.view.custom.cardview.pager;

import android.content.res.ColorStateList;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.EditText;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import timber.log.Timber;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.helper.RenderHelper;
import vn.com.zalopay.wallet.ui.channel.ChannelActivity;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;
import vn.com.zalopay.wallet.workflow.AbstractWorkFlow;
import vn.com.zalopay.wallet.workflow.ui.CardGuiProcessor;

public abstract class CreditCardFragment extends Fragment {
    public String tag;
    protected WeakReference<CardGuiProcessor> mGuiProcessor;

    public abstract void setHint(String pMessage);

    public abstract String getError();

    public abstract void setError(String pMessage);

    public abstract void clearError();

    public void clearText() {
    }

    public boolean hasError() {
        return !TextUtils.isEmpty(getError());
    }

    public EditText getEditText() {
        return null;
    }

    public void onChangeTextHintColor(EditText pEditText) {
        if (!(pEditText instanceof VPaymentEditText) || (((VPaymentEditText) pEditText).getTextInputLayout() == null)) {
            return;
        }
        try {
            int color = GlobalData.getAppContext().getResources().getColor(R.color.color_primary);
            int textColor = GlobalData.getAppContext().getResources().getColor(R.color.text_color);
            TextInputLayout textInputLayout = ((VPaymentEditText) pEditText).getTextInputLayout();

            Field fDefaultTextColor = TextInputLayout.class.getDeclaredField("mDefaultTextColor");
            fDefaultTextColor.setAccessible(true);
            fDefaultTextColor.set(textInputLayout, new ColorStateList(new int[][]{{0}}, new int[]{textColor}));

            Field fFocusedTextColor = TextInputLayout.class.getDeclaredField("mFocusedTextColor");
            fFocusedTextColor.setAccessible(true);
            fFocusedTextColor.set(textInputLayout, new ColorStateList(new int[][]{{0}}, new int[]{color}));

        } catch (Exception ex) {
            Timber.w(ex);
        }
    }

    public void onSelectText() {
        EditText editText = getEditText();
        if (editText != null && !TextUtils.isEmpty(editText.getText())) {
            editText.setSelection(editText.getText().length());
        }
    }

    public CardGuiProcessor getGuiProcessor() throws Exception {
        if (mGuiProcessor != null && mGuiProcessor.get() != null) {
            return mGuiProcessor.get();
        }
        if (getPaymentAdapter() == null) {
            throw new Exception("Invalid paymentflow");
        }
        mGuiProcessor = new WeakReference<>(getPaymentAdapter().getGuiProcessor());
        return mGuiProcessor.get();
    }

    public ChannelActivity getHostActivity() throws Exception {
        if (getActivity() instanceof ChannelActivity) {
            return (ChannelActivity) getActivity();
        }
        throw new Exception();
    }

    public AbstractWorkFlow getPaymentAdapter() throws Exception {
        ChannelActivity channelActivity = getHostActivity();
        if (channelActivity != null && !channelActivity.isFinishing()) {
            return channelActivity.getWorkFlow();
        }
        throw new Exception();
    }

    protected void setErrorHint(EditText pEditText, String pMessage) {
        try {
            RenderHelper.setTextInputLayoutHintError(pEditText, pMessage, GlobalData.getAppContext());
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    protected void setHint(EditText pEditText, String pMessage) {
        try {
            RenderHelper.setTextInputLayoutHint(pEditText, pMessage, GlobalData.getAppContext());
        } catch (Exception e) {
            Timber.w(e);
        }
    }
}
