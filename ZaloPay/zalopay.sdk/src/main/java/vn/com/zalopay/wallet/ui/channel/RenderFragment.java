package vn.com.zalopay.wallet.ui.channel;

import android.content.Context;
import android.content.res.ColorStateList;
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

import java.lang.reflect.Field;

import vn.com.zalopay.utility.SdkUtils;
import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.repository.ResourceManager;
import vn.com.zalopay.wallet.business.data.Log;
import vn.com.zalopay.wallet.business.data.RS;
import vn.com.zalopay.wallet.constants.KeyboardType;
import vn.com.zalopay.wallet.ui.GenericFragment;
import vn.com.zalopay.wallet.ui.IPresenter;
import vn.com.zalopay.wallet.view.custom.VPaymentEditText;

/**
 * Created by chucvv on 6/24/17.
 */

public abstract class RenderFragment<T extends IPresenter> extends GenericFragment<T> {
    protected ResourceRender mResourceRender;
    protected View mRootView;

    public void setTextInputLayoutHint(EditText pEditext, String pMessage, Context pContext) {
        if (pEditext == null) {
            return;
        }

        if (pEditext instanceof VPaymentEditText && ((VPaymentEditText) pEditext).getTextInputLayout() instanceof TextInputLayout) {
            try {
                TextInputLayout textInputLayout = ((VPaymentEditText) pEditext).getTextInputLayout();
                int color = pContext.getResources().getColor(R.color.color_primary);
                int textColor = pContext.getResources().getColor(R.color.text_color);
                Field fDefaultTextColor = TextInputLayout.class.getDeclaredField("mDefaultTextColor");
                fDefaultTextColor.setAccessible(true);
                fDefaultTextColor.set(textInputLayout, new ColorStateList(new int[][]{{0}}, new int[]{textColor}));

                Field fFocusedTextColor = TextInputLayout.class.getDeclaredField("mFocusedTextColor");
                fFocusedTextColor.setAccessible(true);
                fFocusedTextColor.set(textInputLayout, new ColorStateList(new int[][]{{0}}, new int[]{color}));

                int paddingLeft = pEditext.getPaddingLeft();
                int paddingTop = pEditext.getPaddingTop();
                int paddingRight = pEditext.getPaddingRight();
                int paddingBottom = pEditext.getPaddingBottom();

                pEditext.setBackground(pContext.getResources().getDrawable(R.drawable.txt_bottom_default_style));
                //restore padding
                pEditext.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
                textInputLayout.refreshDrawableState();
                textInputLayout.setHint(!TextUtils.isEmpty(pMessage) ? pMessage : (textInputLayout.getTag() != null ? textInputLayout.getTag().toString() : null));
            } catch (Exception ignored) {
            }
        }
    }

    public void setTextInputLayoutHintError(EditText pEditext, String pError, Context pContext) {
        if (pEditext == null) {
            return;
        }
        if (pEditext instanceof VPaymentEditText && ((VPaymentEditText) pEditext).getTextInputLayout() instanceof TextInputLayout) {
            try {
                TextInputLayout textInputLayout = ((VPaymentEditText) pEditext).getTextInputLayout();
                int color = pContext.getResources().getColor(R.color.holo_red_light);
                Field fDefaultTextColor = TextInputLayout.class.getDeclaredField("mDefaultTextColor");
                fDefaultTextColor.setAccessible(true);
                fDefaultTextColor.set(textInputLayout, new ColorStateList(new int[][]{{0}}, new int[]{color}));
                Field fFocusedTextColor = TextInputLayout.class.getDeclaredField("mFocusedTextColor");
                fFocusedTextColor.setAccessible(true);
                fFocusedTextColor.set(textInputLayout, new ColorStateList(new int[][]{{0}}, new int[]{color}));
                int paddingLeft = pEditext.getPaddingLeft();
                int paddingTop = pEditext.getPaddingTop();
                int paddingRight = pEditext.getPaddingRight();
                int paddingBottom = pEditext.getPaddingBottom();
                pEditext.setBackground(pContext.getResources().getDrawable(R.drawable.txt_bottom_error_style));
                //restore padding
                pEditext.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
                textInputLayout.refreshDrawableState();
                textInputLayout.setHint(pError);
            } catch (Exception ignored) {
            }
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

    /***
     * set keyboard type for edittext from config.json
     * @param pStrID
     * @param pKeyBoardType
     * @return
     */
    public View setKeyBoard(String pStrID, @KeyboardType int pKeyBoardType) {
        View view = findViewById(pStrID);
        if (view == null) {
            return view;
        }
        if (pKeyBoardType == KeyboardType.NUMBER && view instanceof EditText) {
            //user using the laban key for example
            if (!SdkUtils.useDefaultKeyBoard(getContext())) {
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
        if (view instanceof ToggleButton) {
            ((ToggleButton) view).setText(pText);
        } else if (view instanceof EditText) {
            EditText editText = ((EditText) view);
            if (editText instanceof VPaymentEditText && ((VPaymentEditText) editText).getTextInputLayout() instanceof TextInputLayout) {
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
    }

    public void setText(int pID, String pText) {
        View view = mRootView.findViewById(pID);
        if (view == null) {
            return;
        }
        if (view instanceof EditText) {
            EditText editText = (EditText) view;
            if (editText instanceof VPaymentEditText && ((VPaymentEditText) editText).getTextInputLayout() instanceof TextInputLayout) {
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
    }

    public void setImage(String pId, String pImageName) {
        View view = findViewById(pId);
        if (view == null) {
            Log.e(this, "view not found", pId);
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
