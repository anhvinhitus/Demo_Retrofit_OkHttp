package vn.com.zalopay.wallet.view.custom.cardview;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.business.behavior.gateway.BankLoader;
import vn.com.zalopay.wallet.business.channel.creditcard.CreditCardCheck;
import vn.com.zalopay.wallet.business.channel.localbank.BankCardCheck;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.base.CardColorText;
import vn.com.zalopay.wallet.business.entity.enumeration.ECardChannelType;
import vn.com.zalopay.wallet.listener.ILoadBankListListener;
import vn.com.zalopay.wallet.utils.Log;

public class CardSelector {
    private static CardColorText CardColorTextDefault = new CardColorText(R.color.default_color_text_normal, R.color.default_color_text_highline, R.color.default_color_text_selected);
    public static final CardSelector DEFAULT = new CardSelector(R.drawable.card_color_round_rect_default, android.R.color.transparent, R.drawable.bg_logo_card_default, CardColorTextDefault);

    private static HashMap<String, CardSelector> cardSelectorHashMap;

    private static CardSelector _object;
    private int mResCardId;
    private int mResCenterImageId;
    private int mResLogoId;
    private CardColorText mCardColorText;
    private ILoadBankListListener mLoadBankListListener = new ILoadBankListListener() {
        @Override
        public void onProcessing() {
            Log.d(this, "===loading bank list===");
        }

        @Override
        public void onComplete() {
            Log.d(this, "===load bank list onComplete===");
            populateCardSelector();
        }

        @Override
        public void onError(String pMessage) {

            Log.d(this, "===load bank list error " + pMessage);
        }
    };

