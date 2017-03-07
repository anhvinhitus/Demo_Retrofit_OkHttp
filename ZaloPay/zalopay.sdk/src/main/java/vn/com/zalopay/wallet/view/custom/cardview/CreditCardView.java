package vn.com.zalopay.wallet.view.custom.cardview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.channel.base.CardGuiProcessor;
import vn.com.zalopay.wallet.business.data.Constants;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.CardColorText;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.ViewUtils;
import vn.com.zalopay.wallet.utils.ZPWUtils;
import vn.com.zalopay.wallet.view.effects.FlipAnimator;

public class CreditCardView extends FrameLayout {
    private static final int TEXTVIEW_CARD_HOLDER_ID = R.id.front_card_holder_name;
    private static final int TEXTVIEW_CARD_EXPIRY_ID = R.id.front_card_expiry;
    private static final int TEXTVIEW_CARD_NUMBER_ID = R.id.front_card_number;
    private static final int TEXTVIEW_CARD_CVV_ID = R.id.back_card_cvv;
    private static final int FRONT_CARD_ID = R.id.front_card_container;
    private static final int BACK_CARD_ID = R.id.back_card_container;
    private static final int FRONT_CARD_OUTLINE_ID = R.id.front_card_outline;
    private static final int BACK_CARD_OUTLINE_ID = R.id.back_card_outline;
    private static final int CARD_VIEW_ID = R.id.card_view_container;
    protected boolean mNeedToReveal;
    private String mRawCardNumber;
    private String mCardHolderName, mCVV, mCardDate;
    private int mWidthCardView;
    private float mPercentWitdh;
    private CardColorText mCardColorText;

    public CreditCardView(Context context) {
        super(context);
        init();
    }

    public CreditCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CreditCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public void initCardSelector() {
        CardSelector.getInstance();
    }

    public String getCardHolderName() {
        return mCardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        cardHolderName = cardHolderName == null ? "" : cardHolderName;

        this.mCardHolderName = cardHolderName;

        ((TextView) findViewById(TEXTVIEW_CARD_HOLDER_ID)).setText(Html.fromHtml(createHighLightText(cardHolderName, mCardColorText)));
        // Clear text set HintText Color
        if (TextUtils.isEmpty(cardHolderName)) {
            setDefaultShadowColor();
        }
    }

    public String getCVV() {
        return mCVV;
    }

    public void setCVV(String cvv) {
        if (cvv == null) {
            cvv = "";
        }

        this.mCVV = cvv;
        //CreditCardUtils.handleCardCVV(getCVV())
        ((TextView) findViewById(TEXTVIEW_CARD_CVV_ID)).setText(Html.fromHtml(createHighLightText(cvv, mCardColorText)));
    }

    public String getCardDate() {
        return mCardDate;
    }

    public void setCardDate(String dateYear) {

        dateYear = dateYear == null ? "" : CreditCardUtils.handleExpiration(dateYear);

        this.mCardDate = dateYear;
        ((TextView) findViewById(TEXTVIEW_CARD_EXPIRY_ID)).setText(Html.fromHtml(createHighLightText(dateYear, mCardColorText)));
        // Clear text set HintText Color
        if (TextUtils.isEmpty(dateYear)) {
            setDefaultShadowColor();
        }
    }

