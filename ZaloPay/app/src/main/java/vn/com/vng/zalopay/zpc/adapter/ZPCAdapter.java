package vn.com.vng.zalopay.zpc.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.util.Strings;
import vn.com.vng.zalopay.data.zpc.ZPCAlias.ColumnAlias;

/**
 * Created by AnhHieu on 10/7/16.
 * *
 */

abstract class ZPCAdapter<T extends ViewHolder> extends CursorSectionAdapter {
    private LayoutInflater mInflater;
    private final TextAppearanceSpan mHighlightTextSpan;
    private String mKeySearch;
    private final int mResLayout;
    private final float mBorderAvatar;
    private final int mPrimaryColor;
    private static final int INDEX_PHONE_FORMAT = 7;

    ZPCAdapter(Context context, @LayoutRes int resLayout) {
        super(context);
        mInflater = LayoutInflater.from(context);
        mHighlightTextSpan = new TextAppearanceSpan(context, R.style.searchTextHiglight);
        mResLayout = resLayout;
        mBorderAvatar = context.getResources().getDimension(R.dimen.border_avatar_contact);
        mPrimaryColor = ContextCompat.getColor(context, R.color.colorPrimary);
    }

    public void setKeySearch(String keySearch) {
        mKeySearch = keySearch == null ? "" : Strings.stripAccents(keySearch.toLowerCase());
    }

    boolean isSearching() {
        return !TextUtils.isEmpty(mKeySearch);
    }

    private int indexOfSearchQuery(String displayName) {
        if (!TextUtils.isEmpty(mKeySearch)) {
            return displayName.toLowerCase(Locale.getDefault()).indexOf(mKeySearch);
        }
        return -1;
    }

    @Override
    protected void bindSeparatorView(View v, Context context, Object item) {
        String sectionObject = (String) item;
        Object object = v.getTag();

        if (object instanceof SectionHolder) {
            ((SectionHolder) object).bindView(sectionObject);
        }
    }

    @Override
    protected View newSeparatorView(Context context2, Object item, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.row_section_layout, parent, false);
        SectionHolder holder = new SectionHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    protected int getRealItemPosition(int position) {
        return super.getRealItemPosition(position);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    protected SortedMap<Integer, Object> initializeSections(Cursor c) {
        TreeMap<Integer, Object> sections = new TreeMap<>();
        int offset = 0, i = 0;
        while (c.moveToNext()) {
            String fullTextSearch = c.getString(c.getColumnIndex(ColumnAlias.NORMALIZE_DISPLAY_NAME));
            if (TextUtils.isEmpty(fullTextSearch)) {
                fullTextSearch = "#";
            }

            String firstLetter = fullTextSearch.substring(0, 1);
            if (!sections.containsValue(firstLetter)) {
                sections.put(offset + i, firstLetter);
                offset++;
            }

            i++;
        }

        return sections;
    }


    abstract T onCreateViewHolder(View v);

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(mResLayout, parent, false);
        T holder = onCreateViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Object tag = view.getTag();

        if (!(tag instanceof ViewHolder)) {
            Timber.d("tag is not instance of ZPCAdapter.ViewHolder");
            return;
        }

        ViewHolder holder = (ViewHolder) tag;

        long zaloId = cursor.getLong(cursor.getColumnIndex(ColumnAlias.ZALO_ID));
        String displayName = cursor.getString(cursor.getColumnIndex(ColumnAlias.DISPLAY_NAME));
        String aliasDisplayName = cursor.getString(cursor.getColumnIndex(ColumnAlias.NORMALIZE_DISPLAY_NAME));
        String avatar = cursor.getString(cursor.getColumnIndex(ColumnAlias.AVATAR));
        String phone = cursor.getString(cursor.getColumnIndex(ColumnAlias.PHONE_NUMBER));
        long zaloPayId = cursor.getLong(cursor.getColumnIndex(ColumnAlias.ZALOPAY_ID));
        int status = cursor.getInt(cursor.getColumnIndex(ColumnAlias.STATUS));
        String firstName = cursor.getString(cursor.getColumnIndex(ColumnAlias.FIRST_NAME));
        String lastName = cursor.getString(cursor.getColumnIndex(ColumnAlias.LAST_NAME));

        holder.bindView(zaloId, phone, displayName, aliasDisplayName, avatar, status);

        if (TextUtils.isEmpty(avatar)) {
            holder.mPlaceHolder.setVisibility(View.VISIBLE);
            holder.mPlaceHolder.setText(aliasDisplayName.substring(0, 1).toUpperCase());
        } else {
            holder.mPlaceHolder.setVisibility(View.INVISIBLE);
            holder.mImgAvatar.setImageURI(avatar);
        }

        if (zaloPayId == 0) {
            holder.mLogo.setVisibility(View.INVISIBLE);
            setShowBorder(holder.mImgAvatar, Color.TRANSPARENT, 0);
        } else {
            holder.mLogo.setVisibility(View.VISIBLE);
            setShowBorder(holder.mImgAvatar, mPrimaryColor, mBorderAvatar);
        }

        setDisplayName(holder.mTvDisplayName, displayName, aliasDisplayName);
        setPhone(holder.mPhoneView, phone);
    }

