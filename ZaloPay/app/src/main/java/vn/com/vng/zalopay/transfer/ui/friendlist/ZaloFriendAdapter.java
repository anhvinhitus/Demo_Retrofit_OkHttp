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

import timber.log.Timber;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.utils.ImageLoader;

/**
 * Created by AnhHieu on 10/7/16.
 * *
 */

public class ZaloFriendAdapter extends CursorSectionAdapter {
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;

    public ZaloFriendAdapter(Context context) {
        super(context);
        mInflater = LayoutInflater.from(context);
        mImageLoader = AndroidApplication.instance().getAppComponent().imageLoader();
    }

    @Override
    protected void bindSeparatorView(View v, Context context2, Object item) {
        SectionHolder holder = (SectionHolder) v.getTag();
        holder.mSectionView.setText(String.valueOf(item));
    }

    @Override
    protected View newSeparatorView(Context context2, Object item, ViewGroup parent) {
        SectionHolder holder = new SectionHolder();
        View view = mInflater.inflate(R.layout.row_section_layout, parent, false);
        holder.mSectionView = (TextView) view.findViewById(R.id.tv_section);
        view.setTag(holder);
        return view;
    }

    @Override
    protected SortedMap<Integer, Object> initializeSections(Cursor c) {
        TreeMap<Integer, Object> sections = new TreeMap<Integer, Object>();
        int offset = 0, i = 0;
        while (c.moveToNext()) {

            boolean isUseApp = c.getInt(6) == 1;
            Timber.d("initializeSections: isUseApp %s", isUseApp);
            if (isUseApp) {
                i++;
                continue;
            }

            String firstLetter = c.getString(7).substring(0, 1);
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
        ViewHolder holder = new ViewHolder();
        View view = mInflater.inflate(R.layout.fragment_zalo_contact_item, parent, false);
        holder.mTvDisplayName = (TextView) view.findViewById(R.id.tvDisplayName);
        holder.mImgAvatar = (SimpleDraweeView) view.findViewById(R.id.imgAvatar);
        holder.mImgZaloPay = view.findViewById(R.id.imgZaloPay);
        holder.mViewSeparate = view.findViewById(R.id.viewSeparate);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String displayName = cursor.getString(2);
        String avatar = cursor.getString(3);
        int isUsingApp = cursor.getInt(6);
        boolean isEndRow = false;

        holder.mTvDisplayName.setText(displayName);
        holder.mImgAvatar.setImageURI(avatar);
        holder.mImgZaloPay.setVisibility(isUsingApp == 1 ? View.VISIBLE : View.INVISIBLE);
        holder.mViewSeparate.setVisibility(isEndRow ? View.VISIBLE : View.INVISIBLE);
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

    private static class ViewHolder {

        TextView mTvDisplayName;

        SimpleDraweeView mImgAvatar;

        View mImgZaloPay;

        View mViewSeparate;
    }

    private static class SectionHolder {
        TextView mSectionView;
    }
}
