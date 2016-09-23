package vn.com.vng.zalopay.transfer.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zalopay.ui.widget.recyclerview.OnItemClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.dao.query.LazyList;
import vn.com.vng.zalopay.AndroidApplication;
import vn.com.vng.zalopay.R;
import vn.com.vng.zalopay.data.cache.model.ZaloFriendGD;
import vn.com.vng.zalopay.domain.model.ZaloFriend;
import vn.com.vng.zalopay.utils.ImageLoader;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ZaloFriend} and makes a call to the
 * specified {}.
 */
public class ZaloContactRecyclerViewAdapter extends AbstractLazyListAdapter<ZaloFriendGD, ZaloContactRecyclerViewAdapter.ViewHolder> {

    public interface OnItemInteractionListener {
        void onItemClick(ZaloFriendGD item);
    }

    private OnItemInteractionListener mListener;

    private LayoutInflater mLayoutInflater;

    public ZaloContactRecyclerViewAdapter(Context context, LazyList<ZaloFriendGD> items, OnItemInteractionListener listener) {
        super(context, items);
        this.mListener = listener;
        mLayoutInflater = LayoutInflater.from(context);
    }


    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onListItemClick(View anchor, int position) {
            ZaloFriendGD item = getItem(position);
            if (item != null && mListener != null) {
                mListener.onItemClick(item);
            }
        }

        @Override
        public boolean onListItemLongClick(View anchor, int position) {
            return false;
        }
    };

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mLayoutInflater.inflate(R.layout.fragment_zalo_contact_item, parent, false), mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ZaloFriendGD item = getItem(position);
        if (item != null) {
            holder.bindView(item, position, getItemCount());
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvDisplayName)
        TextView mTvDisplayName;

        @BindView(R.id.imgAvatar)
        SimpleDraweeView mImgAvatar;

        @BindView(R.id.imgZaloPay)
        ImageView mImgZaloPay;

        @BindView(R.id.viewSeparate)
        View mViewSeparate;

        OnItemClickListener listener;

        ImageLoader mImageLoader;

        public ViewHolder(View view, OnItemClickListener listener) {
            super(view);
            this.listener = listener;
            ButterKnife.bind(this, view);
            mImageLoader = AndroidApplication.instance().getAppComponent().imageLoader();
        }

        void bindView(ZaloFriendGD mItem, int position, int itemCount) {
            mTvDisplayName.setText(mItem.getDisplayName());
            mImageLoader.loadImage(mImgAvatar, mItem.getAvatar());
            if (position < itemCount - 1) {
                mViewSeparate.setVisibility(View.VISIBLE);
            } else {
                mViewSeparate.setVisibility(View.GONE);
            }
            if (mItem.getUsingApp()) {
                mImgZaloPay.setVisibility(View.VISIBLE);
            } else {
                mImgZaloPay.setVisibility(View.GONE);
            }
        }

        @OnClick(R.id.itemLayout)
        public void onItemClick(View v) {
            if (listener != null) {
                listener.onListItemClick(v, getAdapterPosition());
            }
        }
    }
}
