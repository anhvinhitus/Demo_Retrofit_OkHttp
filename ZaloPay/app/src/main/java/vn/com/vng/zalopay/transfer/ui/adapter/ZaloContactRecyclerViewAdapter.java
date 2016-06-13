package vn.com.vng.zalopay.transfer.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.transfer.models.ZaloFriend;
import vn.com.vng.zalopay.transfer.ui.fragment.ZaloContactFragment.OnListFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ZaloFriend} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class ZaloContactRecyclerViewAdapter extends RecyclerView.Adapter<ZaloContactRecyclerViewAdapter.ViewHolder> {
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

    public void addItems(List<ZaloFriend> zaloFriends) {
        if (zaloFriends == null || zaloFriends.size() <=0) {
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_zalo_contact_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mTvDisplayName.setText(holder.mItem.getDisplayName());
        loadImage(holder.mImgAvatar, holder.mItem.getAvatar());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onItemClick(holder.mItem);
                }
            }
        });
    }

    private final void loadImage(ImageView image, String url) {
        if (mContext == null) {
            return;
        }
        Glide.with(mContext).load(url).centerCrop().placeholder(R.color.silver).into(image);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTvDisplayName;
        public final ImageView mImgAvatar;
        public ZaloFriend mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImgAvatar = (ImageView) view.findViewById(R.id.imgAvatar);
            mTvDisplayName = (TextView) view.findViewById(R.id.tvDisplayName);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTvDisplayName.getText() + "'";
        }
    }
}
