package vn.com.zalopay.wallet.view.component.activity;

import android.app.Activity;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import java.util.List;
import java.util.Map.Entry;

import vn.com.zalopay.wallet.business.channel.localbank.BankCardCheck;
import vn.com.zalopay.wallet.business.dao.ResourceManager;
import vn.com.zalopay.wallet.business.entity.enumeration.EKeyBoardType;
import vn.com.zalopay.wallet.business.entity.staticconfig.DKeyBoardConfig;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DDynamicEditText;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DDynamicViewGroup;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DStaticView;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DStaticViewGroup;
import vn.com.zalopay.wallet.utils.GsonUtils;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.view.custom.VPaymentDrawableEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentValidDateEditText;

public class ActivityRendering {

    private ResourceManager mResourceManager = null;
    private BasePaymentActivity mOwnerActivity = null;

    public ActivityRendering(ResourceManager pResourceManager, BasePaymentActivity pOwnerActivity) {
        this.mResourceManager = pResourceManager;
        this.mOwnerActivity = pOwnerActivity;
    }

    public Activity getOwnerActivity() {
        return mOwnerActivity;
    }

    public void setOwnerActivity(BasePaymentActivity mOwnerActivity) {
        this.mOwnerActivity = mOwnerActivity;
    }

    public void render() {
        if (mResourceManager == null)
            return;

        render(mResourceManager.getStaticView(), mResourceManager.getDynamicView());
    }

    public void renderKeyBoard() {
        List<DKeyBoardConfig> keyBoardConfigs = mResourceManager.getKeyBoardConfig();
        if (keyBoardConfigs != null && keyBoardConfigs.size() > 0 && mOwnerActivity != null) {
            for (DKeyBoardConfig keyboard : keyBoardConfigs) {
                Log.d("renderKeyBoard", "preparing to set keyboard " + (keyboard != null ? GsonUtils.toJsonString(keyboard) : "NULL"));

                /***
                 * "keyboard":[
                 * //set keyboad for bank's view with bankcode
                 {
                 "bankcode":"123PVTB",
                 "view": "zpsdk_otp_ctl",
                 "type": "1"
                 },
                 //set keyboard for someview
                 {
                 "view": "edittext_localcard_number",
                 "type": "2"
                 }
                 ],
                 */
                //set keyboard for some view
                if (TextUtils.isEmpty(keyboard.bankcode)) {
                    mOwnerActivity.setKeyBoard(keyboard.view, EKeyBoardType.fromString(keyboard.type));
                    Log.d("renderKeyBoard", "set keyboard for view" + (keyboard != null ? GsonUtils.toJsonString(keyboard) : "NULL"));
                    continue;
                }
                //set keyboard by bank
                if (BankCardCheck.getInstance().isDetected() && BankCardCheck.getInstance().getDetectBankCode().equalsIgnoreCase(keyboard.bankcode)) {
                    mOwnerActivity.setKeyBoard(keyboard.view, EKeyBoardType.fromString(keyboard.type));
                    Log.d("renderKeyBoard", "set keyboard for bank" + (BankCardCheck.getInstance().getDetectBankCode() + "-" + (keyboard != null ? GsonUtils.toJsonString(keyboard) : "NULL")));
                }
            }
        }
    }

    public void render(DStaticViewGroup pStaticViewGroup, DDynamicViewGroup pDynamicViewGroup) {
        if (pStaticViewGroup == null && pDynamicViewGroup == null)
            return;

        View contentView = this.mOwnerActivity.findViewById(android.R.id.content).getRootView();

        if (contentView != null) {
            renderStaticView(pStaticViewGroup);
            renderDynamicView(pDynamicViewGroup);
            renderKeyBoard();
        }
    }

    private void renderStaticView(DStaticViewGroup pStaticViewGroup) {
        if (pStaticViewGroup != null) {
            renderImageView(mOwnerActivity, pStaticViewGroup.ImageView);
            renderTextView(mOwnerActivity, pStaticViewGroup.TextView);
        }
    }

    private void renderDynamicView(DDynamicViewGroup pDynamicViewGroup) {
        if (pDynamicViewGroup != null) {
            //set rule for edittext from bundle.
            if (pDynamicViewGroup.EditText != null && pDynamicViewGroup.EditText.size() > 0) {
                for (DDynamicEditText editText : pDynamicViewGroup.EditText) {
                    View view = mOwnerActivity.findViewById(mOwnerActivity.getViewID(editText.id));

                    //Log.d("renderDynamicView",editText.id + " is "+ ((view != null) ? view.toString(): "null"));

                    if (mOwnerActivity instanceof PaymentChannelActivity) {
                        if (view instanceof VPaymentDrawableEditText || view instanceof VPaymentValidDateEditText) {
                            VPaymentEditText paymentEditText = (VPaymentEditText) view;
                            paymentEditText.init(editText, mOwnerActivity.getAdapter());
                        }
                    }
                }
            }

            //add action keyboard. the done action if this is the last view
            if (pDynamicViewGroup.View != null && pDynamicViewGroup.View.size() > 0) {
                View view = null;
                View lastView = null;

                for (Entry<String, Boolean> entry : pDynamicViewGroup.View.entrySet()) {
                    view = mOwnerActivity.setView(entry.getKey(), entry.getValue());

                    if (entry.getValue()) {
                        if (view instanceof EditText && entry.getValue()) {
                            ((EditText) view).setImeOptions(EditorInfo.IME_ACTION_NEXT | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                        } else if (view instanceof TextInputLayout && ((TextInputLayout) view).getChildAt(0) instanceof EditText) {
                            EditText editText = (EditText) ((TextInputLayout) view).getChildAt(0);

                            editText.setImeOptions(EditorInfo.IME_ACTION_NEXT | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

                        }
                        lastView = view;
                    }
                }
                //SET DONE ACTION ON THE LAST ONE
                if (lastView != null) {
                    if (lastView instanceof EditText) {
                        ((EditText) lastView).setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                    } else if (lastView instanceof TextInputLayout && ((TextInputLayout) lastView).getChildAt(0) instanceof EditText) {
                        EditText editText = (EditText) ((TextInputLayout) lastView).getChildAt(0);

                        editText.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

                    }
                }
            }
        }
    }

    private void renderImageView(BasePaymentActivity pActivity, List<DStaticView> pImgViewList) {
        if (pImgViewList == null) {
            return;
        }
        for (DStaticView imgView : pImgViewList) {
            pActivity.setImage(imgView.id, ResourceManager.getImage(imgView.value));
        }
    }

    private void renderTextView(BasePaymentActivity pActivity, List<DStaticView> pTxtViewList) {
        if (pTxtViewList == null) {
            return;
        }
        for (DStaticView txtView : pTxtViewList) {
            pActivity.setText(txtView.id, txtView.value);
        }
    }

}