    private void init() {
        mRawCardNumber = "";

        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_creditcard, this, true);

    }

    private void init(AttributeSet attrs) {

        init();

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.creditcard, 0, 0);

        String cardHolderName = a.getString(R.styleable.creditcard_card_holder_name);
        String expiry = a.getString(R.styleable.creditcard_card_expiration);
        String cardNumber = a.getString(R.styleable.creditcard_card_number);
        int cvv = a.getInt(R.styleable.creditcard_cvv, 0);
        int cardSide = a.getInt(R.styleable.creditcard_card_side, CreditCardUtils.CARD_SIDE_FRONT);

        //resize width
        try {
            mPercentWitdh = Float.parseFloat(GlobalData.getStringResource(RS.string.percent_ondefault));

            if (ZPWUtils.isTablet(GlobalData.getAppContext()))
                mPercentWitdh = Float.parseFloat(GlobalData.getStringResource(RS.string.percent_ontablet));

        } catch (Exception e) {
            Log.e(this, e);
            mPercentWitdh = 0.8f;
        }


        setCardNumber(cardNumber);
        setCVV(cvv);
        setCardDate(expiry);
        setCardHolderName(cardHolderName);
        if (cardSide == CreditCardUtils.CARD_SIDE_BACK) {
            showBackImmediate();
        }

        paintCard();

        ZPWUtils.applyFont(findViewById(TEXTVIEW_CARD_NUMBER_ID), GlobalData.getStringResource(RS.string.zpw_font_unisec));

        resizeFontCardNumber(mWidthCardView);

        a.recycle();

    }

    public void setOnClickOnCardView(CardGuiProcessor pGuiProcessor) {
        if (pGuiProcessor != null) {
            (findViewById(TEXTVIEW_CARD_NUMBER_ID)).setOnTouchListener(pGuiProcessor.getOnTouchOnCardView());
            (findViewById(TEXTVIEW_CARD_HOLDER_ID)).setOnTouchListener(pGuiProcessor.getOnTouchOnCardView());
            (findViewById(TEXTVIEW_CARD_EXPIRY_ID)).setOnTouchListener(pGuiProcessor.getOnTouchOnCardView());
        }
    }

    public TextView getViewCardNumNer() {
        return (TextView) findViewById(TEXTVIEW_CARD_NUMBER_ID);
    }

    public float getPercentWitdh() {
        return mPercentWitdh;
    }

    /***
     * resize font size cardnumber
     *
     * @param desiredWidth
     */
    private void resizeFontCardNumber(int desiredWidth) {
        if (desiredWidth > 0) {
            //reset font size
            desiredWidth -= GlobalData.getAppContext().getResources().getDimension(R.dimen.card_margin_left) * 2;

            ViewUtils.correctWidth((TextView) findViewById(TEXTVIEW_CARD_NUMBER_ID), desiredWidth);
        }
    }

    public void resize(float pPercentWidth) {
        mPercentWitdh = pPercentWidth;

        if (mPercentWitdh == -1) {
            mPercentWitdh = 0.8f;
        }

        final View frontContentView = findViewById(FRONT_CARD_ID);
        View cardViewLayout = findViewById(CARD_VIEW_ID);

        ViewUtils.setCardViewSize(GlobalData.getAppContext(), cardViewLayout, mPercentWitdh);

        mWidthCardView = ViewUtils.setCardViewSize(GlobalData.getAppContext(), frontContentView, mPercentWitdh);
    }

    private void flip(final boolean ltr, boolean isImmediate) {

        View layoutContainer = findViewById(R.id.card_outline_container);
        View frontView = findViewById(FRONT_CARD_OUTLINE_ID);
        View backView = findViewById(BACK_CARD_OUTLINE_ID);

        final View frontContentView = findViewById(FRONT_CARD_ID);
        final View backContentView = findViewById(BACK_CARD_ID);

        View layoutContentContainer = findViewById(R.id.card_container);


        if (isImmediate) {
            frontContentView.setVisibility(ltr ? VISIBLE : GONE);
            backContentView.setVisibility(ltr ? GONE : VISIBLE);

        } else {

            int duration = 600;

            FlipAnimator flipAnimator = new FlipAnimator(frontView, backView, frontView.getWidth() / 2, backView.getHeight() / 2);
            flipAnimator.setInterpolator(new OvershootInterpolator(0.5f));
            flipAnimator.setDuration(duration);

            if (ltr) {
                flipAnimator.reverse();
            }

            flipAnimator.setTranslateDirection(FlipAnimator.DIRECTION_Z);
            flipAnimator.setRotationDirection(FlipAnimator.DIRECTION_Y);
            layoutContainer.startAnimation(flipAnimator);

            FlipAnimator flipAnimator1 = new FlipAnimator(frontContentView, backContentView, frontContentView.getWidth() / 2, backContentView.getHeight() / 2);
            flipAnimator1.setInterpolator(new OvershootInterpolator(0.5f));
            flipAnimator1.setDuration(duration);

            if (ltr) {
                flipAnimator1.reverse();
            }

            flipAnimator1.setTranslateDirection(FlipAnimator.DIRECTION_Z);
            flipAnimator1.setRotationDirection(FlipAnimator.DIRECTION_Y);

            layoutContentContainer.startAnimation(flipAnimator1);
        }

    }

    protected String createHightLighCardNumber(String pCardNumber) {
        String htmlCardNumber = pCardNumber;
        StringBuilder number = new StringBuilder();
        char[] number_arr = pCardNumber.toCharArray();
        int lastXX = 0;

        boolean hasSpace = false;

        for (int i = 0; i < number_arr.length; i++) {
            if (number_arr[i] != CreditCardUtils.CHAR_X) {
                number.append(String.valueOf(number_arr[i]));
            } else {
                lastXX = i;
                break;
            }
        }
        //has space
        if (number.length() > 0 && number.substring(number.toString().length() - 1).equals(CreditCardUtils.SPACE_SEPERATOR)) {
            hasSpace = true;
            number.deleteCharAt(number.length() - 1);
        } else if (number.length() == 20) {
            hasSpace = true;
            number.deleteCharAt(number.length() - 1);
            number.append(CreditCardUtils.SPACE_SEPERATOR);
            number.append(number_arr[number_arr.length - 1]);
        }

        if (!TextUtils.isEmpty(number.toString())) {
            String htmlNumber = createHighLightText(number.toString(), mCardColorText);
            //plus space again
            if (hasSpace) {
                htmlNumber += CreditCardUtils.SPACE_SEPERATOR;
            }
            htmlCardNumber = htmlNumber;

            if (!TextUtils.isEmpty(htmlNumber) && lastXX < pCardNumber.length() && lastXX > 0) {
                htmlCardNumber += pCardNumber.substring(lastXX);
            }
        }
        return htmlCardNumber;

    }

    protected String createHighLightText(String pText, CardColorText pCardColorText) {

        if (!TextUtils.isEmpty(pText) && pCardColorText != null) {
            String colorHighline = ZPWUtils.getStringColor(getResources().getColor(pCardColorText.highlineColor));
            if (pText.length() == 1) {
                pText = "<font color='" + colorHighline + "'>" + pText + "</font>";
            } else {
                int indexCharacter = pText.length() - 1;

                //get last character
                String lastCharacter = pText.substring(indexCharacter);

                String lastLeave = "";

                if (!Character.isLetterOrDigit(lastCharacter.toCharArray()[0])) {
                    //get next character

                    if (pText.length() - 2 > 0 && !pText.substring(pText.length() - 1).equalsIgnoreCase(CreditCardUtils.SLASH_SEPERATOR)) {
                        indexCharacter = pText.length() - 2;
                        lastCharacter = pText.substring(indexCharacter);
                        lastLeave = pText.substring(pText.length() - 1);

                        if (lastCharacter.equalsIgnoreCase(CreditCardUtils.SPACE_SEPERATOR)) {
                            if (pText.length() - 3 > 0) {
                                indexCharacter = pText.length() - 3;
                                lastCharacter = pText.substring(indexCharacter);
                                lastLeave = pText.substring(pText.length() - 2);
                            }
                        }

                    }
                }

                lastCharacter = "<font color='" + colorHighline + "'>" + lastCharacter + "</font>";

                pText = pText.substring(0, indexCharacter) + lastCharacter + lastLeave;
            }
        }

        return pText;
    }

    public void setCardNumberNoPaintCard(String rawCardNumber) {
        this.mRawCardNumber = rawCardNumber == null ? "" : rawCardNumber;

        String newCardNumber = mRawCardNumber;

        for (int i = mRawCardNumber.length(); i < 16; i++) {
            newCardNumber += CreditCardUtils.CHAR_X;
        }

        String cardNumber = CreditCardUtils.handleCardNumber(newCardNumber, CreditCardUtils.SPACE_SEPERATOR);

        ((TextView) findViewById(TEXTVIEW_CARD_NUMBER_ID)).setText(Html.fromHtml(createHightLighCardNumber(cardNumber)));

        resize(mPercentWitdh);

        if (newCardNumber.length() >= 16) {
            resizeFontCardNumber(mWidthCardView);
        }
    }

    public void clearHighLightCardNumber() {
        this.mRawCardNumber = getCardNumber();

        String newCardNumber = mRawCardNumber;

        for (int i = mRawCardNumber.length(); i < 16; i++) {
            newCardNumber += CreditCardUtils.CHAR_X;
        }

        String cardNumber = CreditCardUtils.handleCardNumber(newCardNumber, CreditCardUtils.SPACE_SEPERATOR);

        ((TextView) findViewById(TEXTVIEW_CARD_NUMBER_ID)).setText(cardNumber);

    }

    public void setCVV(int cvvInt) {

        if (cvvInt == 0) {
            setCVV("");
        } else {
            String cvv = String.valueOf(cvvInt);
            setCVV(cvv);
        }

    }

    public void showFront() {
        flip(true, false);
    }

    public void setHintTextIssue() {

        ((TextView) findViewById(TEXTVIEW_CARD_EXPIRY_ID)).setHint(R.string.card_date_hint_local);
    }

    public void hideCardDate() {
        findViewById(TEXTVIEW_CARD_EXPIRY_ID).setVisibility(GONE);
    }

    public void visibleCardDate() {
        findViewById(TEXTVIEW_CARD_EXPIRY_ID).setVisibility(VISIBLE);
    }

    public void setHintTextExpire() {

        ((TextView) findViewById(TEXTVIEW_CARD_EXPIRY_ID)).setHint(R.string.card_date_hint_expire);
    }

    public void showFrontImmediate() {
        flip(true, true);
    }

    public void showBack() {
        flip(false, false);
    }

    public void showBackImmediate() {
        flip(false, true);
    }

    public void clearHighLightCVV() {
        //CreditCardUtils.handleCardCVV(getCVV())
        ((TextView) findViewById(TEXTVIEW_CARD_CVV_ID)).setText(getCVV());
    }

    public void clearHighLightCardDate() {
        ((TextView) findViewById(TEXTVIEW_CARD_EXPIRY_ID)).setText(getCardDate());
    }

    public void clearHighLightCardHolderName() {
        ((TextView) findViewById(TEXTVIEW_CARD_HOLDER_ID)).setText(getCardHolderName());
    }

    /***
     * update card date hint by atm or cc (issue date / expire date)
     *
     * @param bankCode
     */
    public void switchCardDateHintByBankCode(String bankCode) {

        if (bankCode.equalsIgnoreCase(Constants.CCCode)) {
            ((TextView) findViewById(TEXTVIEW_CARD_EXPIRY_ID)).setHint(R.string.card_date_hint_expire);
        } else {
            ((TextView) findViewById(TEXTVIEW_CARD_EXPIRY_ID)).setHint(R.string.card_date_hint_local);
        }
    }

    public void paintCard() {
        CardSelector card = selectCard();

        if (card == null) {
            Log.d(this, "===card=NULL===");
            return;
        }

        mCardColorText = card.getCardColorText();
        //can not detect
        if (mNeedToReveal != true && card.getResCardId() == R.drawable.card_color_round_rect_default) {
            mNeedToReveal = true;
            setCardNumber(getCardNumber());

        }

        if (mNeedToReveal && card.getResCardId() != R.drawable.card_color_round_rect_default) {
            mNeedToReveal = false;


            revealCardAnimation(card);
            setCardNumber(getCardNumber());
            return;
        }

        View cardContainer = findViewById(R.id.card_outline_container);
        View cardBack = findViewById(BACK_CARD_OUTLINE_ID);
        View cardFront = findViewById(FRONT_CARD_OUTLINE_ID);

        ImageView frontLogoImageView = (ImageView) cardContainer.findViewById(R.id.logo_img);
        frontLogoImageView.setImageResource(card.getResLogoId());
        ImageView centerImageView = (ImageView) cardContainer.findViewById(R.id.logo_center_img);
        centerImageView.setImageResource(card.getResCenterImageId());
        ImageView backLogoImageView = (ImageView) findViewById(BACK_CARD_ID).findViewById(R.id.logo_img);
        backLogoImageView.setImageResource(card.getResLogoId());
        cardBack.setBackgroundResource(card.getResCardId());
        cardFront.setBackgroundResource(card.getResCardId());
        setDefaultShadowColor();
    }

    /***
     * Set Hint text color
     */
    public void setDefaultShadowColor() {
        if (mCardColorText != null) {
            ((TextView) findViewById(TEXTVIEW_CARD_EXPIRY_ID)).setHintTextColor(getResources().getColor(mCardColorText.defaultColor));
            ((TextView) findViewById(TEXTVIEW_CARD_HOLDER_ID)).setHintTextColor(getResources().getColor(mCardColorText.defaultColor));
        }
    }

    public void revealCardAnimation(CardSelector pCard) {
        View cardFront = findViewById(FRONT_CARD_OUTLINE_ID);
        View cardContainer = findViewById(R.id.card_outline_container);
        paintCard();
        animateChange(cardContainer, cardFront, pCard.getResCardId());
    }

    public CardSelector selectCard() {
        return CardSelector.getInstance().selectCard(mRawCardNumber);
    }

    public void animateChange(final View cardContainer, final View v, final int drawableId) {
        showAnimation(cardContainer, v, drawableId);
    }

    public void showAnimation(final View cardContainer, final View v, final int drawableId) {

        final View mRevealView = v;
        mRevealView.setBackgroundResource(drawableId);

        int duration = 1000;
        int cx = mRevealView.getLeft();
        int cy = mRevealView.getTop();

        int radius = Math.max(mRevealView.getWidth(), mRevealView.getHeight()) * 4;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {


            SupportAnimator animator =
                    ViewAnimationUtils.createCircularReveal(mRevealView, cx, cy, 0, radius);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(duration);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    cardContainer.setBackgroundResource(drawableId);
                }
            }, duration);

            mRevealView.setVisibility(View.VISIBLE);
            animator.start();

        } else {
            Animator anim = android.view.ViewAnimationUtils.createCircularReveal(mRevealView, cx, cy, 0, radius);
            mRevealView.setVisibility(View.VISIBLE);
            anim.setDuration(duration);
            anim.start();
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    cardContainer.setBackgroundResource(drawableId);
                }
            });
        }
    }

    public String getCardNumber() {
        return mRawCardNumber;
    }

    public void setCardNumber(String rawCardNumber) {
        this.mRawCardNumber = rawCardNumber == null ? "" : rawCardNumber;

        String newCardNumber = mRawCardNumber;

        for (int i = mRawCardNumber.length(); i < 16; i++) {
            newCardNumber += CreditCardUtils.CHAR_X;
        }

        String cardNumber = CreditCardUtils.handleCardNumber(newCardNumber, CreditCardUtils.SPACE_SEPERATOR);

        ((TextView) findViewById(TEXTVIEW_CARD_NUMBER_ID)).setText(Html.fromHtml(createHightLighCardNumber(cardNumber)));

        paintCard();

        resize(mPercentWitdh);

        if (newCardNumber.length() >= 16) {
            resizeFontCardNumber(mWidthCardView);
        }
    }


}
