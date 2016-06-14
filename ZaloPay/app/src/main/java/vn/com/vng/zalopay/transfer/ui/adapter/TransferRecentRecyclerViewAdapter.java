package vn.com.vng.zalopay.transfer.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.transfer.models.TransferRecent;
import vn.com.vng.zalopay.transfer.ui.fragment.TransferHomeFragment.OnListFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link TransferRecent} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class TransferRecentRecyclerViewAdapter extends RecyclerView.Adapter<TransferRecentRecyclerViewAdapter.ViewHolder> {

    private final Context mContext;
    private final List<TransferRecent> mValues;
    private final OnTransferRecentItemListener mListener;

    public interface OnTransferRecentItemListener {
        // TODO: Update argument type and name
        void onItemClick(TransferRecent item);
    }

    public TransferRecentRecyclerViewAdapter(Context context, List<TransferRecent> items, OnTransferRecentItemListener listener) {
        mContext = context;
        mValues = items;
        mListener = listener;
    }

    public void setData(List<TransferRecent> items) {
        mValues.clear();
        if (items == null || items.size() <= 0) {
            return;
        }
        mValues.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_trasfer_recent_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mTvDisplayName.setText(holder.mItem.getDisplayName());
        String phone = "SĐT: ";
        if (!TextUtils.isEmpty(holder.mItem.getPhoneNumber())) {
            phone+= holder.mItem.getPhoneNumber();
            holder.mTvPhone.setText(phone);
            holder.mTvPhone.setVisibility(View.VISIBLE);
        } else {
            holder.mTvPhone.setVisibility(View.GONE);
        }
        loadImage(holder.mImgAvatar, holder.mItem.getAvatar());

        if (position != getItemCount()-1) {
            holder.mViewSeparate.setVisibility(View.VISIBLE);
        } else {
            holder.mViewSeparate.setVisibility(View.GONE);
        }
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
        public final TextView mTvPhone;
        public final ImageView mImgAvatar;
        public final ImageView mImgTransferType;
        public final View mViewSeparate;
        public TransferRecent mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImgAvatar = (ImageView) view.findViewById(R.id.imgAvatar);
            mTvDisplayName = (TextView) view.findViewById(R.id.tvDisplayName);
            mTvPhone = (TextView) view.findViewById(R.id.tvPhone);
            mImgTransferType = (ImageView) view.findViewById(R.id.imgTransferType);
            mViewSeparate = view.findViewById(R.id.viewSeparate);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTvDisplayName.getText() + "'";
        }
    }
}
