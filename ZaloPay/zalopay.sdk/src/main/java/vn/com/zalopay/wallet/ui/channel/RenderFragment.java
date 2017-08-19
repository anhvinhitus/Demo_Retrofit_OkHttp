package vn.com.zalopay.wallet.ui.channel;

import android.support.design.widget.TextInputLayout;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import timber.log.Timber;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.configure.GlobalData;
import vn.com.zalopay.wallet.configure.RS;
import vn.com.zalopay.wallet.entity.config.DynamicViewGroup;
import vn.com.zalopay.wallet.entity.config.StaticViewGroup;
import vn.com.zalopay.wallet.constants.KeyboardType;
import vn.com.zalopay.wallet.repository.ResourceManager;
import vn.com.zalopay.wallet.ui.GenericFragment;
import vn.com.zalopay.wallet.ui.IPresenter;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;

/*
 * Created by chucvv on 6/24/17.
 */

public abstract class RenderFragment<T extends IPresenter> extends GenericFragment<T> {
    protected ResourceRender mResourceRender;
    protected View mRootView;

    public void renderByResource(String screenName) throws Exception {
        try {
            renderByResource(screenName, null, null);
        } catch (Exception e) {
            throw e;
        }
    }

    public void renderByResource(String screenName, StaticViewGroup pAdditionStaticViewGroup,
                                 DynamicViewGroup pAdditionDynamicViewGroup) throws Exception {
        Timber.d("start render screen name %s", screenName);
        long time = System.currentTimeMillis();
        initResourceRender(screenName);
        if(mResourceRender == null){
            Timber.d("resource render is null");
            return;
        }
        mResourceRender.render();
        mResourceRender.render(pAdditionStaticViewGroup, pAdditionDynamicViewGroup);
        Timber.d("render resource: Total time: %s", (System.currentTimeMillis() - time));
    }

    private void initResourceRender(String screenName){
        ResourceManager resourceManager = ResourceManager.getInstance(screenName);
        if (resourceManager != null) {
            mResourceRender = resourceManager.produceRendering(this);
        }
    }

    public void renderKeyBoard(String screenName, String pBankCode) {
        if(mResourceRender == null){
            initResourceRender(screenName);
        }
        if(mResourceRender == null){
            Timber.d("resource render is null");
            return;
        }
        mResourceRender.renderKeyBoard(pBankCode);
    }

    /*
     * set keyboard type for edittext from config.json
     */
    public View setKeyBoard(String pStrID, @KeyboardType int pKeyBoardType) {
        View view = findViewById(pStrID);
        if (view == null) {
            return view;
        }
        if(!(view instanceof EditText)){
            return view;
        }
        if (pKeyBoardType == KeyboardType.NUMBER) {
            //user using the laban key for example
            if (!SdkUtils.useDefaultKeyBoard(GlobalData.getAppContext())) {
                ((EditText) view).setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                ((EditText) view).setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            }
            return view;
        }
        if (pKeyBoardType == KeyboardType.TEXT) {
            ((EditText) view).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        }
        return view;
    }

    public boolean isVisible(View view) {
        return view != null && view.getVisibility() == View.VISIBLE;
    }

    public void setTextHtml(int pId, String pHtmlText) {
        View view = mRootView.findViewById(pId);
        if (view != null && view instanceof TextView) {
            ((TextView) view).setText(Html.fromHtml(pHtmlText));
        }
    }


    public View setVisible(String pStrID, boolean pIsVisible) {
        View view = findViewById(pStrID);
        if (view == null) {
            return view;
        }
        if (pIsVisible) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
        return view;
    }

    public void setVisible(int pId, boolean pIsVisible) {
        View view = mRootView.findViewById(pId);
        if (view != null) {
            view.setVisibility(pIsVisible ? View.VISIBLE : View.GONE);
        }
    }

    public void setText(String pStrID, String pText) {
        View view = findViewById(pStrID);
        if (view == null) {
            return;
        }
        try {
            if (view instanceof ToggleButton) {
                ((ToggleButton) view).setText(pText);
            } else if (view instanceof EditText) {
                EditText editText = ((EditText) view);
                if (editText instanceof VPaymentEditText && ((VPaymentEditText) editText).getTextInputLayout() != null) {
                    TextInputLayout textInputLayout = ((VPaymentEditText) editText).getTextInputLayout();
                    if (textInputLayout != null) {
                        textInputLayout.setHint(pText);
                    }
                } else {
                    editText.setHint(pText);
                }
            } else if (view instanceof TextView) {
                ((TextView) view).setText(pText);
            }
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    public void setText(int pID, String pText) {
        View view = mRootView.findViewById(pID);
        if (view == null) {
            return;
        }
        try {
            if (view instanceof EditText) {
                EditText editText = (EditText) view;
                if (editText instanceof VPaymentEditText && ((VPaymentEditText) editText).getTextInputLayout() != null) {
                    TextInputLayout textInputLayout = ((VPaymentEditText) editText).getTextInputLayout();
                    if (textInputLayout != null) {
                        textInputLayout.setHint(pText);
                    }
                } else {
                    editText.setHint(pText);
                }
            } else if (view instanceof TextView) {
                ((TextView) view).setText(!TextUtils.isEmpty(pText) ? Html.fromHtml(pText) : pText);
            }
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    public void setImage(String pId, String pImageName) {
        View view = findViewById(pId);
        if (view == null) {
            Timber.d("view not found %s", pId);
            return;
        }
        ResourceManager.loadLocalSDKImage(view, pImageName);
    }

    public View findViewById(String pName) {
        return mRootView.findViewById(RS.getID(pName));
    }

    public View findViewById(int pId) {
        return mRootView.findViewById(pId);
    }
}
