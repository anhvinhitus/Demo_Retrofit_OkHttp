package vn.com.vng.zalopay.passport.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import butterknife.BindView;
import vn.com.vng.zalopay.R;

/**
 * Created by hieuvm on 6/14/17.
 * *
 */
public class OnboardingPwdView extends OnboardingView {

    public OnboardingPwdView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public OnboardingPwdView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @BindView(R.id.inputview)
    CustomPassCodeRoundView mInputView;

    public void assign() {
        super.assign();

    }

    public void setEnteredListener(InputEnteredListener listener) {
        mInputView.setPinEnteredListener(listener);
    }

    @NonNull
    public String getInputText() {
        return mInputView.getInputText();
    }

    public void setInputText(@NonNull String text) {
        mInputView.setInputText(text);
    }

    public NumberEditable getNumberEditable() {
        return mInputView;
    }

}