    private void setDisplayName(TextView mTvDisplayName, String displayName, String aliasDisPlayName) {
        int startIndex = indexOfSearchQuery(aliasDisPlayName);
        if (startIndex == -1) {
            startIndex = indexOfSearchQuery(displayName);
        }

        if (startIndex == -1) {
            mTvDisplayName.setText(displayName);
        } else {
            int length = mKeySearch == null ? 0 : mKeySearch.length();
            final SpannableString highlightedName = new SpannableString(displayName);
            highlightedName.setSpan(mHighlightTextSpan, startIndex,
                    startIndex + length, 0);
            mTvDisplayName.setText(highlightedName);
        }

    }

    private void setPhone(TextView mPhoneView, String phone) {
        if (TextUtils.isEmpty(phone)) {
            mPhoneView.setVisibility(View.GONE);
            return;
        }

        int lengthPhone = phone.length();
        String formattedNumber;
        if (lengthPhone > INDEX_PHONE_FORMAT) {
            int endIndex = lengthPhone - INDEX_PHONE_FORMAT;
            formattedNumber = phone.substring(0, endIndex) + " " + phone.substring(endIndex, lengthPhone);
        } else {
            formattedNumber = phone;
        }

        int startIndex = indexOfSearchQuery(formattedNumber);

        if (startIndex == -1) {
            mPhoneView.setText(formattedNumber);
        } else {
            int length = mKeySearch == null ? 0 : mKeySearch.length();
            final SpannableString highlightedName = new SpannableString(formattedNumber);
            highlightedName.setSpan(mHighlightTextSpan, startIndex,
                    startIndex + length, 0);
            mPhoneView.setText(highlightedName);

        }
        mPhoneView.setVisibility(View.VISIBLE);
    }


    private void setShowBorder(@NonNull SimpleDraweeView draweeView, @ColorInt int color, float width) {
        if (!draweeView.hasHierarchy()) {
            return;
        }

        RoundingParams roundingParams = draweeView.getHierarchy().getRoundingParams();
        if (roundingParams == null) {
            return;
        }

        roundingParams.setBorder(color, width);
        draweeView.getHierarchy().setRoundingParams(roundingParams);
    }

    @Override
    protected boolean isSection(int position) {
        return getSections().containsKey(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (getSections().containsKey(position)) {
            return 0;
        } else {
            return 1;
        }
    }

    static class SectionHolder {

        @BindView(R.id.tv_section)
        TextView mSectionView;

        SectionHolder(View view) {
            ButterKnife.bind(this, view);
        }

        void bindView(String section) {
            mSectionView.setText(section);
        }
    }
}
