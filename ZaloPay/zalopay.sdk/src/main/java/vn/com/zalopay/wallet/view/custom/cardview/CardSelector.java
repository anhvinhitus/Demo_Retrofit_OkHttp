package vn.com.zalopay.wallet.view.custom.cardview;

import android.os.Build;
import android.text.TextUtils;
import android.util.ArrayMap;

import java.util.HashMap;
import java.util.Map;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.configure.GlobalData;
import vn.com.zalopay.wallet.entity.CardColorText;
import vn.com.zalopay.wallet.card.BankDetector;
import vn.com.zalopay.wallet.card.CreditCardDetector;
import vn.com.zalopay.wallet.constants.CardChannel;
import vn.com.zalopay.wallet.constants.CardType;
import vn.com.zalopay.wallet.constants.TransactionType;
import vn.com.zalopay.wallet.controller.SDKApplication;

public class CardSelector {
    private static CardColorText CardColorTextDefault = new CardColorText(R.color.default_color_text_normal, R.color.default_color_text_highline, R.color.default_color_text_selected);
    private static final CardSelector DEFAULT = new CardSelector(R.drawable.card_color_round_rect_default, android.R.color.transparent, R.drawable.bg_logo_card_default, CardColorTextDefault);
    private static Map<String, CardSelector> cardSelectorHashMap;
    private static CardSelector _object;
    private int mResCardId;
    private int mResCenterImageId;
    private int mResLogoId;
    private CardColorText mCardColorText;

    public CardSelector() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            cardSelectorHashMap = new ArrayMap<>();
        } else {
            cardSelectorHashMap = new HashMap<>();
        }
        populateCardSelector();
    }

    public CardSelector(int mDrawableCard, int mDrawableCenterImage, int logoId, CardColorText pCardColorText) {
        this.mResCardId = mDrawableCard;
        this.mResCenterImageId = mDrawableCenterImage;
        this.mResLogoId = logoId;
        this.mCardColorText = pCardColorText;
    }

    public static CardSelector getInstance() {
        if (CardSelector._object == null)
            CardSelector._object = new CardSelector();

        return CardSelector._object;
    }

    private CardSelector detectCardType(@TransactionType int pTranstype) {
        String bankCode = BankDetector.getInstance().getCodeBankForVerifyCC();

        if (pTranstype == TransactionType.LINK) {
            bankCode = BankDetector.getInstance().getCodeBankForVerifyCC();
            if (TextUtils.isEmpty(bankCode)) {
                bankCode = CreditCardDetector.getInstance().getCodeBankForVerifyCC();
            }
        } else if (GlobalData.cardChannelType == CardChannel.ATM) {
            bankCode = BankDetector.getInstance().getCodeBankForVerifyCC();
        } else if (GlobalData.cardChannelType == CardChannel.CREDIT) {
            bankCode = CreditCardDetector.getInstance().getCodeBankForVerifyCC();
        }

        if (!TextUtils.isEmpty(bankCode)) {
            return cardSelectorHashMap.get(bankCode);
        } else
            return DEFAULT;
    }

    CardSelector selectCard(String pCardNumber) {
        if (!TextUtils.isEmpty(pCardNumber) && pCardNumber.length() >= 1) {
            return detectCardType(GlobalData.transtype());
        }
        return DEFAULT;
    }

    private CardSelector createCardSelector(String pBankCode) {
        CardSelector card = null;
        CardColorText cardColorText;
        switch (pBankCode) {
            case CardType.PVTB:
                cardColorText = new CardColorText(R.color.viettin_color_text_normal, R.color.viettin_color_text_highline, R.color.viettin_color_text_selected);
                card = new CardSelector(R.drawable.card_color_round_rect_viettin, android.R.color.transparent, R.drawable.ic_viettinbank, cardColorText);
                break;
            case CardType.PSCB:
                cardColorText = new CardColorText(R.color.sacom_color_text_normal, R.color.sacom_color_text_highline, R.color.sacom_color_text_selected);
                card = new CardSelector(R.drawable.card_color_round_rect_sacom, android.R.color.transparent, R.drawable.ic_sacombank, cardColorText);
                break;
            case CardType.PVCB:
                cardColorText = new CardColorText(R.color.sacom_color_text_normal, R.color.master_color_text_highline, R.color.sacom_color_text_selected);
                card = new CardSelector(R.drawable.card_color_round_rect_sacom, android.R.color.transparent, R.drawable.ic_zp_vcb, cardColorText);
                break;
            case CardType.PSGCB:
                cardColorText = new CardColorText(R.color.commercial_color_text_normal, R.color.commercial_color_text_highline, R.color.commercial_color_text_selected);
                card = new CardSelector(R.drawable.card_color_round_rect_commercial, android.R.color.transparent, R.drawable.ic_sgcb, cardColorText);
                break;
            case CardType.PBIDV:
                cardColorText = new CardColorText(R.color.bidv_color_text_normal, R.color.bidv_color_text_highline, R.color.bidv_color_text_selected);
                card = new CardSelector(R.drawable.card_color_round_rect_bidv, android.R.color.transparent, R.drawable.ic_bidv, cardColorText);
                break;
            case CardType.VISA:
                cardColorText = new CardColorText(R.color.visa_color_text_normal, R.color.visa_color_text_highline, R.color.visa_color_text_selected);
                card = new CardSelector(R.drawable.card_color_round_rect_visa, android.R.color.transparent, R.drawable.ic_visa, cardColorText);
                break;
            case CardType.MASTER:
                cardColorText = new CardColorText(R.color.master_color_text_normal, R.color.master_color_text_highline, R.color.master_color_text_selected);
                card = new CardSelector(R.drawable.card_color_round_rect_master, android.R.color.transparent, R.drawable.ic_mastercard, cardColorText);
                break;
        }
        return card;
    }

    private void addCardSelectorToHashMap(String pBankCode) {
        CardSelector cardSelector = createCardSelector(pBankCode);
        if (cardSelector != null) {
            cardSelectorHashMap.put(pBankCode, cardSelector);
        }
    }

    private void populateCardSelector() {
        Map<String, String> bankPrefix = SDKApplication.getApplicationComponent()
                .bankListInteractor()
                .getBankPrefix();
        if (bankPrefix != null) {
            for (Object o : bankPrefix.entrySet()) {
                Map.Entry pair = (Map.Entry) o;
                String bankCode = String.valueOf(pair.getValue());
                if (!TextUtils.isEmpty(bankCode)) {
                    addCardSelectorToHashMap(bankCode);
                }
            }
        }
        //credit card
        String bankCodeVisa = CardType.VISA;
        String bankCodeMaster = CardType.MASTER;
        if (!TextUtils.isEmpty(bankCodeVisa)) {
            addCardSelectorToHashMap(bankCodeVisa);
        }
        if (!TextUtils.isEmpty(bankCodeMaster)) {
            addCardSelectorToHashMap(bankCodeMaster);
        }
    }

    int getResCardId() {
        return mResCardId;
    }

    int getResCenterImageId() {
        return mResCenterImageId;
    }


    int getResLogoId() {
        return mResLogoId;
    }


    CardColorText getCardColorText() {
        return mCardColorText;
    }
}
