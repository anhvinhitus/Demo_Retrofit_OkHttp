package vn.com.zalopay.wallet.ui.channel;

import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import java.util.List;
import java.util.Map.Entry;

import timber.log.Timber;
import vn.com.zalopay.utility.GsonUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.repository.ResourceManager;
import vn.com.zalopay.wallet.entity.config.DKeyBoardConfig;
import vn.com.zalopay.wallet.entity.config.DynamicEditText;
import vn.com.zalopay.wallet.entity.config.DynamicViewGroup;
import vn.com.zalopay.wallet.entity.config.StaticView;
import vn.com.zalopay.wallet.entity.config.StaticViewGroup;
import vn.com.zalopay.wallet.view.custom.VPaymentDrawableEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;
import vn.com.zalopay.wallet.view.custom.VPaymentValidDateEditText;

public class ResourceRender {
    private ResourceManager mResourceManager = null;
    private RenderFragment mView = null;

    public ResourceRender(ResourceManager pResourceManager, RenderFragment renderFragment) {
        this.mResourceManager = pResourceManager;
        this.mView = renderFragment;
    }

    public void render() {
        if (mResourceManager == null) {
            return;
        }
        render(mResourceManager.getStaticView(), mResourceManager.getDynamicView());
    }

    public void renderKeyBoard(String pBankCode) {
        List<DKeyBoardConfig> keyBoardConfigs = mResourceManager.getKeyBoardConfig();
        if(keyBoardConfigs == null || keyBoardConfigs.size() <= 0){
            return;
        }
        if(mView == null){
            return;
        }
        for (DKeyBoardConfig keyboard : keyBoardConfigs) {
            Timber.d("render keyboard %s", GsonUtils.toJsonString(keyboard));
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
                mView.setKeyBoard(keyboard.view, keyboard.type);
                continue;
            }
            //set keyboard by bank
            if (!TextUtils.isEmpty(pBankCode) && pBankCode.equalsIgnoreCase(keyboard.bankcode)) {
                mView.setKeyBoard(keyboard.view, keyboard.type);
            }
        }
    }

    public void render(StaticViewGroup pStaticViewGroup, DynamicViewGroup pDynamicViewGroup) {
        if (pStaticViewGroup == null && pDynamicViewGroup == null) {
            return;
        }
        View contentView = mView.findViewById(R.id.supperRootView);
        if (contentView != null) {
            renderStaticView(pStaticViewGroup);
            renderDynamicView(pDynamicViewGroup);
        }
    }

    private void renderStaticView(StaticViewGroup pStaticViewGroup) {
        if (pStaticViewGroup != null) {
            renderImageView(mView, pStaticViewGroup.ImageView);
            renderTextView(mView, pStaticViewGroup.TextView);
        }
    }

    private void renderDynamicView(DynamicViewGroup pDynamicViewGroup) {
        if (pDynamicViewGroup != null) {
            //set rule for edittext from bundle.
            if (pDynamicViewGroup.EditText != null && pDynamicViewGroup.EditText.size() > 0) {
                for (DynamicEditText editText : pDynamicViewGroup.EditText) {
                    View view = mView.findViewById(editText.id);
                    //Log.d("renderDynamicView",editText.id + " is "+ ((view != null) ? view.toString(): "null"));
                    if (view instanceof VPaymentDrawableEditText || view instanceof VPaymentValidDateEditText) {
                        VPaymentEditText paymentEditText = (VPaymentEditText) view;
                        paymentEditText.init(editText);
                    }
                }
            }
            //add action keyboard. the done action if this is the last view
            if (pDynamicViewGroup.View != null && pDynamicViewGroup.View.size() > 0) {
                View view;
                View lastView = null;
                for (Entry<String, Boolean> entry : pDynamicViewGroup.View.entrySet()) {
                    view = mView.setVisible(entry.getKey(), entry.getValue());
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

    private void renderImageView(RenderFragment renderFragment, List<StaticView> pImgViewList) {
        if (pImgViewList == null) {
            return;
        }
        for (StaticView imgView : pImgViewList) {
            renderFragment.setImage(imgView.id, imgView.value);
        }
    }

    private void renderTextView(RenderFragment renderFragment, List<StaticView> pTxtViewList) {
        if (pTxtViewList == null) {
            return;
        }
        for (StaticView txtView : pTxtViewList) {
            renderFragment.setText(txtView.id, txtView.value);
        }
    }

}
