package vn.com.zalopay.wallet.ui.channel;

import android.net.Uri;
import android.support.annotation.IdRes;
import android.support.design.widget.TextInputLayout;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.facebook.drawee.view.SimpleDraweeView;

import timber.log.Timber;
import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.business.data.GlobalData;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DDynamicViewGroup;
import vn.com.zalopay.wallet.business.entity.staticconfig.page.DStaticViewGroup;
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

    public void renderByResource(String screenName, DStaticViewGroup pAdditionStaticViewGroup,
                                 DDynamicViewGroup pAdditionDynamicViewGroup) throws Exception {
        try {
            Log.d(this, "start render screen name", screenName);
            long time = System.currentTimeMillis();
            ResourceManager resourceManager = ResourceManager.getInstance(screenName);
            if (resourceManager != null) {
                mResourceRender = resourceManager.produceRendering(this);
                if (mResourceRender != null) {
                    mResourceRender.render();
                    mResourceRender.render(pAdditionStaticViewGroup, pAdditionDynamicViewGroup);
                } else {
                    Timber.d("resource render is null");
                }
            } else {
                Timber.d("resource manager is null");
            }
            Log.d(this, "render resource: Total time:", (System.currentTimeMillis() - time));
        } catch (Exception e) {
            throw e;
        }
    }

    public void renderKeyBoard() {
        if (mResourceRender != null) {
            mResourceRender.renderKeyBoard();
        }
    }

    // fresco load Uri
    protected SimpleDraweeView loadIntoView(@IdRes int viewId, String uri) {
        SimpleDraweeView view = (SimpleDraweeView) findViewById(viewId);
        view.setImageURI(Uri.parse(uri));
        return view;
    }

    /*
     * set keyboard type for edittext from config.json
     */
    public View setKeyBoard(String pStrID, @KeyboardType int pKeyBoardType) {
        View view = findViewById(pStrID);
        if (view == null) {
            return view;
        }
        if (pKeyBoardType == KeyboardType.NUMBER && view instanceof EditText) {
            //user using the laban key for example
            if (!SdkUtils.useDefaultKeyBoard(GlobalData.getAppContext())) {
                ((EditText) view).setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                ((EditText) view).setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            }
        } else if (pKeyBoardType == KeyboardType.TEXT && view instanceof EditText) {
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
        ResourceManager.loadImageIntoView(view, pImageName);
    }

    public View findViewById(String pName) {
        return mRootView.findViewById(RS.getID(pName));
    }

    public View findViewById(int pId) {
        return mRootView.findViewById(pId);
    }
}
