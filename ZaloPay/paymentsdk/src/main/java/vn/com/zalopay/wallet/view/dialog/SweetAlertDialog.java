package vn.com.zalopay.wallet.view.dialog;


import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Transformation;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import vn.com.zalopay.wallet.R;
import vn.com.zalopay.wallet.listener.ZPWOnDialogListener;
import vn.com.zalopay.wallet.utils.Log;
import vn.com.zalopay.wallet.utils.ZPWUtils;

public class SweetAlertDialog extends Dialog implements View.OnClickListener {
    public static final int NORMAL_TYPE = 0;
    public static final int ERROR_TYPE = 1;
    public static final int SUCCESS_TYPE = 2;
    public static final int WARNING_TYPE = 3;
    public static final int INFO_NO_ICON = 4;
    public static final int PROGRESS_TYPE = 5;
    public static final int UPDATE_TYPE = 6;
    public static final int INFO_TYPE = 7;
    public static final int CUSTOM_CONTENT_VIEW = 8;
    public static final int UPDATE = 9;
    public static final int NO_INTERNET = 10;
    private View mDialogView;
    private View mLine;
    private LinearLayout mRootView;
    private LinearLayout mLayoutCustomButton;
    private RelativeLayout mLayoutTitleVer;
    private RelativeLayout mLayoutButton;
    private AnimationSet mModalInAnim;
    private AnimationSet mModalOutAnim;
    private Animation mOverlayOutAnim;
    private Animation mErrorInAnim;
    private AnimationSet mErrorXInAnim;
    private AnimationSet mSuccessLayoutAnimSet;
    private Animation mSuccessBowAnim;
    private TextView mContentTextView;
    private String mContentText;
    private boolean mShowCancel;
    private boolean mShowConfirm;
    private boolean mShowContent;
    private String mCancelText;
    private String mConfirmText;
    private String mUpdateText;
    private String mVersionText;
    private String mContentTextNointernet;
    private String mCheckBoxTitle;
    private View mViewCustom;
    private int mID;
    private int mAlertType;
    private FrameLayout mErrorFrame;
    private FrameLayout mSuccessFrame;
    private FrameLayout mNormalFrame;
    private FrameLayout mProgressFrame;
    private FrameLayout mUpdateFrame;
    private FrameLayout mInfoFrame;
    private FrameLayout mCustomFrame;
    private FrameLayout mInternetFrame;
    private FrameLayout mCustomContentFrame;
    private ImageView mErrorX;
    private ImageView mNormal_ic;
    private Drawable mCustomImgDrawable;
    private ImageView mCustomImage;
    private TextView mConfirmButton;
    private TextView mCancelButton;
    private TextView mUpdateButton;
    private TextView mTitleTextView;
    private TextView mVersionTextView;
    private String mTitleText;
    private String[] mArrButton;
    private FrameLayout mWarningFrame;
    private CheckBox mCheckBox;
    private OnSweetClickListener mCancelClickListener;
    private OnSweetClickListener mConfirmClickListener;
    private OnSweetClickListener mUpdateClickListener;
    private ZPWOnDialogListener mClickButtonArrListener;
    private boolean mCloseFromCancel;
    private boolean mIsChecked = false;
    private int backgroundResource;

    public SweetAlertDialog(Context context) {
        this(context, NORMAL_TYPE, R.style.alert_dialog);
    }

