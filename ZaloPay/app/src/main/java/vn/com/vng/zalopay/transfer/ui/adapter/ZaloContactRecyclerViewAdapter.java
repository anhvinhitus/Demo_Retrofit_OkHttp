package vn.com.vng.zalopay.transfer.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import java.util.List;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.transfer.models.ZaloFriend;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ZaloFriend} and makes a call to the
 * specified {}.
 */
public class ZaloContactRecyclerViewAdapter extends UltimateViewAdapter<ZaloContactRecyclerViewAdapter.ViewHolder> {
    private final Context mContext;
    private final List<ZaloFriend> mValues;
    private final OnItemInteractionListener mListener;

    public interface OnItemInteractionListener {
        void onItemClick(ZaloFriend item);
    }

    public ZaloContactRecyclerViewAdapter(Context context, List<ZaloFriend> items, OnItemInteractionListener listener) {
        mContext = context;
        mValues = items;
        mListener = listener;
    }

    public void setData(List<ZaloFriend> zaloFriends) {
        mValues.clear();
        if (zaloFriends == null || zaloFriends.size() <= 0) {
            return;
        } else {
            mValues.addAll(zaloFriends);
        }
        notifyDataSetChanged();
    }

    public void addItems(List<ZaloFriend> zaloFriends) {
        if (zaloFriends == null || zaloFriends.size() <= 0) {
            return;
        }
        int currentItemIndex = mValues.size() - 1;
        mValues.addAll(zaloFriends);
        notifyItemRangeChanged(currentItemIndex, zaloFriends.size());
    }

    public void setItems(List<ZaloFriend> zaloFriends) {
        mValues.clear();
        mValues.addAll(zaloFriends);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder newFooterHolder(View view) {
        return null;
    }

    @Override
    public ViewHolder newHeaderHolder(View view) {
        return null;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_zalo_contact_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        return null;
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        viewHolder.mItem = mValues.get(position);
        viewHolder.mTvDisplayName.setText(viewHolder.mItem.getDisplayName());
        loadImage(viewHolder.mImgAvatar, viewHolder.mItem.getAvatar());
        if (position < getItemCount() - 1) {
            viewHolder.mViewSeparate.setVisibility(View.VISIBLE);
        } else {
            viewHolder.mViewSeparate.setVisibility(View.GONE);
        }
        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onItemClick(viewHolder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public int getAdapterItemCount() {
        return mValues == null ? 0 : mValues.size();
    }

    @Override
    public long generateHeaderId(int position) {
        return 0;
    }


    private void loadImage(ImageView image, String url) {
        if (mContext == null) {
            return;
        }
        Glide.with(mContext).load(url).centerCrop().placeholder(R.color.silver).into(image);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTvDisplayName;
        public final ImageView mImgAvatar;
        public final View mViewSeparate;
        public ZaloFriend mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImgAvatar = (ImageView) view.findViewById(R.id.imgAvatar);
            mTvDisplayName = (TextView) view.findViewById(R.id.tvDisplayName);
            mViewSeparate = view.findViewById(R.id.viewSeparate);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTvDisplayName.getText() + "'";
        }
    }
}
