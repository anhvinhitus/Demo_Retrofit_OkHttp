package vn.com.zalopay.wallet.view.custom.cardview.pager;

import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.facebook.drawee.view.SimpleDraweeView;

import timber.log.Timber;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.configure.GlobalData;
import vn.com.zalopay.wallet.configure.RS;
import vn.com.zalopay.wallet.card.BankDetector;
import vn.com.zalopay.wallet.card.CreditCardDetector;
import vn.com.zalopay.wallet.repository.ResourceManager;
import vn.com.zalopay.wallet.view.custom.VPaymentDrawableEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;
import vn.com.zalopay.wallet.workflow.ui.CardGuiProcessor;

/**
 * Card number fragment
 */
public class CardNumberFragment extends CreditCardFragment {
    protected VPaymentDrawableEditText mCardNumberView;

    protected SimpleDraweeView mImageViewQuestion;

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
            mImageViewQuestion = new SimpleDraweeView(GlobalData.getAppContext());
            ResourceManager.loadLocalSDKImage(mImageViewQuestion, RS.drawable.ic_bank_support_help);

            try {
                if (getGuiProcessor() != null) {
                    mImageViewQuestion.setOnClickListener(getGuiProcessor().getOnQuestionIconClick());
                }
            } catch (Exception e) {
                Timber.w(e);
            }
        }

        if (mImageViewQuestion != null && mCardNumberView != null && mRootView != null) {
            //calculate the width of text
            String text = GlobalData.getAppContext().getResources().getString(R.string.sdk_card_not_support);

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
                try {
                    mVirtualView.setOnClickListener(getGuiProcessor().getOnQuestionIconClick());
                } catch (Exception e) {
                    Timber.d(e);
                }

            }
            // get height when iconQuestion loadconplete
            new Handler().postDelayed(() -> {
                try {
                    FrameLayout.LayoutParams virtualParams = new FrameLayout.LayoutParams(width + mImageViewQuestion.getWidth(), mImageViewQuestion.getHeight() + 20);
                    mVirtualView.setLayoutParams(virtualParams);
                    mRootView.removeView(mVirtualView);
                    mRootView.addView(mVirtualView);
                    mRootView.requestLayout();
                } catch (Exception e) {
                    Timber.d(e, "Exception showQuestionIcon");
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
            CardGuiProcessor cardGuiProcessor = getGuiProcessor();
            if (cardGuiProcessor == null) {
                return v;
            }
            if (mCardNumberView == null) {
                return v;
            }
            mCardNumberView.addTextChangedListener(cardGuiProcessor.getCardDetectionTextWatcher());
            mCardNumberView.setOnEditorActionListener(cardGuiProcessor.getEditorActionListener());
            mCardNumberView.setOnFocusChangeListener(cardGuiProcessor.getOnFocusChangeListener());

            //user touch on edittext,show keyboard
            if (mCardNumberView instanceof VPaymentEditText && mCardNumberView.getTextInputLayout() != null) {
                (mCardNumberView.getTextInputLayout()).setOnClickListener(cardGuiProcessor.getClickOnEditTextListener());
            } else {
                mCardNumberView.setOnClickListener(cardGuiProcessor.getClickOnEditTextListener());
            }
        } catch (Exception e) {
            Timber.w(e);
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
        if (mCardNumberView != null && mCardNumberView.getTextInputLayout() != null) {
            errorMess = (String) (mCardNumberView.getTextInputLayout()).getHint();
            Object tag = mCardNumberView.getTextInputLayout().getTag();
            if (!TextUtils.isEmpty(errorMess) && errorMess.equalsIgnoreCase(tag.toString())) {
                errorMess = null;
            }
        }
        //this is not error hint,it is detected bank name
        String warning = null;
        try {
            warning = getPaymentFlow().getGuiProcessor().warningCardExist();
        } catch (Exception e) {
            Timber.d(e, "Exception warningCardExist");
        }
        if (!TextUtils.isEmpty(errorMess) && !errorMess.equalsIgnoreCase(warning)) {
            try {
                if ((getPaymentFlow().isATMFlow() && (BankDetector.getInstance().detected()
                        || CreditCardDetector.getInstance().detected()))) {
                    errorMess = null;
                } else if (getPaymentFlow().isCCFlow() && (CreditCardDetector.getInstance().detected()
                        || BankDetector.getInstance().detected())) {
                    errorMess = null;
                }
            } catch (Exception e) {
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