    public CardSelector() {
        cardSelectorHashMap = new HashMap<>();

        //fill bankcode and selector
        if (BankLoader.existedBankListOnMemory()) {
            populateCardSelector();
        } else {
            //reload bank map
            BankLoader.loadBankList(mLoadBankListListener);
        }

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

    public CardSelector detectCardType(String pCardNumber) {
        String bankCode = BankCardCheck.getInstance().getCodeBankForVerify();

        if (GlobalData.isLinkCardChannel()) {
            bankCode = BankCardCheck.getInstance().getCodeBankForVerify();

            if (TextUtils.isEmpty(bankCode)) {
                bankCode = CreditCardCheck.getInstance().getCodeBankForVerify();
            }
        } else if (GlobalData.cardChannelType == ECardChannelType.ATM) {
            bankCode = BankCardCheck.getInstance().getCodeBankForVerify();
        } else if (GlobalData.cardChannelType == ECardChannelType.CC) {
            bankCode = CreditCardCheck.getInstance().getCodeBankForVerify();
        }

        if (!TextUtils.isEmpty(bankCode)) {
            return cardSelectorHashMap.get(bankCode);
        } else
            return DEFAULT;
    }

    public CardSelector selectCard(String pCardNumber) {
        if (!TextUtils.isEmpty(pCardNumber) && pCardNumber.length() >= 1) {
            CardSelector selector = detectCardType(pCardNumber);
            return selector;
        }
        Log.d("selectCard=====", "Return DEFAULT ");
        return DEFAULT;
    }

    protected CardSelector createCardSelector(String pBankCode) {
        CardSelector card = null;
        CardColorText cardColorText = null;
        //Bitmap bitmap = ResourceManager.getImage(String.format("bank_%s%s",pBankCode, Constants.BITMAP_EXTENSION));

        if (pBankCode.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_bankcode_viettinbank))) {
            //card = new CardSelector(R.drawable.card_color_round_rect_viettin,  android.R.color.transparent, bitmap);
            cardColorText = new CardColorText(R.color.viettin_color_text_normal, R.color.viettin_color_text_highline, R.color.viettin_color_text_selected);
            card = new CardSelector(R.drawable.card_color_round_rect_viettin, android.R.color.transparent, R.drawable.ic_viettinbank, cardColorText);
        } else if (pBankCode.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_bankcode_sacombank))) {
            //card = new CardSelector(R.drawable.card_color_round_rect_sacom,  android.R.color.transparent, bitmap);
            cardColorText = new CardColorText(R.color.sacom_color_text_normal, R.color.sacom_color_text_highline, R.color.sacom_color_text_selected);
            card = new CardSelector(R.drawable.card_color_round_rect_sacom, android.R.color.transparent, R.drawable.ic_sacombank, cardColorText);
        } else if (pBankCode.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_bankcode_vietcombank))) {
            //card = new CardSelector(R.drawable.card_color_round_rect_sacom,  android.R.color.transparent, bitmap);
            cardColorText = new CardColorText(R.color.sacom_color_text_normal, R.color.master_color_text_highline, R.color.sacom_color_text_selected);
            card = new CardSelector(R.drawable.card_color_round_rect_sacom, android.R.color.transparent, R.drawable.ic_zp_vcb, cardColorText);
        } else if (pBankCode.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_bankcode_commercialbank))) {
            //card = new CardSelector(R.drawable.card_color_round_rect_sacom,  android.R.color.transparent, bitmap);
            cardColorText = new CardColorText(R.color.commercial_color_text_normal, R.color.commercial_color_text_highline, R.color.commercial_color_text_selected);
            card = new CardSelector(R.drawable.card_color_round_rect_commercial, android.R.color.transparent, R.drawable.ic_sgcb, cardColorText);
        } else if (pBankCode.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_bankcode_bidv))) {
            //card = new CardSelector(R.drawable.card_color_round_rect_sacom,  android.R.color.transparent, bitmap);
            cardColorText = new CardColorText(R.color.bidv_color_text_normal, R.color.bidv_color_text_highline, R.color.bidv_color_text_selected);
            card = new CardSelector(R.drawable.card_color_round_rect_bidv, android.R.color.transparent, R.drawable.ic_bidv, cardColorText);
        } else if (pBankCode.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_bankcode_visa))) {
            //card = new CardSelector(R.drawable.card_color_round_rect_visa, android.R.color.transparent, bitmap);
            cardColorText = new CardColorText(R.color.visa_color_text_normal, R.color.visa_color_text_highline, R.color.visa_color_text_selected);
            card = new CardSelector(R.drawable.card_color_round_rect_visa, android.R.color.transparent, R.drawable.ic_visa, cardColorText);
        } else if (pBankCode.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_bankcode_master))) {
            //card = new CardSelector(R.drawable.card_color_round_rect_master, android.R.color.transparent, bitmap);
            cardColorText = new CardColorText(R.color.master_color_text_normal, R.color.master_color_text_highline, R.color.master_color_text_selected);
            card = new CardSelector(R.drawable.card_color_round_rect_master, android.R.color.transparent, R.drawable.ic_mastercard, cardColorText);
        } else if (pBankCode.equalsIgnoreCase(GlobalData.getStringResource(RS.string.zpw_string_bankcode_jcb))) {
            //card = new CardSelector(R.drawable.card_color_round_rect_green,  R.drawable.img_amex_center_face, bitmap);
            cardColorText = new CardColorText(R.color.jcb_color_text_normal, R.color.jcb_color_text_highline, R.color.jcb_color_text_selected);
            card = new CardSelector(R.drawable.card_color_round_rect_jcb, android.R.color.transparent, R.drawable.ic_jcb, cardColorText);
        }

        return card;
    }

    protected void addCardSelectorToHashMap(String pBankCode) {
        Log.d(this, "===addCardSelectorToHashMap pBankCode===" + pBankCode);

        CardSelector cardSelector = createCardSelector(pBankCode);

        if (cardSelector != null) {
            cardSelectorHashMap.put(pBankCode, cardSelector);
        }
    }

    protected void populateCardSelector() {
        if (BankLoader.existedBankListOnMemory()) {
            Iterator it = BankLoader.mapBank.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();

                String bankCode = String.valueOf(pair.getValue());

                if (!TextUtils.isEmpty(bankCode)) {
                    addCardSelectorToHashMap(bankCode);
                }
            }
        }

        //credit card
        String bankCodeVisa = GlobalData.getStringResource(RS.string.zpw_string_bankcode_visa);
        String bankCodeMaster = GlobalData.getStringResource(RS.string.zpw_string_bankcode_master);
        String bankCodeJcb = GlobalData.getStringResource(RS.string.zpw_string_bankcode_jcb);

        if (!TextUtils.isEmpty(bankCodeVisa)) {
            addCardSelectorToHashMap(bankCodeVisa);
        }
        if (!TextUtils.isEmpty(bankCodeMaster)) {
            addCardSelectorToHashMap(bankCodeMaster);
        }
        if (!TextUtils.isEmpty(bankCodeJcb)) {
            addCardSelectorToHashMap(bankCodeJcb);
        }
    }

    public int getResCardId() {
        return mResCardId;
    }

    public void setResCardId(int mResCardId) {
        this.mResCardId = mResCardId;
    }

    public int getResCenterImageId() {
        return mResCenterImageId;
    }

    public void setResCenterImageId(int mResCenterImageId) {
        this.mResCenterImageId = mResCenterImageId;
    }

    public int getResLogoId() {
        return mResLogoId;
    }

    ;

    public void setResLogoId(int mResLogoId) {
        this.mResLogoId = mResLogoId;
    }

    public CardColorText getCardColorText() {
        return mCardColorText;
    }
}
