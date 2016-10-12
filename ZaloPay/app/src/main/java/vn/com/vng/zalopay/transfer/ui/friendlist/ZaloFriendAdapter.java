package vn.com.vng.zalopay.transfer.ui.friendlist;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.SortedMap;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.zfriend.ColumnIndex;
import vn.com.vng.zalopay.utils.ImageLoader;

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
    protected void bindSeparatorView(View v, Context context2, Object item) {
        SectionHolder holder = (SectionHolder) v.getTag();
        holder.bindView(String.valueOf(item));
    }

    @Override
    protected View newSeparatorView(Context context2, Object item, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.row_section_layout, parent, false);
        SectionHolder holder = new SectionHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    protected SortedMap<Integer, Object> initializeSections(Cursor c) {
        TreeMap<Integer, Object> sections = new TreeMap<>();
        int offset = 0, i = 0;
        while (c.moveToNext()) {

            boolean isUseApp = c.getInt(ColumnIndex.UsingApp) == 1;
            //   Timber.d("initializeSections: isUseApp %s", isUseApp);
            if (isUseApp) {
                i++;
                continue;
            }

            String firstLetter = c.getString(ColumnIndex.Fulltextsearch).substring(0, 1);
            if (!sections.containsValue(firstLetter)) {
                sections.put(offset + i, firstLetter);
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
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.bindView(cursor);
    }


    @Override
    protected boolean isSection(int position) {
        return getItemViewType(position) == 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (getSections().containsKey(position))
            return 0;
        else
            return 1;
    }

    static class ViewHolder {

        @BindView(R.id.tvDisplayName)
        public TextView mTvDisplayName;

        @BindView(R.id.imgAvatar)
        public SimpleDraweeView mImgAvatar;

        @BindView(R.id.imgZaloPay)
        public View mImgZaloPay;

        @BindView(R.id.viewSeparate)
        public View mViewSeparate;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        void bindView(Cursor cursor) {

            String displayName = cursor.getString(ColumnIndex.DisplayName);
            String avatar = cursor.getString(ColumnIndex.Avatar);
            int isUsingApp = cursor.getInt(ColumnIndex.UsingApp);

            mTvDisplayName.setText(displayName);
            mImgAvatar.setImageURI(avatar);
            mImgZaloPay.setVisibility(isUsingApp == 1 ? View.VISIBLE : View.INVISIBLE);
        }
    }

    static class SectionHolder {

        @BindView(R.id.tv_section)
        public TextView mSectionView;

        public SectionHolder(View view) {
            ButterKnife.bind(this, view);
        }

        void bindView(String section) {
            mSectionView.setText(section);
        }
    }
}
