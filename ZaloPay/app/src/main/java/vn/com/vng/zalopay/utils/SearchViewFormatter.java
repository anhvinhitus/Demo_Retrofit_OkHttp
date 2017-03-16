package vn.com.vng.zalopay.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.ActionBarOverlayLayout;
import android.support.v7.widget.SearchView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zalopay.ui.widget.IconFont;
import com.zalopay.ui.widget.IconFontDrawable;


/**
 * Created by khattn on 3/13/17.
 * Class helps format style search view
 */

public class SearchViewFormatter {
    private int mBackgroundResource = 0;
    private int mIconResource = 0;
    private IconFontDrawable mIconFont = null;
    private boolean mIconInside = false;
    private boolean mIconOutside = false;
    private int mVoiceIconResource = 0;
    private int mTextSize = 0;
    private int mTextColorResource = 0;
    private int mHintColorResource = 0;
    private String mHintText = "";
    private int mHintTextResource = 0;
    private int mInputType = Integer.MIN_VALUE;
    private IconFontDrawable mCloseIconFont = null;
    private int mCloseIconResource = 0;
    private TextView.OnEditorActionListener mEditorActionListener;

    public SearchViewFormatter setBackgroundResource(int backGroundRes) {
        mBackgroundResource = backGroundRes;
        return this;
    }

    public SearchViewFormatter setIconResource(int iconRes,
                                               boolean inside, boolean outside) {
        mIconResource = iconRes;
        mIconInside = inside;
        mIconOutside = outside;
        return this;
    }

    public SearchViewFormatter setIconFont(Context context, int iconName,
                                           boolean inside, boolean outside) {
        mIconFont = new IconFontDrawable(context)
                .setIcon(iconName);
        mIconInside = inside;
        mIconOutside = outside;
        return this;
    }

    public SearchViewFormatter setIconFont(Context context, int iconName, int iconResColor,
                                           boolean inside, boolean outside) {
        mIconFont = new IconFontDrawable(context)
                .setIcon(iconName)
                .setResourcesColor(iconResColor);
        mIconInside = inside;
        mIconOutside = outside;
        return this;
    }

    public SearchViewFormatter setIconFont(Context context, int iconName, int iconResColor, int iconResSize,
                                           boolean inside, boolean outside) {
        mIconFont = new IconFontDrawable(context)
                .setIcon(iconName)
                .setResourcesColor(iconResColor)
                .setResourcesSize(iconResSize);
        mIconInside = inside;
        mIconOutside = outside;
        return this;
    }

    public SearchViewFormatter setVoiceIconResource(int voiceIconRes) {
        mVoiceIconResource = voiceIconRes;
        return this;
    }

    public SearchViewFormatter setTextSize(int textSize) {
        mTextSize = textSize;
        return this;
    }

    public SearchViewFormatter setTextColorResource(int textColorRes) {
        mTextColorResource = textColorRes;
        return this;
    }

    public SearchViewFormatter setHintColorResource(int hintColorRes) {
        mHintColorResource = hintColorRes;
        return this;
    }

    public SearchViewFormatter setHintText(String hint) {
        mHintText = hint;
        return this;
    }

    public SearchViewFormatter setHintTextResource(int hintRes) {
        mHintTextResource = hintRes;
        return this;
    }

    public SearchViewFormatter setInputType(int inputType) {
        mInputType = inputType;
        return this;
    }

    public SearchViewFormatter setCloseIconFont(Context context, int iconName) {
        mCloseIconFont = new IconFontDrawable(context)
                .setIcon(iconName);
        return this;
    }

    public SearchViewFormatter setCloseIconFont(Context context, int iconName, int iconResColor) {
        mCloseIconFont = new IconFontDrawable(context)
                .setIcon(iconName)
                .setResourcesColor(iconResColor);
        return this;
    }

    public SearchViewFormatter setCloseIconFont(Context context, int iconName, int iconResColor, int iconResSize) {
        mCloseIconFont = new IconFontDrawable(context)
                .setIcon(iconName)
                .setResourcesColor(iconResColor)
                .setResourcesSize(iconResSize);
        return this;
    }

    public SearchViewFormatter setCloseIconResource(int searchCloseIconResource) {
        mCloseIconResource = searchCloseIconResource;
        return this;
    }

    public SearchViewFormatter setEditorActionListener(TextView.OnEditorActionListener editorActionListener) {
        mEditorActionListener = editorActionListener;
        return this;
    }

    public void format(SearchView searchView) {
        if (searchView == null) {
            return;
        }

        Context mContext = searchView.getContext();

        if (mBackgroundResource != 0) {
            View view = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
            view.setBackgroundResource(mBackgroundResource);

            view = searchView.findViewById(android.support.v7.appcompat.R.id.submit_area);
            view.setBackgroundResource(mBackgroundResource);
        }

        if (mVoiceIconResource != 0) {
            ImageView view = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_voice_btn);
            view.setImageResource(mVoiceIconResource);
        }

        if (mCloseIconResource != 0) {
            ImageView view = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
            view.setImageResource(mCloseIconResource);
        }

        if (mCloseIconFont != null) {
            ImageView view = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
            view.setImageDrawable(mCloseIconFont);
        }

        TextView view = (TextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        if (mTextColorResource != 0) {
            view.setTextColor(ContextCompat.getColor(mContext, mTextColorResource));
        }
        if (mTextSize != 0) {
            view.setTextSize(mTextSize);
        }
        if (mHintTextResource != 0) {
            mHintText = mContext.getString(mHintTextResource);
            view.setHint(mHintText);
        }
        if (!TextUtils.isEmpty(mHintText)) {
            view.setHint(mHintText);
        }
        if (mHintColorResource != 0) {
            view.setHintTextColor(ContextCompat.getColor(mContext, mHintColorResource));
        }
        if (mInputType > Integer.MIN_VALUE) {
            view.setInputType(mInputType);
        }

        if (mEditorActionListener != null) {
            view.setOnEditorActionListener(mEditorActionListener);
        }

        if (mIconResource != 0) {
            ImageView imageView;
            if (mIconInside) {
                imageView = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
                imageView.setImageResource(mIconResource);

//                Drawable iconDrawable = ContextCompat.getDrawable(mContext, mIconResource);
//                int size = (int) (view.getTextSize() * 1.25f);
//                iconDrawable.setBounds(size, size, 0, 0);
//
//                SpannableStringBuilder hintBuilder = new SpannableStringBuilder(mHintText);
//                hintBuilder.append("   ");
//                hintBuilder.setSpan(
//                        new ImageSpan(iconDrawable),
//                        hintBuilder.length() - 1,
//                        hintBuilder.length(),
//                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                );
//
//                view.setHint(hintBuilder);
//                imageView.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
            }
            if (mIconOutside) {
                imageView = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_button);
                imageView.setImageResource(mIconResource);
            }
        }

        if (mIconFont != null) {
            ImageView imageView;
            if (mIconInside) {
                imageView = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
                imageView.setImageDrawable(mIconFont);
//                view.setCompoundDrawables(null, null, mIconFontDrawable, null);
            }
            if (mIconOutside) {
                imageView = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_button);
                imageView.setImageDrawable(mIconFont);
            }
        }
    }
}
