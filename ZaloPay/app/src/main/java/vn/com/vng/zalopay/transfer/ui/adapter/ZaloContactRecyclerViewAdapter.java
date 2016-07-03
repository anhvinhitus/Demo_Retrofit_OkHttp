package vn.com.vng.zalopay.transfer.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.recyclerview.EndlessListAdapter;
import vn.com.vng.zalopay.transfer.models.ZaloFriend;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ZaloFriend} and makes a call to the
 * specified {}.
 */
public class ZaloContactRecyclerViewAdapter extends EndlessListAdapter<ZaloFriend> {
    private Context mContext;

    private OnItemInteractionListener mListener;

    public interface OnItemInteractionListener {
        void onItemClick(ZaloFriend item);
    }

    public ZaloContactRecyclerViewAdapter(Context context, OnItemInteractionListener listener, OnLoadMoreListener onLoadMoreListener) {
        super(onLoadMoreListener);
        this.mContext = context;
        this.mListener = listener;
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ZaloContactViewHolder) {
            final ZaloContactViewHolder viewHolder = (ZaloContactViewHolder) holder;
            viewHolder.mItem = getItemList().get(position);
            viewHolder.mTvDisplayName.setText(viewHolder.mItem.getDisplayName());
            loadImage(viewHolder.mImgAvatar, viewHolder.mItem.getAvatar());
            if (position < getItemCount() - 1) {
                viewHolder.mViewSeparate.setVisibility(View.VISIBLE);
            } else {
                viewHolder.mViewSeparate.setVisibility(View.GONE);
            }
            if (viewHolder.mItem.isUsingApp()) {
                viewHolder.mImgZaloPay.setVisibility(View.VISIBLE);
            } else {
                viewHolder.mImgZaloPay.setVisibility(View.GONE);
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
    }

    @Override
    public RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        return new ZaloContactViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_zalo_contact_item, parent, false));
    }

    private void loadImage(ImageView image, String url) {
        if (mContext == null) {
            return;
        }
        Glide.with(mContext).load(url).centerCrop().placeholder(R.color.silver).into(image);
    }

    public class ZaloContactViewHolder extends UltimateRecyclerviewViewHolder {
        public View mView;
        public TextView mTvDisplayName;
        public ImageView mImgAvatar;
        public ImageView mImgZaloPay;
        public View mViewSeparate;
        public ZaloFriend mItem;

        public ZaloContactViewHolder(View view) {
            super(view);
            mView = view;
            mImgAvatar = (ImageView) view.findViewById(R.id.imgAvatar);
            mImgZaloPay = (ImageView) view.findViewById(R.id.imgZaloPay);
            mTvDisplayName = (TextView) view.findViewById(R.id.tvDisplayName);
            mViewSeparate = view.findViewById(R.id.viewSeparate);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTvDisplayName.getText() + "'";
        }
    }
}