    public SweetAlertDialog(Context context, int alertType, int style) {
        super(context, style);

        setCancelable(true);
        setCanceledOnTouchOutside(false);

        int[] attrs = new int[]{R.attr.selectableItemBackground};
        TypedArray typedArray = context.obtainStyledAttributes(attrs);
        backgroundResource = typedArray.getResourceId(0, 0);

        mAlertType = alertType;
        mErrorInAnim = OptAnimationLoader.loadAnimation(getContext(), R.anim.error_frame_in);
        mErrorXInAnim = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.error_x_in);
        // 2.3.x system don't support alpha-animation on layer-list drawable
        // remove it from animation set
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            List<Animation> childAnims = mErrorXInAnim.getAnimations();
            int idx = 0;
            for (; idx < childAnims.size(); idx++) {
                if (childAnims.get(idx) instanceof AlphaAnimation) {
                    break;
                }
            }
            if (idx < childAnims.size()) {
                childAnims.remove(idx);
            }
        }
        mSuccessBowAnim = OptAnimationLoader.loadAnimation(getContext(), R.anim.success_bow_roate);
        mSuccessLayoutAnimSet = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.success_mask_layout);
        mModalInAnim = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.modal_in);
        mModalOutAnim = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.modal_out);
        mModalOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mDialogView.setVisibility(View.GONE);
                mDialogView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mCloseFromCancel) {
                            SweetAlertDialog.super.cancel();
                        } else {
                            SweetAlertDialog.super.dismiss();
                        }
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        // dialog overlay fade out

        if (mAlertType == SweetAlertDialog.PROGRESS_TYPE)
            return;

        mOverlayOutAnim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                WindowManager.LayoutParams wlp = getWindow().getAttributes();
                wlp.alpha = 1 - interpolatedTime;
                getWindow().setAttributes(wlp);
            }
        };
        mOverlayOutAnim.setDuration(120);

    }

    @Override
    public void onBackPressed() {
        return;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog__sweet);

        mRootView = (LinearLayout) findViewById(R.id.sweetDialogRootView);
        mLayoutTitleVer = (RelativeLayout) findViewById(R.id.layout_title_ver);
        mLayoutButton = (RelativeLayout) findViewById(R.id.layout_dialog_button);
        mLayoutCustomButton = (LinearLayout) findViewById(R.id.layout_custom_button);
        mDialogView = getWindow().getDecorView().findViewById(android.R.id.content);
        mContentTextView = (TextView) findViewById(R.id.content_text);
        mTitleTextView = (TextView) findViewById(R.id.title_text);
        mErrorFrame = (FrameLayout) findViewById(R.id.error_frame);
        mInfoFrame = (FrameLayout) findViewById(R.id.info_frame);
        mCustomFrame = (FrameLayout) findViewById(R.id.custom_rootview);
        mInternetFrame = (FrameLayout) findViewById(R.id.internet_frame);
        mCustomContentFrame = (FrameLayout) findViewById(R.id.custom_content);
        mErrorX = (ImageView) mErrorFrame.findViewById(R.id.error_x);
        mVersionTextView = (TextView) findViewById(R.id.version);
        mSuccessFrame = (FrameLayout) findViewById(R.id.success_frame);
        mNormalFrame = (FrameLayout) findViewById(R.id.normal_frame);
        mUpdateFrame = (FrameLayout) findViewById(R.id.update_frame);
        mNormal_ic = (ImageView) mNormalFrame.findViewById(R.id.normal_im);
        mProgressFrame = (FrameLayout) findViewById(R.id.progress_dialog);
        mLine = (View) findViewById(R.id.view_line);
        mCustomImage = (ImageView) findViewById(R.id.custom_image);
        mWarningFrame = (FrameLayout) findViewById(R.id.warning_frame);
        mConfirmButton = (TextView) findViewById(R.id.confirm_button);
        mCancelButton = (TextView) findViewById(R.id.cancel_button);
        mUpdateButton = (TextView) findViewById(R.id.update_button);
        mCheckBox = (CheckBox) findViewById(R.id.id_check_box);
        mUpdateButton.setOnClickListener(this);
        mConfirmButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mConfirmButton.setBackgroundResource(backgroundResource);
        mCancelButton.setBackgroundResource(backgroundResource);
        setTitleText(mTitleText);
        setCustomViewContent(mContentTextNointernet);
        setContentText(mContentText);
        setCancelText(mCancelText);
        setConfirmText(mConfirmText);
        setUpdatetext(mUpdateText);
        setVersionText(mVersionText);
        setCheckBoxTitle(mCheckBoxTitle);
        setArrButton(mArrButton);
        changeAlertType(mAlertType, true);
        setContentHtmlText(mContentText);
        setWidthDialog();

    }

    /**
     * Set width dialog
     */
    public void setWidthDialog()

    {
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        int densityDpi = display.getWidth();
        ViewGroup.LayoutParams params = mRootView.getLayoutParams();

        if (!ZPWUtils.isTablet(getContext())) {
            params.width = (int) (densityDpi * 0.85);
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            mRootView.setLayoutParams(params);
        } else {
            params.width = (int) (densityDpi * 0.5);
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            mRootView.setLayoutParams(params);
        }
    }

    public void setBackground(int color) {
        if (mRootView != null) {
            mRootView.setBackgroundColor(color);
        }
    }

    private void restore() {
        mCustomImage.setVisibility(View.GONE);
        mErrorFrame.setVisibility(View.GONE);
        mInfoFrame.setVisibility(View.GONE);
        mCustomFrame.setVisibility(View.GONE);
        mSuccessFrame.setVisibility(View.GONE);
        mWarningFrame.setVisibility(View.GONE);
        mUpdateFrame.setVisibility(View.GONE);
        mProgressFrame.setVisibility(View.GONE);
        mConfirmButton.setVisibility(View.VISIBLE);
        mUpdateButton.setVisibility(View.GONE);
        mErrorFrame.clearAnimation();

    }

    private void playAnimation() {
        if (mAlertType == ERROR_TYPE) {

        }

    }

    private void changeAlertType(int alertType, boolean fromCreate) {
        mAlertType = alertType;
        // call after created views
        if (mDialogView != null) {
            if (!fromCreate) {
                // restore all of views state before switching alert type
                restore();
            }
            switch (mAlertType) {
                case NORMAL_TYPE:
                    mNormalFrame.setVisibility(View.VISIBLE);
                    break;
                case ERROR_TYPE:
                    mErrorFrame.setVisibility(View.VISIBLE);
                    break;
                case UPDATE_TYPE:
                    mLine.setVisibility(View.GONE);
                    mVersionTextView.setVisibility(View.VISIBLE);
                    mUpdateFrame.setVisibility(View.VISIBLE);
                    mConfirmButton.setVisibility(View.GONE);
                    mUpdateButton.setVisibility(View.VISIBLE);
                    break;
                case UPDATE:
                    mLine.setVisibility(View.VISIBLE);
                    mVersionTextView.setVisibility(View.VISIBLE);
                    mUpdateFrame.setVisibility(View.VISIBLE);
                    // mUpdateButton.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS_TYPE:
                    mSuccessFrame.setVisibility(View.VISIBLE);
                    // initial rotate layout of success mask
                    break;
                case WARNING_TYPE:
                    mWarningFrame.setVisibility(View.VISIBLE);
                    break;
                case INFO_NO_ICON:
                    setCustomImage(mCustomImgDrawable);
                    break;
                case INFO_TYPE:
                    mInfoFrame.setVisibility(View.VISIBLE);
                    break;
                case PROGRESS_TYPE:
                    mProgressFrame.setVisibility(View.VISIBLE);
                    mConfirmButton.setVisibility(View.GONE);
                    mLine.setVisibility(View.GONE);
                    break;
                case CUSTOM_CONTENT_VIEW:
                    mCustomFrame.setVisibility(View.GONE);
                    mNormalFrame.setVisibility(View.GONE);
                    break;
                case NO_INTERNET:
                    mInternetFrame.setVisibility(View.VISIBLE);
                    break;

            }

        }
    }

    public String getTitleText() {
        return mTitleText;
    }

    public SweetAlertDialog setTitleText(String text) {
        mTitleText = text;
        if (mTitleTextView != null && !TextUtils.isEmpty(mTitleText) && mTitleText != null) {
            mTitleTextView.setText(Html.fromHtml(mTitleText));
            mTitleTextView.setVisibility(View.VISIBLE);
            mLayoutTitleVer.setVisibility(View.VISIBLE);
        } else if (mLayoutTitleVer != null) {

            mLayoutTitleVer.setVisibility(View.GONE);
        }
        return this;
    }

    public SweetAlertDialog setCustomViewContent(String pTextView) {
        mContentTextNointernet = pTextView;
        if (mCustomFrame != null && pTextView != null) {
            TextView textViewContentNointernet = (TextView) mCustomFrame.findViewById(R.id.text_content_nointernet);
            if (textViewContentNointernet != null) {
                textViewContentNointernet.setText(Html.fromHtml(pTextView));
            }
            mCustomFrame.setVisibility(View.VISIBLE);
        }
        return this;
    }

    public SweetAlertDialog setVersionText(String text) {
        mVersionText = text;
        if (mVersionText != null && mVersionTextView != null) {
            mVersionTextView.setText(Html.fromHtml(mVersionText));
            mVersionTextView.setVisibility(View.VISIBLE);
        }
        return this;
    }

    public SweetAlertDialog setCustomImage(Drawable drawable) {
        mCustomImgDrawable = drawable;
        if (mCustomImage != null && mCustomImgDrawable != null) {
            mNormalFrame.setVisibility(View.GONE);
            mCustomImage.setVisibility(View.VISIBLE);
            mCustomImage.setImageDrawable(mCustomImgDrawable);
        }
        return this;
    }

    public SweetAlertDialog setCheckBoxTitle(String pText) {
        mCheckBoxTitle = pText;
        if (mCheckBox != null && mCheckBoxTitle != null) {
            mCheckBox.setVisibility(View.VISIBLE);
            mCheckBox.setText(mCheckBoxTitle);
        }
        return this;
    }

    public SweetAlertDialog setCustomImage(int resourceId) {
        return setCustomImage(getContext().getResources().getDrawable(resourceId));
    }

    public String getContentText() {
        return mContentText;
    }

    public SweetAlertDialog setContentText(String text) {
        mContentText = text;
        if (mContentTextView != null && mContentText != null) {
            showContentText(true);
            mContentTextView.setText(mContentText);
        }
        return this;
    }

    public SweetAlertDialog setContentHtmlText(String text) {
        mContentText = text;
        if (mContentTextView != null && mContentText != null) {
            showContentText(true);
            mContentTextView.setText(Html.fromHtml(mContentText));
        }
        return this;
    }

    public boolean isShowCancelButton() {
        return mShowCancel;
    }

    public boolean isShowConfirmButton() {
        return mShowConfirm;
    }

    public SweetAlertDialog showCancelButton(boolean isShow) {
        mShowCancel = isShow;
        if (mCancelButton != null) {
            mCancelButton.setVisibility(mShowCancel ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    public SweetAlertDialog showConfirmButton(boolean isShow) {
        mShowConfirm = isShow;
        if (mConfirmButton != null) {
            mConfirmButton.setVisibility(mShowConfirm ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    public boolean isShowContentText() {
        return mShowContent;
    }

    public SweetAlertDialog showContentText(boolean isShow) {
        mShowContent = isShow;
        if (mContentTextView != null) {
            mContentTextView.setVisibility(mShowContent ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    public String getCancelText() {
        return mCancelText;
    }

    public SweetAlertDialog setCancelText(String text) {
        mCancelText = text;
        if (mCancelButton != null && mCancelText != null) {
            // if cancel button show , Confirm button set blod
            mConfirmButton.setTypeface(null, Typeface.BOLD);
            showCancelButton(true);
            mCancelButton.setText(Html.fromHtml(mCancelText));
        }
        return this;
    }

    public boolean CheckBoxIsChecked(View v) {
        //code to check if this checkbox is checked!
        if (v != null && v instanceof CheckBox) {
            CheckBox checkBox = (CheckBox) v;

            return checkBox.isChecked();
        }

        return false;
    }

    public boolean getCheckBox() {
        return CheckBoxIsChecked(mCheckBox);
    }

    public String getConfirmText() {
        return mConfirmText;
    }

    public SweetAlertDialog setConfirmText(String text) {
        mConfirmText = text;
        if (mConfirmButton != null && mConfirmText != null) {
            mConfirmButton.setText(Html.fromHtml(mConfirmText));
        } else if (mConfirmButton != null && TextUtils.isEmpty(mConfirmText)) {
            mConfirmButton.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mCancelButton.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            mCancelButton.setLayoutParams(params);
        }
        return this;
    }

    public SweetAlertDialog setUpdatetext(String text) {
        mUpdateText = text;
        if (mUpdateButton != null && mUpdateText != null) {
            mUpdateButton.setText(Html.fromHtml(mUpdateText));
        }
        return this;
    }

    public SweetAlertDialog setCancelClickListener(OnSweetClickListener listener) {
        mCancelClickListener = listener;
        return this;
    }

    public SweetAlertDialog setConfirmClickListener(OnSweetClickListener listener) {
        mConfirmClickListener = listener;
        return this;
    }

    public SweetAlertDialog setCustomClickListener(ZPWOnDialogListener listener) {
        mClickButtonArrListener = listener;
        return this;
    }

    public SweetAlertDialog setUpdateClickListener(OnSweetClickListener listener) {
        mUpdateClickListener = listener;
        return this;
    }

    protected void onStart() {
        if (mAlertType == SweetAlertDialog.PROGRESS_TYPE)
            return;

        mDialogView.startAnimation(mModalInAnim);
        playAnimation();
    }

    /**
     * The real Dialog.cancel() will be invoked async-ly after the animation finishes.
     */
    @Override
    public void cancel() {
        dismissWithAnimation(true);
    }

    /**
     * The real Dialog.dismiss() will be invoked async-ly after the animation finishes.
     */
    public void dismissWithAnimation() {
        dismissWithAnimation(false);
    }

    private void dismissWithAnimation(boolean fromCancel) {
        mCloseFromCancel = fromCancel;
        mConfirmButton.startAnimation(mOverlayOutAnim);
        mDialogView.startAnimation(mModalOutAnim);
    }

    //set arr Button
    public SweetAlertDialog setArrButton(String... pArrButton) {
        mArrButton = pArrButton;

        if (mLayoutCustomButton != null && mArrButton != null) {
            try {
                mLayoutCustomButton.setVisibility(View.VISIBLE);
                int paddingTop = (int) getContext().getResources().getDimension(R.dimen.zpw_padding_6);
                int paddingRight = (int) getContext().getResources().getDimension(R.dimen.zpw_padding_12);
                int paddingLeft = (int) getContext().getResources().getDimension(R.dimen.zpw_padding_12);
                int paddingBottom = (int) getContext().getResources().getDimension(R.dimen.zpw_padding_6);
                for (int i = 0; i < mArrButton.length; i++) {
                    TextView tv = new TextView(getContext());
                    tv.setId(i);
                    tv.setText(mArrButton[i]);
                    tv.setBackgroundResource(backgroundResource);
                    tv.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
                    tv.setTextColor(getContext().getResources().getColor(R.color.text_color_bold));
                    tv.setTextAppearance(getContext(), R.style.dialog_blue_button);
                    if (i == mArrButton.length - 1) {
                        tv.setTypeface(null, Typeface.BOLD);
                    } else {
                        tv.setTypeface(null, Typeface.NORMAL);

                    }

                    tv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mClickButtonArrListener.onCloseDialog(SweetAlertDialog.this, view.getId());
                        }
                    });
                    mLayoutCustomButton.addView(tv);
                }
            } catch (Exception e) {
                Log.e(this, e);
            }
        }
        if (mLayoutButton != null && mArrButton != null) {
            mLayoutButton.setVisibility(View.GONE);
        }

        return this;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cancel_button) {
            if (mCancelClickListener != null) {
                mCancelClickListener.onClick(SweetAlertDialog.this);
            } else {
                dismissWithAnimation();
            }
        } else if (v.getId() == R.id.confirm_button) {
            if (mConfirmClickListener != null && mAlertType != UPDATE) {
                mConfirmClickListener.onClick(SweetAlertDialog.this);
            } else if (mAlertType == UPDATE && mUpdateClickListener != null) {
                mUpdateClickListener.onClick(SweetAlertDialog.this);
            } else {
                dismissWithAnimation();
            }
        } else if (v.getId() == R.id.update_button) {
            if (mUpdateClickListener != null)
                mUpdateClickListener.onClick(SweetAlertDialog.this);
            else {
                dismissWithAnimation();
            }
        }
    }

    public static interface OnSweetClickListener {
        public void onClick(SweetAlertDialog sweetAlertDialog);
    }
}