package vn.com.vng.zalopay.transfer.ui.friendlist;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.zfriend.ColumnIndex;

/**
 * Created by AnhHieu on 10/7/16.
 * *
 */

final class ZaloFriendAdapter extends CursorSectionAdapter {
    private LayoutInflater mInflater;

    ZaloFriendAdapter(Context context) {
        super(context);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    protected void bindSeparatorView(View v, Context context, Object item) {
        SectionObject sectionObject = (SectionObject) item;
        Object object = v.getTag();

        if (object instanceof SectionHolder) {
            //Timber.d("SectionHolder: %s", sectionObject.firstChar);
            ((SectionHolder) object).bindView(sectionObject);
        } else if (object instanceof TitleHolder) {
            //Timber.d("TitleHolder: %s", sectionObject.firstChar);
            ((TitleHolder) object).bindView(sectionObject);
        }
    }

    @Override
    protected View newSeparatorView(Context context2, Object item, ViewGroup parent) {
        SectionObject sectionObject = (SectionObject) item;
        //Timber.d("newSeparatorView: item %s", ((SectionObject) item).firstChar);
        View view;
        if (!TextUtils.isEmpty(sectionObject.firstChar) && sectionObject.firstChar.length() > 1) {
            view = mInflater.inflate(R.layout.row_section_friend_layout, parent, false);
            //Timber.d("create new title section");
            TitleHolder holder = new TitleHolder(view);
            view.setTag(holder);
        } else {
            //Timber.d("create new section");
            view = mInflater.inflate(R.layout.row_section_layout, parent, false);
            SectionHolder holder = new SectionHolder(view);
            view.setTag(holder);
        }
        return view;
    }

    @Override
    protected int getRealItemPosition(int position) {
        return super.getRealItemPosition(position);
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    protected SortedMap<Integer, Object> initializeSections(Cursor c) {
        TreeMap<Integer, Object> sections = new TreeMap<>();
        int offset = 0, i = 0;
        HashMap<String, Boolean> titleSectionUsingApp = new HashMap<>();
        while (c.moveToNext()) {

            boolean isUseApp = c.getInt(c.getColumnIndex(ColumnIndex.STATUS)) == 1;

            if (isUseApp && !titleSectionUsingApp.containsKey("use")) {
                SectionObject section = new SectionObject(mContext.getString(R.string.friends_use_zalopay), isUseApp);
                if (!sections.containsValue(section)) {
                    sections.put(offset + i, section);
                    offset++;
                }

                titleSectionUsingApp.put("use", true);
            } else if (!isUseApp && !titleSectionUsingApp.containsKey("notuse")) {
                SectionObject section = new SectionObject(mContext.getString(R.string.friends_not_use_zalopay), isUseApp);
                if (!sections.containsValue(section)) {
                    sections.put(offset + i, section);
                    offset++;
                }
                titleSectionUsingApp.put("notuse", true);
            }
            String firstLetter;
            String fullTextSearch = c.getString(c.getColumnIndex(ColumnIndex.ALIAS_FULL_TEXT_SEARCH));
            if (TextUtils.isEmpty(fullTextSearch)) {
                fullTextSearch = "#";
            }

            firstLetter = fullTextSearch.substring(0, 1);

            SectionObject section = new SectionObject(firstLetter, isUseApp);
            if (!sections.containsValue(section)) {
                sections.put(offset + i, section);
                offset++;
            }

            i++;
        }

        return sections;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.row_zalo_friend_list, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor, int position) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.bindView(cursor, !isSection(position + 1));
    }

    @Override
    protected boolean isSection(int position) {
        int viewType = getItemViewType(position);
        return viewType == 0 || viewType == 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (getSections().containsKey(position)) {
            SectionObject sectionObject = (SectionObject) getSections().get(position);
            if (!TextUtils.isEmpty(sectionObject.firstChar) && sectionObject.firstChar.length() > 1) {
                return 2;
            } else {
                return 0;
            }
        } else {
            return 1;
        }
    }

    static class ViewHolder {

        @BindView(R.id.tvDisplayName)
        TextView mTvDisplayName;

        @BindView(R.id.imgAvatar)
        SimpleDraweeView mImgAvatar;

        @BindView(R.id.imgZaloPay)
        View mImgZaloPay;

        @BindView(R.id.viewSeparate)
        View mViewSeparate;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        void bindView(Cursor cursor, boolean isShowSeparate) {

            String displayName = cursor.getString(cursor.getColumnIndex(ColumnIndex.ALIAS_DISPLAY_NAME));
            String avatar = cursor.getString(ColumnIndex.AVATAR);
            int status = cursor.getInt(cursor.getColumnIndex(ColumnIndex.STATUS));

            mTvDisplayName.setText(displayName);
            mImgAvatar.setImageURI(avatar);
            mImgZaloPay.setSelected(status == 1);
            mViewSeparate.setVisibility(isShowSeparate ? View.VISIBLE : View.INVISIBLE);
        }
    }

    static class SectionHolder {

        @BindView(R.id.tv_section)
        TextView mSectionView;

        SectionHolder(View view) {
            ButterKnife.bind(this, view);
        }

        void bindView(SectionObject section) {
            mSectionView.setText(section.firstChar);
        }
    }

    static class TitleHolder {

        @BindView(R.id.tv_section)
        TextView mSectionView;

        TitleHolder(View view) {
            ButterKnife.bind(this, view);
        }

        void bindView(SectionObject section) {
            mSectionView.setText(section.firstChar);
        }
    }
}
