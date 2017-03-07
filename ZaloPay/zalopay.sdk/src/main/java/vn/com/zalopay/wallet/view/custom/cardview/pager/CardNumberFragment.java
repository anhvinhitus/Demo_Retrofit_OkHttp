package vn.com.zalopay.wallet.view.custom.cardview.pager;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.channel.creditcard.CreditCardCheck;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardCheck;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.view.custom.VPaymentDrawableEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;

/**
 * Card number fragment
 */
public class CardNumberFragment extends CreditCardFragment {
    protected VPaymentDrawableEditText mCardNumberView;

    protected ImageView mImageViewQuestion;

    protected View mVirtualView;

    protected FrameLayout mRootView;

    public CardNumberFragment() {
    }

    /***
     * show icon support bank ?
     * user click into this icon that will show support card list dialog
     */
    public void showQuestionIcon() {
        if (mImageViewQuestion == null) {
            mImageViewQuestion = new ImageView(GlobalData.getAppContext());

            Bitmap bmBankSupportHelp = ResourceManager.getImage(RS.drawable.ic_bank_support_help);

            if (bmBankSupportHelp == null) {
                Log.d(this, "===bmBankSupportHelp=null===");
            }

            if (bmBankSupportHelp != null) {
                mImageViewQuestion.setImageBitmap(bmBankSupportHelp);

                if (getGuiProcessor() != null) {
                    mImageViewQuestion.setOnClickListener(getGuiProcessor().getOnQuestionIconClick());
                }
            }
        }

        if (mImageViewQuestion != null && mCardNumberView != null && mRootView != null) {
            //calculate the width of text
            String text = GlobalData.getStringResource(RS.string.zpw_string_card_not_support);

            Rect bounds = new Rect();
            Paint textPaint = mCardNumberView.getPaint();
            textPaint.getTextBounds(text, 0, text.length(), bounds);

            final int width = bounds.width();

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );

            params.leftMargin = width / 4 * 3 + 20;
            params.topMargin = (int) GlobalData.getAppContext().getResources().getDimension(R.dimen.margin_small);

            mImageViewQuestion.setLayoutParams(params);

            mRootView.removeView(mImageViewQuestion);
            mRootView.addView(mImageViewQuestion);

            //add virtual view to extend region of touched
            if (mVirtualView == null) {
                mVirtualView = new View(GlobalData.getAppContext());
                mVirtualView.setOnClickListener(getGuiProcessor().getOnQuestionIconClick());

            }
            // get height when iconQuestion loadconplete
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        FrameLayout.LayoutParams virtualParams = new FrameLayout.LayoutParams(width + mImageViewQuestion.getWidth(), mImageViewQuestion.getHeight() + 20);
                        mVirtualView.setLayoutParams(virtualParams);
                        mRootView.removeView(mVirtualView);
                        mRootView.addView(mVirtualView);
                        mRootView.requestLayout();
                    } catch (Exception e) {
                        Log.e(this, e);
                    }
                }
            }, 100);
        }
    }

    public void hideQuestionIcon() {
        if (mRootView != null && mImageViewQuestion != null) {
            mRootView.removeView(mImageViewQuestion);

            mRootView.removeView(mVirtualView);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle state) {

        View v = inflater.inflate(R.layout.lyt_card_number, group, false);

        mCardNumberView = (VPaymentDrawableEditText) v.findViewById(R.id.edittext_localcard_number);

        if (mCardNumberView != null) {
            mCardNumberView.setImeOptions(EditorInfo.IME_ACTION_NEXT | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

            onChangeTextHintColor(mCardNumberView);
        }

        mRootView = (FrameLayout) v.findViewById(R.id.framelayoutContainer);

        try {
            if (getGuiProcessor() != null && mCardNumberView != null) {
                mCardNumberView.addTextChangedListener(getGuiProcessor().getCardDetectionTextWatcher());
                mCardNumberView.setOnEditorActionListener(getGuiProcessor().getEditorActionListener());
                mCardNumberView.setOnFocusChangeListener(getGuiProcessor().getOnFocusChangeListener());

                //user touch on edittext,show keyboard
                if (mCardNumberView instanceof VPaymentEditText && mCardNumberView.getTextInputLayout() instanceof TextInputLayout) {
                    (mCardNumberView.getTextInputLayout()).setOnClickListener(getGuiProcessor().getClickOnEditTextListener());
                } else {
                    mCardNumberView.setOnClickListener(getGuiProcessor().getClickOnEditTextListener());
                }

            }
        } catch (Exception e) {
            Log.e(this, e);
        }

        return v;
    }

    @Override
    public void clearText() {
        if (mCardNumberView != null) {
            mCardNumberView.setText(null);
        }
    }

    @Override
    public EditText getEditText() {
        return mCardNumberView;
    }

    @Override
    public void setHint(String pMessage) {
        if (mCardNumberView != null) {
            hideQuestionIcon();

            setHint(mCardNumberView, pMessage);
        }
    }

    @Override
    public void clearError() {
        if (mCardNumberView != null) {
            hideQuestionIcon();

            setHint(mCardNumberView, null);
        }
    }

    /***
     * get hint error
     *
     * @return
     */
    @Override
    public String getError() {
        String errorMess = null;

        if (mCardNumberView instanceof VPaymentEditText && mCardNumberView.getTextInputLayout() instanceof TextInputLayout) {
            errorMess = (String) (mCardNumberView.getTextInputLayout()).getHint();

            if (!TextUtils.isEmpty(errorMess) && errorMess.equalsIgnoreCase((mCardNumberView.getTextInputLayout()).getTag().toString())) {
                errorMess = null;
            }
        }

        //this is not error hint,it is detected bank name
        if (!TextUtils.isEmpty(errorMess) && !errorMess.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_link_card_existed))) {
            try {
                if ((getPaymentAdapter().isATMFlow() && (BankCardCheck.getInstance().isDetected()
                        || CreditCardCheck.getInstance().isDetected()))) {
                    errorMess = null;

                } else if (getPaymentAdapter().isCCFlow() && (CreditCardCheck.getInstance().isDetected()
                        || BankCardCheck.getInstance().isDetected())) {
                    errorMess = null;
                }

            } catch (Exception e) {
                Log.e(this, e);

                errorMess = null;
            }
        }

        return errorMess;
    }

    @Override
    public void setError(String pMessage) {
        if (mCardNumberView != null) {
            setErrorHint(mCardNumberView, pMessage);
        }
    }
}
